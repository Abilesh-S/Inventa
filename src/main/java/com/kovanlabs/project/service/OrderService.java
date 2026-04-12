package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.OrderDTO;
import com.kovanlabs.project.dto.CustomerOrderDTO;
import com.kovanlabs.project.dto.OrderBillResponseDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
                        SmsService smsService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.lowStockAlertService = lowStockAlertService;
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
        this.auditService = auditService;
        this.smsService = smsService;
    }

    public OrderBillResponseDTO placeOrder(OrderDTO dto) {
        logger.info("Placing walk-in order: productId={}, quantity={}, branchId={}",
                dto.getProductId(), dto.getQuantity(), dto.getBranchId());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + dto.getProductId()));

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        deductIngredients(dto.getBranchId(), product, dto.getQuantity());

        Customer customer = customerRepository.findByPhone(WALK_IN_TAG).orElseGet(Customer::new);
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

        auditService.log("ORDER_CREATED", "Order",
                "Order placed for product: " + product.getName() + " quantity: " + dto.getQuantity(),
                order.getId());

        Bill bill = buildAndSaveBill(order, customer, product, dto.getQuantity(), 0.0);
        logger.info("Bill generated: billId={}, totalAmount={}", bill.getId(), bill.getTotalAmount());
        return new OrderBillResponseDTO(order, bill);
    }

    public OrderBillResponseDTO placeCustomerOrder(CustomerOrderDTO dto) {
        logger.info("Placing customer order: productId={}, quantity={}, customerPhone={}",
                dto.getProductId(), dto.getQuantity(), dto.getCustomerPhone());

        if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()
                || dto.getCustomerPhone() == null || dto.getCustomerPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name and phone are required");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Order quantity must be positive");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Target product not found for id: " + dto.getProductId()));

        deductIngredients(dto.getBranchId(), product, dto.getQuantity());

        Customer customer = customerRepository.findByPhone(dto.getCustomerPhone()).orElseGet(Customer::new);
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

        double taxPercent = dto.getTaxPercent() == null ? 5.0 : dto.getTaxPercent();
        Bill bill = buildAndSaveBill(order, customer, product, dto.getQuantity(), taxPercent);

        try {
            smsService.sendBillSms(customer, bill);
        } catch (Throwable t) {
            logger.error("SMS TRIGGER FAILED (Non-Blocking): orderId={}, error={}", bill.getId(), t.getMessage());
        }

        logger.info("Order processed successfully. ID={}, Total={}", bill.getId(), bill.getTotalAmount());
        return new OrderBillResponseDTO(order, bill);
    }

    public Bill getBillByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Bill not found for order id: " + orderId));
    }

    public List<Bill> getAllBills() {
        return billRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Bill> getBillsByBranchId(Long branchId) {
        return billRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
    }

    // ── Ingredient deduction with unit normalisation ──────────────────────────

    private void deductIngredients(Long branchId, Product product, Integer orderedQty) {
        List<Recipe> recipes = recipeRepository.findByProductId(product.getId());

        for (Recipe r : recipes) {
            String name = r.getIngredientName().trim().toLowerCase();

            List<BranchInventory> batches =
                    branchInventoryRepository
                            .findByBranchIdAndIngredientNameIgnoreCaseAndStatusOrderByExpiryDateAsc(
                                    branchId, name, "ACTIVE");

            if (batches.isEmpty()) {
                throw new IllegalStateException(
                        "Required ingredient not found in branch inventory: " + name);
            }

            String recipeUnit = r.getUnit() != null ? r.getUnit().trim().toLowerCase() : "";
            double neededBase = toBaseUnit(r.getQuantity() * orderedQty, recipeUnit);
            double remainingBase = neededBase;
            boolean fulfilled = false;

            for (BranchInventory batch : batches) {
                boolean notExpired = batch.getExpiryDate() == null
                        || batch.getExpiryDate().isAfter(LocalDate.now().minusDays(1));
                if (!notExpired) continue;

                String batchUnit = batch.getUnit() != null ? batch.getUnit().trim().toLowerCase() : "";
                double availableBase = toBaseUnit(batch.getQuantity(), batchUnit);
                double deductBase = Math.min(availableBase, remainingBase);
                double deductInBatchUnit = fromBaseUnit(deductBase, batchUnit);

                batch.setQuantity(batch.getQuantity() - deductInBatchUnit);
                branchInventoryRepository.save(batch);
                remainingBase -= deductBase;

                auditService.log("STOCK_DEDUCTED", "BranchInventory",
                        String.format("Deducted %.4f %s of %s from Batch %s",
                                deductInBatchUnit, batchUnit, name, batch.getBatchNumber()),
                        batch.getId());

                lowStockAlertService.createIfLowStock(batch);

                if (remainingBase <= 0) {
                    fulfilled = true;
                    break;
                }
            }

            if (!fulfilled) {
                throw new IllegalStateException(String.format(
                        "Insufficient stock for %s. Missing: %.2f %s",
                        name, fromBaseUnit(remainingBase, recipeUnit), recipeUnit));
            }
        }
    }

    /** Converts to base unit: weight → grams, volume → ml, count → unchanged */
    private double toBaseUnit(double qty, String unit) {
        switch (unit) {
            case "kg":                                      return qty * 1000.0;
            case "g":                                       return qty;
            case "mg":                                      return qty / 1000.0;
            case "l": case "ltr": case "liter":
            case "litre": case "liters": case "litres":     return qty * 1000.0;
            case "ml":                                      return qty;
            default:                                        return qty;
        }
    }

    private double fromBaseUnit(double base, String unit) {
        switch (unit) {
            case "kg":                                      return base / 1000.0;
            case "g":                                       return base;
            case "mg":                                      return base * 1000.0;
            case "l": case "ltr": case "liter":
            case "litre": case "liters": case "litres":     return base / 1000.0;
            case "ml":                                      return base;
            default:                                        return base;
        }
    }

    // ── Bill builder ──────────────────────────────────────────────────────────

    private Bill buildAndSaveBill(Order order, Customer customer, Product product,
                                  Integer quantity, Double taxPercent) {
        double unitPrice = product.getPrice() == null ? 0.0 : product.getPrice();
        double subtotal = unitPrice * quantity;
        double normalizedTax = taxPercent == null ? 0.0 : taxPercent;
        double taxAmount = subtotal * (normalizedTax / 100.0);
        double totalAmount = subtotal + taxAmount;

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

        return billRepository.save(bill);
    }
}
