package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class StockRequestCreateDTO {
    private Long branchId;
    private String ingredientName;
    private Double quantity;
    private String unit;


}
