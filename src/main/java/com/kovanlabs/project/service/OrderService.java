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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String WALK_IN_TAG = "WALKIN";

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
                    logger.error("Core product lookup failed for ID: {}", dto.getProductId());
                    return new IllegalArgumentException("Product not found with id: " + dto.getProductId());
                });


        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            logger.warn("Invalid quantity: {}", dto.getQuantity());
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        deductIngredients(dto.getBranchId(), product, dto.getQuantity());

        Customer customer = customerRepository.findByPhone(WALK_IN_TAG)
                .orElseGet(() -> {
                    logger.info("Initializing new walk-in customer profile");
                    return new Customer();
                });
        customer.setName("Walk-in Customer");
        customer.setPhone(WALK_IN_TAG);
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
            throw new IllegalArgumentException("Customer name and phone are required for personalized orders");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            logger.warn("Invalid quantity requested: {}", dto.getQuantity());
            throw new IllegalArgumentException("Order quantity must be positive");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    logger.error("Specific product retrieval failed for customer order: ID={}", dto.getProductId());
                    return new IllegalArgumentException("Target product not found for id: " + dto.getProductId());
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
        logger.info("Order created successfully: orderId={}, Received Phone: {}", order.getId(), dto.getCustomerPhone());

        double taxPercent = dto.getTaxPercent() == null ? 5.0 : dto.getTaxPercent();
        Bill bill = buildAndSaveBill(order, customer, product, dto.getQuantity(), taxPercent);

        logger.info("Order/Bill ready. Preparing SMS for Customer Phone: {}", customer.getPhone());
        try {
            smsService.sendBillSms(customer, bill);
        } catch (Exception e) {
            logger.error("SYSTEM CRASH in SMS trigger for order {}: {}", bill.getId(), e.getMessage(), e);
        }

        logger.info("Order processed successfully. ID={}, Total={}", bill.getId(), bill.getTotalAmount());
        return new OrderBillResponseDTO(order, bill);
    }

    public Bill getBillByOrderId(Long orderId) {
        logger.info("Fetching bill for orderId={}", orderId);
        return billRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Financial record missing for requested orderId: {}", orderId);
                    return new IllegalStateException("Bill record not discovered for order id: " + orderId);
                });
    }

    public List<Bill> getAllBills() {
        return billRepository.findAllByOrderByCreatedAtDesc();
    }

    private void deductIngredients(Long branchId, Product product, Integer orderedQty) {
        logger.info("Deducting ingredients for productId={}, branchId={}, quantity={}",
                product.getId(), branchId, orderedQty);
        List<Recipe> recipes = recipeRepository.findByProductId(product.getId());

        for (Recipe r : recipes) {

            String name = r.getIngredientName().trim().toLowerCase();
            logger.info("Diagnosis checking: " + name);

            List<BranchInventory> batches =
                    branchInventoryRepository
                            .findByBranchIdAndIngredientNameIgnoreCaseAndStatusOrderByExpiryDateAsc(
                                    branchId, name, "ACTIVE"
                            );

            if (batches.isEmpty()) {
                throw new IllegalStateException("Required ingredient unavailable: " + name + " in Branch ID " + branchId);
            }

            double remainingQty = r.getQuantity() * orderedQty;
            boolean fulfilled = false;

            for (BranchInventory batch : batches) {
                // Ignore expired stock batches
                if (batch.getExpiryDate().isAfter(LocalDate.now().minusDays(1))) {
                    double available = batch.getQuantity();
                    double deduct = Math.min(available, remainingQty);

                    batch.setQuantity(available - deduct);
                    branchInventoryRepository.save(batch);
                    remainingQty -= deduct;

                    auditService.log(
                            "STOCK_DEDUCTED",
                            "BranchInventory",
                            String.format("Deducted %.2f of %s from Batch %s", deduct, name, batch.getBatchNumber()),
                            batch.getId()
                    );

                    if (remainingQty <= 0) {
                        fulfilled = true;
                        break; // Exit loop early once stock is fulfilled
                    }
                }
            }

            if (!fulfilled) {
                throw new IllegalStateException(String.format("Insufficient unexpired stock for %s. Missing: %.2f", name, remainingQty));
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