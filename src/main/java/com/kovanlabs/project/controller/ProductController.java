package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductDTO dto) {
        return service.createProduct(dto);
    }

    @PostMapping("/recipe")
    public Recipe addRecipe(@RequestBody RecipeDTO dto) {
        return service.addRecipe(dto);
    }

    @GetMapping("/{productId}/recipes")
    public List<Recipe> getRecipes(@PathVariable Long productId) {
        return service.getRecipes(productId);
    }
}