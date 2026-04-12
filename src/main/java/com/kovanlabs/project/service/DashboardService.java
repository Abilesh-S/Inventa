package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.DashboardDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final BillRepository billRepository;
    private final BranchInventoryService branchInventoryService;
    private final UserRepository userRepository;

    public DashboardService(ProductRepository productRepository,
                            WarehouseInventoryRepository warehouseInventoryRepository,
                            WarehouseRepository warehouseRepository,
                            BranchRepository branchRepository,
                            BranchInventoryRepository branchInventoryRepository,
                            AuditLogRepository auditLogRepository,
                            BillRepository billRepository,
                            BranchInventoryService branchInventoryService,
                            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.branchRepository = branchRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.billRepository = billRepository;
        this.branchInventoryService = branchInventoryService;
        this.userRepository = userRepository;
    }

    public DashboardDTO getStats(User user, Long branchId, String period) {
        DashboardDTO dto = new DashboardDTO();
        if (user == null || user.getBusiness() == null) return dto;

        Long businessId = user.getBusiness().getId();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        // 0. Set Warehouse Identity & Branch Count
        Warehouse warehouse = warehouseRepository.findAll().stream()
                .filter(w -> w.getBusiness() != null && businessId.equals(w.getBusiness().getId()))
                .findFirst().orElse(null);
        if (warehouse != null) {
            dto.setWarehouseName(warehouse.getName());
        }

        long branchCount = branchRepository.findAll().stream()
                .filter(b -> b.getBusiness() != null && businessId.equals(b.getBusiness().getId()))
                .count();
        dto.setTotalBranches(branchId != null ? 1 : branchCount);

        long userCount = (branchId != null)
                ? userRepository.findByBranchId(branchId).size()
                : userRepository.findByBusinessId(businessId).size();
        dto.setTotalUsers(userCount);

        // 1. Total Final Products (business-scoped)
        dto.setTotalProducts(productRepository.countByBusinessId(businessId));

        // 2. Fetch Central Warehouse Inventory for the user's business
        List<WarehouseInventory> warehouseItems = warehouseInventoryRepository.findAll()
                .stream()
                .filter(wi -> wi != null && wi.getWarehouse() != null &&
                        wi.getWarehouse().getBusiness() != null &&
                        businessId.equals(wi.getWarehouse().getBusiness().getId()))
                .collect(Collectors.toList());

        // Inventory Variety (Distinct ingredient types)
        long inventoryCount = warehouseItems.stream()
                .map(WarehouseInventory::getIngredientName)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        dto.setInventoryCount(inventoryCount);

        // Available Warehouse Stock
        double totalWarehouseStock = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null && !"EXPIRED".equalsIgnoreCase(wi.getStatus()))
                .mapToDouble(WarehouseInventory::getQuantity)
                .sum();
        dto.setWarehouseStock(totalWarehouseStock);

        // Total Branch Units
        if (branchId != null) {
            dto.setTotalBranchInventoryUnits(branchInventoryRepository.findByBranchId(branchId).stream()
                    .mapToDouble(BranchInventory::getQuantity).sum());
        } else {
            dto.setTotalBranchInventoryUnits(branchInventoryService.getTotalQuantityAcrossBranches(businessId));
        }

        // 2b. Overall inventory (warehouse + branch) and cost for owner dashboard
        double totalInventoryUnits = dto.getWarehouseStock() + dto.getTotalBranchInventoryUnits();
        dto.setTotalInventoryUnits(totalInventoryUnits);
        double warehouseInventoryCost = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null && wi.getPricePerUnit() != null)
                .mapToDouble(wi -> wi.getQuantity() * wi.getPricePerUnit())
                .sum();

        List<BranchInventory> branchRowsForCost = (branchId != null)
                ? branchInventoryRepository.findByBranchId(branchId)
                : branchInventoryRepository.findByBranch_Business_Id(businessId);

        double branchInventoryCost = branchRowsForCost.stream()
                .filter(bi -> bi.getQuantity() != null && bi.getPricePerUnit() != null)
                .mapToDouble(bi -> bi.getQuantity() * bi.getPricePerUnit())
                .sum();

        dto.setWarehouseInventoryCost(warehouseInventoryCost);
        dto.setBranchInventoryCost(branchInventoryCost);
        dto.setEstimatedInventoryCost(warehouseInventoryCost + branchInventoryCost);

        // 3. Out of Stock
        long outOfStockCount = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null && wi.getQuantity() <= 0)
                .map(WarehouseInventory::getIngredientName)
                .distinct()
                .count();
        dto.setOutOfStockCount(outOfStockCount);

        // 4. Low Stock — includes expired
        List<BranchInventory> lowStockSource = (branchId != null)
                ? branchInventoryRepository.findByBranchId(branchId)
                : branchInventoryRepository.findByBranch_Business_Id(businessId);

        long lowStockCount = lowStockSource.stream()
                .filter(bi -> bi.getIngredientName() != null
                        && bi.getQuantity() != null
                        && bi.getThreshold() != null)
                .filter(bi -> {
                    boolean qtyLow = bi.getQuantity() > 0 && bi.getQuantity() <= bi.getThreshold();
                    boolean expired = (bi.getExpiryDate() != null && bi.getExpiryDate().isBefore(today))
                            || ("EXPIRED".equalsIgnoreCase(bi.getStatus()));
                    return qtyLow || expired;
                })
                .count();
        dto.setLowStockCount(lowStockCount);

        // 5. Expiring Soon (Next 7 days)
        long expiringCount = lowStockSource.stream()
                .filter(bi -> bi.getExpiryDate() != null && bi.getExpiryDate().isAfter(today.minusDays(1))
                        && bi.getExpiryDate().isBefore(nextWeek))
                .map(BranchInventory::getIngredientName)
                .distinct()
                .count();
        dto.setExpiringCount(expiringCount);

        // 6. Food Wastage (Expired)
        double wastage = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null &&
                        (wi.getExpiryDate() != null && wi.getExpiryDate().isBefore(today) || "EXPIRED".equalsIgnoreCase(wi.getStatus())))
                .mapToDouble(WarehouseInventory::getQuantity)
                .sum();
        dto.setFoodWastage(wastage);

        // 7. Financial Analytics (Filtered by Branch if requested)
        List<Bill> allBills = (branchId != null)
                ? billRepository.findByBranchIdOrderByCreatedAtDesc(branchId)
                : billRepository.findAll();

        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate sixtyDaysAgo = today.minusDays(60);

        double monthlyRevenue = allBills.stream()
                .filter(b -> b.getTotalAmount() != null && b.getCreatedAt() != null && b.getCreatedAt().toLocalDate().isAfter(thirtyDaysAgo.minusDays(1)))
                .mapToDouble(Bill::getTotalAmount)
                .sum();
        dto.setTotalProfit(monthlyRevenue);

        double previousMonthlyRevenue = allBills.stream()
                .filter(b -> b.getTotalAmount() != null && b.getCreatedAt() != null
                        && b.getCreatedAt().toLocalDate().isAfter(sixtyDaysAgo.minusDays(1))
                        && b.getCreatedAt().toLocalDate().isBefore(thirtyDaysAgo.plusDays(1)))
                .mapToDouble(Bill::getTotalAmount)
                .sum();

        double todayRevenue = allBills.stream()
                .filter(b -> b.getCreatedAt() != null && b.getCreatedAt().toLocalDate().equals(today))
                .mapToDouble(Bill::getTotalAmount)
                .sum();
        dto.setTodayProfit(todayRevenue);

        double growth = (previousMonthlyRevenue > 0)
                ? ((monthlyRevenue - previousMonthlyRevenue) / previousMonthlyRevenue) * 100
                : 0;
        dto.setProfitGrowth(growth);

        dto.setOverallPercentage(84.0);

        // 8. Categories & Activity
        dto.setProfitByCategory(generateRealProfitByCategory());

        try {
            List<AuditLog> logs = auditLogRepository.findAllByOrderByTimestampDesc();
            if (logs != null) {
                dto.setRecentActivity(logs.stream().limit(10).map(l -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("action", l.getAction() != null ? l.getAction() : "ACTION");
                    map.put("details", l.getDetails() != null ? l.getDetails() : "");
                    map.put("timestamp", l.getTimestamp() != null ? l.getTimestamp().toString() : "");
                    return map;
                }).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            dto.setRecentActivity(new ArrayList<>());
        }

        // 9. Critical Stock Levels (Use branch stock if branch selected, else warehouse)
        if (branchId != null) {
            dto.setStockLevels(lowStockSource.stream()
                    .filter(bi -> bi.getQuantity() <= bi.getThreshold())
                    .limit(5)
                    .map(bi -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", bi.getIngredientName());
                        map.put("branchId", bi.getBranch() != null ? bi.getBranch().getId() : branchId);
                        map.put("left", bi.getQuantity());
                        return map;
                    }).collect(Collectors.toList()));
        } else {
            dto.setStockLevels(warehouseItems.stream()
                    .filter(wi -> wi.getIngredientName() != null && wi.getQuantity() != null && wi.getThreshold() != null && wi.getQuantity() <= wi.getThreshold())
                    .map(wi -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", wi.getIngredientName() + " (Batch: " + (wi.getBatchNumber() != null ? wi.getBatchNumber() : "N/A") + ")");
                        map.put("left", wi.getQuantity());
                        map.put("total", wi.getThreshold() > 0 ? wi.getThreshold() * 2 : 100);
                        return map;
                    }).collect(Collectors.toList()));
        }

        // 10. Recent Orders (Bills)
        dto.setRecentOrders(allBills.stream().limit(10).map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("customerName", b.getCustomer() != null ? b.getCustomer().getName() : "Walk-in");
            map.put("productName", b.getProductName());
            map.put("totalAmount", b.getTotalAmount());
            map.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            map.put("branchId", b.getOrder() != null ? b.getOrder().getBranchId() : null);
            return map;
        }).collect(Collectors.toList()));

        dto.setOrderSummary(generateOrderSummary(today, branchId, period));

        // 11. Low Stock Items — warehouse + all branches (or filtered branch)
        List<Map<String, Object>> lowStockList = new ArrayList<>();

        // Warehouse low-stock items
        warehouseItems.stream()
                .filter(wi -> wi.getIngredientName() != null
                        && wi.getQuantity() != null
                        && wi.getThreshold() != null
                        && (wi.getQuantity() <= wi.getThreshold()
                        || (wi.getExpiryDate() != null && wi.getExpiryDate().isBefore(today))
                        || "EXPIRED".equalsIgnoreCase(wi.getStatus())))
                .forEach(wi -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ingredientName", wi.getIngredientName());
                    m.put("quantity", wi.getQuantity());
                    m.put("threshold", wi.getThreshold());
                    m.put("unit", wi.getUnit() != null ? wi.getUnit() : "");
                    m.put("source", "Warehouse");
                    m.put("sourceName", warehouse != null ? warehouse.getName() : "Warehouse");
                    boolean expired = wi.getExpiryDate() != null && wi.getExpiryDate().isBefore(today)
                            || "EXPIRED".equalsIgnoreCase(wi.getStatus());
                    m.put("status", expired ? "EXPIRED" : "LOW");
                    lowStockList.add(m);
                });

        // Branch low-stock items
        lowStockSource.stream()
                .filter(bi -> bi.getIngredientName() != null
                        && bi.getQuantity() != null
                        && bi.getThreshold() != null
                        && (bi.getQuantity() <= bi.getThreshold()
                        || (bi.getExpiryDate() != null && bi.getExpiryDate().isBefore(today))
                        || "EXPIRED".equalsIgnoreCase(bi.getStatus())))
                .forEach(bi -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ingredientName", bi.getIngredientName());
                    m.put("quantity", bi.getQuantity());
                    m.put("threshold", bi.getThreshold());
                    m.put("unit", bi.getUnit() != null ? bi.getUnit() : "");
                    m.put("source", "Branch");
                    m.put("sourceName", bi.getBranch() != null ? bi.getBranch().getName() : "Branch");
                    boolean expired = (bi.getExpiryDate() != null && bi.getExpiryDate().isBefore(today))
                            || "EXPIRED".equalsIgnoreCase(bi.getStatus());
                    m.put("status", expired ? "EXPIRED" : "LOW");
                    lowStockList.add(m);
                });

        dto.setLowStockItems(lowStockList);

        return dto;
    }

    private List<Map<String, Object>> generateRealProfitByCategory() {
        try {
            Map<String, Long> categoryCounts = productRepository.findAll().stream()
                    .collect(Collectors.groupingBy(p -> p.getCategory() == null ? "Uncategorized" : p.getCategory(), Collectors.counting()));
            long total = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
            String[] colors = {"primary", "on-surface", "surface-dim", "error", "warning"};
            List<Map<String, Object>> profits = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", entry.getKey());
                map.put("percent", total > 0 ? (int) ((double) entry.getValue() / total * 100) : 0);
                map.put("color", colors[i % colors.length]);
                profits.add(map);
                i++;
            }
            return profits;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Generates order summary for daily (7 days), weekly (4 weeks), or monthly (12 months).
     */
    private List<Map<String, Object>> generateOrderSummary(LocalDate today, Long branchId, String period) {
        try {
            if ("weekly".equalsIgnoreCase(period)) {
                return generateWeeklyBuckets(today, branchId);
            } else if ("monthly".equalsIgnoreCase(period)) {
                return generateMonthlyBuckets(today, branchId);
            } else {
                return generateDailyBuckets(today, branchId);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Last 7 days, one bar per day
     */
    private List<Map<String, Object>> generateDailyBuckets(LocalDate today, Long branchId) {
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Bill> bills = fetchBills(branchId, start, end);

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) counts.put(startDate.plusDays(i), 0L);
        for (Bill b : bills) {
            if (b.getCreatedAt() == null) continue;
            LocalDate d = b.getCreatedAt().toLocalDate();
            if (counts.containsKey(d)) counts.put(d, counts.get(d) + 1);
        }

        List<Map<String, Object>> summary = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> e : counts.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("day", e.getKey().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            m.put("value", e.getValue().intValue());
            summary.add(m);
        }
        return summary;
    }

    /**
     * Last 4 weeks, one bar per week (Mon–Sun)
     */
    private List<Map<String, Object>> generateWeeklyBuckets(LocalDate today, Long branchId) {
        // Start from 4 weeks ago (Monday of that week)
        LocalDate startDate = today.minusWeeks(3)
                .with(java.time.DayOfWeek.MONDAY);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Bill> bills = fetchBills(branchId, start, end);

        // Build 4 week buckets
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) counts.put(startDate.plusWeeks(i), 0L);

        for (Bill b : bills) {
            if (b.getCreatedAt() == null) continue;
            LocalDate d = b.getCreatedAt().toLocalDate();
            // Find which week bucket this falls into
            for (LocalDate weekStart : counts.keySet()) {
                LocalDate weekEnd = weekStart.plusDays(6);
                if (!d.isBefore(weekStart) && !d.isAfter(weekEnd)) {
                    counts.put(weekStart, counts.get(weekStart) + 1);
                    break;
                }
            }
        }

        List<Map<String, Object>> summary = new ArrayList<>();
        int weekNum = 1;
        for (Map.Entry<LocalDate, Long> e : counts.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("day", "W" + weekNum++);
            m.put("value", e.getValue().intValue());
            summary.add(m);
        }
        return summary;
    }

    /**
     * Last 12 months, one bar per month
     */
    private List<Map<String, Object>> generateMonthlyBuckets(LocalDate today, Long branchId) {
        LocalDate startDate = today.minusMonths(11).withDayOfMonth(1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Bill> bills = fetchBills(branchId, start, end);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            LocalDate m = startDate.plusMonths(i);
            String label = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            counts.put(label, 0L);
        }

        for (Bill b : bills) {
            if (b.getCreatedAt() == null) continue;
            String label = b.getCreatedAt().toLocalDate().getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (counts.containsKey(label)) counts.put(label, counts.get(label) + 1);
        }

        List<Map<String, Object>> summary = new ArrayList<>();
        for (Map.Entry<String, Long> e : counts.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("day", e.getKey());
            m.put("value", e.getValue().intValue());
            summary.add(m);
        }
        return summary;
    }

    private List<Bill> fetchBills(Long branchId, LocalDateTime start, LocalDateTime end) {
        return (branchId != null)
                ? billRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end)
                : billRepository.findByCreatedAtBetween(start, end);
    }

    // Keep old name as alias so nothing else breaks
    private List<Map<String, Object>> generateWeeklyOrderSummary(LocalDate today, Long branchId) {
        return generateDailyBuckets(today, branchId);
    }
}