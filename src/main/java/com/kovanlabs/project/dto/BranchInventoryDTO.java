package com.kovanlabs.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BranchInventoryDTO {

    private Long id;
    private String ingredientName;
    private Double quantity;
    private Double threshold;
    private String unit;
    private Double pricePerUnit;
    private Long branchId;
    private String batchNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String status;
}