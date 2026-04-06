package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;

    public ProductService(ProductRepository productRepository,
                          RecipeRepository recipeRepository) {
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
    }
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product createProduct(ProductDTO dto) {
        logger.info("Saving product: name={}, category={}, price={}",
                dto.getName(), dto.getCategory(), dto.getPrice());

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Product name is required");
        }

        Product p = null;

        if (dto.getId() > 0) {
            p = productRepository.findById(dto.getId()).orElse(null);
        }

        // 2. Attempt lookup by Name (Fallback for new entries or ID mismatches)
        if (p == null) {
            p = productRepository.findByNameIgnoreCase(dto.getName().trim()).orElse(null);
        }

        if (p == null) {
            logger.info("No existing product found. Creating new Product entity.");
            p = new Product();
        } else {
            logger.info("Found existing product: {} (ID: {}). Updating...", p.getName(), p.getId());
        }

        p.setName(dto.getName());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory());
        p.setDescription(dto.getDescription());
        p.setInstructions(dto.getInstructions());
        p.setImageUrl(dto.getImageUrl());

        if (p.getRecipes() == null) {
            p.setRecipes(new java.util.ArrayList<>());
        } else {
            p.getRecipes().clear();
        }

        if (dto.getRecipes() != null) {
            for (RecipeDTO rDto : dto.getRecipes()) {
                Recipe recipe = new Recipe();
                recipe.setProduct(p);
                recipe.setIngredientName(rDto.getIngredientName().trim().toLowerCase());
                recipe.setQuantity(rDto.getQuantity());
                recipe.setUnit(rDto.getUnit());
                recipe.setIngredientId(rDto.getIngredientId());
                p.getRecipes().add(recipe);
            }
        }

        p = productRepository.save(p);

        logger.info("Product saved successfully: productId={}", p.getId());
        return p;
    }

    public Recipe addRecipe(RecipeDTO dto) {
        logger.info("Adding recipe: productId={}, ingredient={}, quantity={}, unit={}",
                dto.getProductId(), dto.getIngredientName(), dto.getQuantity(), dto.getUnit());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product not found for recipe: productId={}", dto.getProductId());
                    return new RuntimeException("Product not found");
                });
        if (dto.getIngredientName() == null || dto.getIngredientName().trim().isEmpty()) {
            logger.warn("Invalid ingredient name for productId={}", dto.getProductId());
            throw new RuntimeException("Ingredient name is required");
        }

        Recipe recipe = new Recipe();
        recipe.setProduct(product);
        recipe.setIngredientName(dto.getIngredientName().trim().toLowerCase());
        recipe.setQuantity(dto.getQuantity());
        recipe.setUnit(dto.getUnit());

        recipe= recipeRepository.save(recipe);
        logger.info("Recipe added successfully: recipeId={}, productId={}",
                recipe.getId(), product.getId());
        return recipe;
    }

    public List<Recipe> getRecipes(Long productId) {
        logger.info("Fetching recipes for productId={}", productId);
        List<Recipe> recipes = recipeRepository.findByProductId(productId);
        logger.debug("Recipes fetched: count={}, productId={}", recipes.size(), productId);
        return recipes;
    }
}