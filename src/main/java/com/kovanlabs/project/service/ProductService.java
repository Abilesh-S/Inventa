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
    private final BusinessRepository businessRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final BranchRepository branchRepository;

    public ProductService(ProductRepository productRepository,
                          RecipeRepository recipeRepository,
                          BusinessRepository businessRepository,
                          BranchInventoryRepository branchInventoryRepository,
                          BranchRepository branchRepository) {
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
        this.businessRepository = businessRepository;
        this.branchInventoryRepository = branchInventoryRepository;
        this.branchRepository = branchRepository;
    }
    public List<Product> getAllProducts(Long businessId) {
        return productRepository.findByBusinessId(businessId);
    }

    /**
     * Returns products scoped to the given branch PLUS business-wide products (branch = null).
     * Falls back to all business products if no branchId is provided.
     */
    public List<Product> getProductsAvailableForBranch(Long businessId, Long branchId) {
        if (branchId == null) return productRepository.findByBusinessId(businessId);
        // Branch-specific products for this branch
        List<Product> branchProducts = productRepository.findByBranchIdAndBusinessId(branchId, businessId);
        // Business-wide products (no branch assigned)
        List<Product> businessWideProducts = productRepository.findByBranchIsNullAndBusinessId(businessId);
        // Merge — branch-specific first, then business-wide
        List<Product> merged = new java.util.ArrayList<>(branchProducts);
        merged.addAll(businessWideProducts);
        return merged;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product createProduct(ProductDTO dto, Long businessId) {
        logger.info("Saving product: name={}, category={}, price={}",
                dto.getName(), dto.getCategory(), dto.getPrice());

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Product name is required");
        }

        Product p = null;

        // 1. Attempt lookup by ID
        if (dto.getId() != null && dto.getId() > 0) {
            Product candidate = productRepository.findById(dto.getId()).orElse(null);
            if (candidate != null && candidate.getBusiness() != null && businessId.equals(candidate.getBusiness().getId())) {
                p = candidate;
            }
        }

        // 2. Name-based fallback — only when NOT creating a branch-specific product
        //    (branch products can share names across branches, so name lookup would wrongly
        //     match a product from a different branch)
        if (p == null && dto.getBranchId() == null) {
            p = productRepository.findByNameIgnoreCaseAndBusinessId(dto.getName().trim(), businessId).orElse(null);
        }

        if (p == null) {
            logger.info("No existing product found. Creating new Product entity.");
            p = new Product();
            Business business = businessRepository.findById(businessId)
                    .orElseThrow(() -> new RuntimeException("Business not found"));
            p.setBusiness(business);
        } else {
            logger.info("Found existing product: {} (ID: {}). Updating...", p.getName(), p.getId());
        }

        p.setName(dto.getName());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory());
        p.setDescription(dto.getDescription());
        p.setInstructions(dto.getInstructions());
        p.setImageUrl(dto.getImageUrl());

        // Set branch if provided
        if (dto.getBranchId() != null) {
            branchRepository.findById(dto.getBranchId()).ifPresent(p::setBranch);
        }

        // ✅ Handle Recipes (Synchronize using managed list + orphanRemoval)
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
                recipe.setIngredientId(rDto.getIngredientId()); // Set the ID
                p.getRecipes().add(recipe); // Add to the list
            }
        }

        p = productRepository.save(p); // Saves both product and its recipes

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