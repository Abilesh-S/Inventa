package com.kovanlabs.project.dto;

import com.kovanlabs.project.model.Product;
import com.kovanlabs.project.model.Recipe;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NewProductDTO {
    private Product product;
    private Recipe recipe;

    public NewProductDTO(Product product, Recipe recipe) {
        this.product = product;
        this.recipe = recipe;
    }
}
