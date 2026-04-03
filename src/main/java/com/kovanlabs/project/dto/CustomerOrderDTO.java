package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class CustomerOrderDTO {
    private Long branchId;
    private Long productId;
    private Integer quantity;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Double taxPercent;

}
