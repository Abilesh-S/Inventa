package com.kovanlabs.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private long id;
    private String name;
    private Double price;
    private String category;
    private String description;
    private String instructions;
    private String imageUrl;
    private java.util.List<RecipeDTO> recipes;

}