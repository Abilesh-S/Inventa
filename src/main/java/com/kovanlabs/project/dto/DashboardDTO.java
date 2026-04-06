package com.kovanlabs.project.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private String warehouseName;
    private long totalProducts;
    private double warehouseStock;
    private long inventoryCount;
    private long outOfStockCount;
    private long lowStockCount;
    private long expiringCount;
    private long totalBranches;
    private double foodWastage;
    private double totalProfit;
    private double profitGrowth;
    private double overallPercentage;
    private List<Map<String, Object>> recentActivity;
    private List<Map<String, Object>> stockLevels;
    private List<Map<String, Object>> orderSummary;
    private List<Map<String, Object>> profitByCategory;

    // Getters and Setters
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public double getWarehouseStock() { return warehouseStock; }
    public void setWarehouseStock(double warehouseStock) { this.warehouseStock = warehouseStock; }

    public long getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(long inventoryCount) { this.inventoryCount = inventoryCount; }

    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getExpiringCount() { return expiringCount; }
    public void setExpiringCount(long expiringCount) { this.expiringCount = expiringCount; }

    public long getTotalBranches() { return totalBranches; }
    public void setTotalBranches(long totalBranches) { this.totalBranches = totalBranches; }

    public double getFoodWastage() { return foodWastage; }
    public void setFoodWastage(double foodWastage) { this.foodWastage = foodWastage; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }

    public double getProfitGrowth() { return profitGrowth; }
    public void setProfitGrowth(double profitGrowth) { this.profitGrowth = profitGrowth; }

    public double getOverallPercentage() { return overallPercentage; }
    public void setOverallPercentage(double overallPercentage) { this.overallPercentage = overallPercentage; }

    public List<Map<String, Object>> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<Map<String, Object>> recentActivity) { this.recentActivity = recentActivity; }

    public List<Map<String, Object>> getStockLevels() { return stockLevels; }
    public void setStockLevels(List<Map<String, Object>> stockLevels) { this.stockLevels = stockLevels; }

    public List<Map<String, Object>> getOrderSummary() { return orderSummary; }
    public void setOrderSummary(List<Map<String, Object>> orderSummary) { this.orderSummary = orderSummary; }

    public List<Map<String, Object>> getProfitByCategory() { return profitByCategory; }
    public void setProfitByCategory(List<Map<String, Object>> profitByCategory) { this.profitByCategory = profitByCategory; }
}
