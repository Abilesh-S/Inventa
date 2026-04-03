package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class InventoryDTO {

    private String ingredientName;
    private Double quantity;
    private Long warehouseId;
    private String unit;

}