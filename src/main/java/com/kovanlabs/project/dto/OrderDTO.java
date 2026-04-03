package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class OrderDTO {

    private Long branchId;
    private Long productId;
    private Integer quantity;


}