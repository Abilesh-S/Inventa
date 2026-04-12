package com.kovanlabs.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kovanlabs.project.model.Warehouse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor

public class WarehouseInventoryDTO {

    private Long id;
    private String ingredientName;
    private Double quantity;
    private Double pricePerUnit;
    private String unit;
    private Long warehouseId;

    private Double threshold;
    private String batchNumber;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}
