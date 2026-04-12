package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.service.ProductService;
import com.kovanlabs.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    private final ProductService service;
    private final UserService userService;

    public ProductController(ProductService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping
    public List<Product> getAllProducts(Authentication authentication) {
        System.out.println("--- PRODUCT API HIT ---");
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) throw new RuntimeException("Invalid user context");
        return service.getAllProducts(user.getBusiness().getId());
    }

    @CrossOrigin("http://localhost:5173")
    @GetMapping("/available")
    public List<Product> getAvailableForBranch(
            @RequestParam Long branchId,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) throw new RuntimeException("Invalid user context");
        return service.getProductsAvailableForBranch(user.getBusiness().getId(), branchId);
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
    public Product createProduct(@RequestBody ProductDTO dto, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        if (user == null || user.getBusiness() == null) throw new RuntimeException("Invalid user context");
        return service.createProduct(dto, user.getBusiness().getId());
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