package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.OrderDTO;
import com.kovanlabs.project.dto.CustomerOrderDTO;
import com.kovanlabs.project.dto.OrderBillResponseDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final LowStockAlertService lowStockAlertService;
    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;
    private final AuditService auditService;
    private final SmsService smsService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        RecipeRepository recipeRepository,
                        BranchInventoryRepository branchInventoryRepository,
                        LowStockAlertService lowStockAlertService,
                        CustomerRepository customerRepository,
                        BillRepository billRepository,
                        AuditService auditService,
                        SmsService smsService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.lowStockAlertService = lowStockAlertService;
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
        this.auditService=auditService;
        this.smsService = smsService;
    }

    public OrderBillResponseDTO placeOrder(OrderDTO dto) {
        logger.info("Placing walk-in order: productId={}, quantity={}, branchId={}",
                dto.getProductId(), dto.getQuantity(), dto.getBranchId());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product not found with id={}", dto.getProductId());
                    return new RuntimeException("Product not found");
                });


        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            logger.warn("Invalid quantity: {}", dto.getQuantity());
            throw new RuntimeException("quantity must be greater than zero");
        }

        deductIngredients(dto.getBranchId(), product, dto.getQuantity());

        Customer customer = customerRepository.findByPhone("WALKIN")
                .orElseGet(() -> {
                    logger.info("Creating new walk-in customer");
                    return new Customer();
                });
        customer.setName("Walk-in Customer");
        customer.setPhone("WALKIN");
        customer.setEmail(null);
        customer = customerRepository.save(customer);

        Order order = new Order();
        order.setBranchId(dto.getBranchId());
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity());
        order.setCustomer(customer);
        order = orderRepository.save(order);
        logger.info("Order created successfully: orderId={}", order.getId());
        auditService.log(
                "ORDER_CREATED",
                "Order",
                "Order placed for product: " + product.getName() +
                        " quantity: " + dto.getQuantity(),
                order.getId()
        );

        Bill bill = buildAndSaveBill(order, customer, product, dto.getQuantity(), 0.0);
        logger.info("Bill generated: billId={}, totalAmount={}", bill.getId(), bill.getTotalAmount());
        return new OrderBillResponseDTO(order, bill);
    }

    public OrderBillResponseDTO placeCustomerOrder(CustomerOrderDTO dto) {
        logger.info("Placing customer order: productId={}, quantity={}, customerPhone={}",
                dto.getProductId(), dto.getQuantity(), dto.getCustomerPhone());
        if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()
                || dto.getCustomerPhone() == null || dto.getCustomerPhone().trim().isEmpty()) {
            logger.warn("Invalid customer details: name={}, phone={}",
                    dto.getCustomerName(), dto.getCustomerPhone());
            throw new RuntimeException("customerName and customerPhone are required");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            logger.warn("Invalid quantity: {}", dto.getQuantity());
            throw new RuntimeException("quantity must be greater than zero");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product not found with id={}", dto.getProductId());
                    return new RuntimeException("Product not found");
                });


        deductIngredients(dto.getBranchId(), product, dto.getQuantity());

        Customer customer = customerRepository.findByPhone(dto.getCustomerPhone())
                .orElseGet(() -> {
                    logger.info("Creating new customer with phone={}", dto.getCustomerPhone());
                    return new Customer();
                });

        customer.setName(dto.getCustomerName());
        customer.setPhone(dto.getCustomerPhone());
        customer.setEmail(dto.getCustomerEmail());
        customer = customerRepository.save(customer);

        Order order = new Order();
        order.setBranchId(dto.getBranchId());
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity());
        order.setCustomer(customer);
        order = orderRepository.save(order);
        logger.info("Order created successfully: orderId={}", order.getId());

        double taxPercent = dto.getTaxPercent() == null ? 5.0 : dto.getTaxPercent();
        Bill bill = buildAndSaveBill(order, customer, product, dto.getQuantity(), taxPercent);

        // SMS send should never break the billing flow.
        smsService.sendBillSms(customer, bill);

        logger.info("Bill generated: billId={}, totalAmount={}", bill.getId(), bill.getTotalAmount());
        return new OrderBillResponseDTO(order, bill);
    }

    public Bill getBillByOrderId(Long orderId) {
        logger.info("Fetching bill for orderId={}", orderId);
        return billRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Bill not found for orderId={}", orderId);
                    return new RuntimeException("Bill not found for order id: " + orderId);
                });
    }

    private void deductIngredients(Long branchId, Product product, Integer orderedQty) {
        logger.info("Deducting ingredients for productId={}, branchId={}, quantity={}",
                product.getId(), branchId, orderedQty);
        List<Recipe> recipes = recipeRepository.findByProductId(product.getId());

        for (Recipe r : recipes) {

            String name = r.getIngredientName().trim().toLowerCase();

            List<BranchInventory> batches =
                    branchInventoryRepository
                            .findByBranchIdAndIngredientNameIgnoreCaseAndStatusOrderByExpiryDateAsc(
                                    branchId, name, "ACTIVE"
                            );

            if (batches.isEmpty()) {
                throw new RuntimeException("Ingredient not found: " + name);
            }

            double requiredQty = r.getQuantity() * orderedQty;
            double remainingQty = requiredQty;

            boolean fulfilled = false;

            for (BranchInventory batch : batches) {

                if (batch.getExpiryDate().isBefore(LocalDate.now())) {
                    continue;

                }

                double available = batch.getQuantity();

                if (available >= remainingQty) {

                    batch.setQuantity(available - remainingQty);
                    branchInventoryRepository.save(batch);
                    auditService.log(
                            "STOCK_DEDUCTED",
                            "BranchInventory",
                            "Used " + remainingQty + " of " + name +
                                    " (Batch: " + batch.getBatchNumber() + ")",
                            batch.getId()
                    );

                    fulfilled = true;
                    break;
                } else {

                    batch.setQuantity(0.0);
                    branchInventoryRepository.save(batch);

                    remainingQty -= available;
                    auditService.log(
                            "STOCK_DEDUCTED",
                            "BranchInventory",
                            "Used " + remainingQty + " of " + name +
                                    " (Batch: " + batch.getBatchNumber() + ")",
                            batch.getId()
                    );
                }
            }

            if (!fulfilled) {
                throw new RuntimeException(name + " is expired or not enough stock");
            }


            batches.forEach(lowStockAlertService::createIfLowStock);
        }

}

    private Bill buildAndSaveBill(Order order, Customer customer, Product product, Integer quantity, Double taxPercent) {
        double unitPrice = product.getPrice() == null ? 0.0 : product.getPrice();
        double subtotal = unitPrice * quantity;
        double normalizedTax = taxPercent == null ? 0.0 : taxPercent;
        double taxAmount = subtotal * (normalizedTax / 100.0);
        double totalAmount = subtotal + taxAmount;

        logger.info("Calculating bill: orderId={}, subtotal={}, taxPercent={}",
                order.getId(), subtotal, normalizedTax);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setCustomer(customer);
        bill.setProductName(product.getName());
        bill.setUnitPrice(unitPrice);
        bill.setQuantity(quantity);
        bill.setSubtotal(subtotal);
        bill.setTaxPercent(normalizedTax);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        logger.debug("Bill saved: billId={}, totalAmount={}", bill.getId(), totalAmount);

        return billRepository.save(bill);
    }
}