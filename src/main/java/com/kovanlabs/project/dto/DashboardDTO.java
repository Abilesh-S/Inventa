package com.kovanlabs.project.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private String warehouseName;
    private long totalProducts;
    private double warehouseStock;
    /** Sum of branch-ingredient quantities across every branch in the business (same as warehouse inventory “all branches” KPI). */
    private double totalBranchInventoryUnits;
    /** Combined warehouse + branch units used for owner-level inventory overview. */
    private double totalInventoryUnits;
    /** Estimated combined inventory cost (warehouse + branch). */
    private double estimatedInventoryCost;
    /** Warehouse-only inventory cost. */
    private double warehouseInventoryCost;
    /** Branch-only inventory cost. */
    private double branchInventoryCost;
    private long inventoryCount;
    private long outOfStockCount;
    private long lowStockCount;
    private long expiringCount;
    private long totalBranches;
    private long totalUsers;
    private double foodWastage;
    private double totalProfit;
    private double todayProfit;
    private double profitGrowth;
    private double overallPercentage;
    private List<Map<String, Object>> recentActivity;
    private List<Map<String, Object>> stockLevels;
    private List<Map<String, Object>> orderSummary;
    private List<Map<String, Object>> profitByCategory;
    private List<Map<String, Object>> recentOrders;
    private List<Map<String, Object>> lowStockItems;

    // Getters and Setters
    public List<Map<String, Object>> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<Map<String, Object>> recentOrders) { this.recentOrders = recentOrders; }
    public List<Map<String, Object>> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<Map<String, Object>> lowStockItems) { this.lowStockItems = lowStockItems; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public double getWarehouseStock() { return warehouseStock; }
    public void setWarehouseStock(double warehouseStock) { this.warehouseStock = warehouseStock; }

    public double getTotalBranchInventoryUnits() { return totalBranchInventoryUnits; }
    public void setTotalBranchInventoryUnits(double totalBranchInventoryUnits) { this.totalBranchInventoryUnits = totalBranchInventoryUnits; }

    public double getTotalInventoryUnits() { return totalInventoryUnits; }
    public void setTotalInventoryUnits(double totalInventoryUnits) { this.totalInventoryUnits = totalInventoryUnits; }

    public double getEstimatedInventoryCost() { return estimatedInventoryCost; }
    public void setEstimatedInventoryCost(double estimatedInventoryCost) { this.estimatedInventoryCost = estimatedInventoryCost; }

    public double getWarehouseInventoryCost() { return warehouseInventoryCost; }
    public void setWarehouseInventoryCost(double warehouseInventoryCost) { this.warehouseInventoryCost = warehouseInventoryCost; }

    public double getBranchInventoryCost() { return branchInventoryCost; }
    public void setBranchInventoryCost(double branchInventoryCost) { this.branchInventoryCost = branchInventoryCost; }

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

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public double getFoodWastage() { return foodWastage; }
    public void setFoodWastage(double foodWastage) { this.foodWastage = foodWastage; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }

    public double getTodayProfit() { return todayProfit; }
    public void setTodayProfit(double todayProfit) { this.todayProfit = todayProfit; }

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
