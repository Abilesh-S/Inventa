package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping
    public List<Product> getAllProducts() {
        System.out.println("--- PRODUCT API HIT ---");
        return service.getAllProducts();
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return service.getProductById(id);
    }

    @CrossOrigin("http://localhost:5173")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
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