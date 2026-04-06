package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.DashboardDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;
    private final AuditLogRepository auditLogRepository;
    private final BillRepository billRepository;

    public DashboardService(ProductRepository productRepository,
                            WarehouseInventoryRepository warehouseInventoryRepository,
                            WarehouseRepository warehouseRepository,
                            BranchRepository branchRepository,
                            AuditLogRepository auditLogRepository,
                            BillRepository billRepository) {
        this.productRepository = productRepository;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.branchRepository = branchRepository;
        this.auditLogRepository = auditLogRepository;
        this.billRepository = billRepository;
    }

    public DashboardDTO getStats(User user) {
        DashboardDTO dto = new DashboardDTO();
        if (user == null || user.getBusiness() == null) return dto;

        Long businessId = user.getBusiness().getId();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        Warehouse warehouse = warehouseRepository.findAll().stream()
                .filter(w -> w.getBusiness() != null && businessId.equals(w.getBusiness().getId()))
                .findFirst().orElse(null);
        if (warehouse != null) {
            dto.setWarehouseName(warehouse.getName());
        }

        long branchCount = branchRepository.findAll().stream()
                .filter(b -> b.getBusiness() != null && businessId.equals(b.getBusiness().getId()))
                .count();
        dto.setTotalBranches(branchCount);

        dto.setTotalProducts(productRepository.count());

        List<WarehouseInventory> warehouseItems = warehouseInventoryRepository.findAll()
                .stream()
                .filter(wi -> wi != null && wi.getWarehouse() != null &&
                        wi.getWarehouse().getBusiness() != null &&
                        businessId.equals(wi.getWarehouse().getBusiness().getId()))
                .collect(Collectors.toList());

        long inventoryCount = warehouseItems.stream()
                .map(WarehouseInventory::getIngredientName)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        dto.setInventoryCount(inventoryCount);

        double totalWarehouseStock = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null && !"EXPIRED".equalsIgnoreCase(wi.getStatus()))
                .mapToDouble(WarehouseInventory::getQuantity)
                .sum();
        dto.setWarehouseStock(totalWarehouseStock);

        long outOfStockCount = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null && wi.getQuantity() <= 0)
                .map(WarehouseInventory::getIngredientName)
                .distinct()
                .count();
        dto.setOutOfStockCount(outOfStockCount);

        long lowStockCount = warehouseItems.stream()
                .filter(wi -> wi.getIngredientName() != null && wi.getQuantity() != null && wi.getThreshold() != null
                        && wi.getQuantity() > 0 && wi.getQuantity() <= wi.getThreshold())
                .map(WarehouseInventory::getIngredientName)
                .distinct()
                .count();
        dto.setLowStockCount(lowStockCount);

        long expiringCount = warehouseItems.stream()
                .filter(wi -> wi.getExpiryDate() != null && wi.getExpiryDate().isAfter(today.minusDays(1))
                        && wi.getExpiryDate().isBefore(nextWeek))
                .map(WarehouseInventory::getIngredientName)
                .distinct()
                .count();
        dto.setExpiringCount(expiringCount);

        double wastage = warehouseItems.stream()
                .filter(wi -> wi.getQuantity() != null &&
                        (wi.getExpiryDate() != null && wi.getExpiryDate().isBefore(today) || "EXPIRED".equalsIgnoreCase(wi.getStatus())))
                .mapToDouble(WarehouseInventory::getQuantity)
                .sum();
        dto.setFoodWastage(wastage);

        double totalRevenue = billRepository.findAll().stream()
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(Bill::getTotalAmount)
                .sum();
        dto.setTotalProfit(totalRevenue);
        dto.setProfitGrowth(15.8);
        dto.setOverallPercentage(84.0);

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

        // 9. Critical Warehouse Stock Levels
        dto.setStockLevels(warehouseItems.stream()
                .filter(wi -> wi.getIngredientName() != null && wi.getQuantity() != null && wi.getThreshold() != null && wi.getQuantity() <= wi.getThreshold())
                .map(wi -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", wi.getIngredientName() + " (Batch: " + (wi.getBatchNumber() != null ? wi.getBatchNumber() : "N/A") + ")");
                    map.put("left", wi.getQuantity());
                    map.put("total", wi.getThreshold() > 0 ? wi.getThreshold() * 2 : 100);
                    return map;
                }).collect(Collectors.toList()));

        dto.setOrderSummary(generateMockOrderSummary());

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
                map.put("percent", total > 0 ? (int)((double)entry.getValue() / total * 100) : 0);
                map.put("color", colors[i % colors.length]);
                profits.add(map);
                i++;
            }
            return profits;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> generateMockOrderSummary() {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] values = {65, 59, 80, 81, 56, 55, 40};
        List<Map<String, Object>> summary = new ArrayList<>();
        for(int i=0; i<days.length; i++){
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", days[i]);
            dayMap.put("value", values[i]);
            summary.add(dayMap);
        }
        return summary;
    }
}
