package com.kovanlabs.project.dto;

import lombok.Data;

@Data
public class RecipeDTO {

    private Long productId;
    private String ingredientName;
    private Double quantity;
    private String unit;


}