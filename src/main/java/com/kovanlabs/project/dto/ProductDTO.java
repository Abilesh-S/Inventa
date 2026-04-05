package com.kovanlabs.project.dto;

import com.kovanlabs.project.model.Product;
import com.kovanlabs.project.model.Recipe;
import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {

    private Product product;
    private List<RecipeDTO> recipes;
}