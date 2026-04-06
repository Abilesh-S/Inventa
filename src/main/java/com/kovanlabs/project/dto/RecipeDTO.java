package com.kovanlabs.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {

    private Long productId;
    private String ingredientName;
    private Double quantity;
    private String unit;
    private Long ingredientId;


}