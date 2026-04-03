package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.RecipeDTO;
import com.kovanlabs.project.model.Product;
import com.kovanlabs.project.model.Recipe;
import com.kovanlabs.project.repository.ProductRepository;
import com.kovanlabs.project.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RecipeService {

    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         ProductRepository productRepository) {
        this.recipeRepository = recipeRepository;
        this.productRepository = productRepository;
    }


    public Recipe addRecipe(RecipeDTO dto) {
        logger.info("Adding recipe: productId={}, ingredient={}, quantity={}, unit={}",
                dto.getProductId(), dto.getIngredientName(), dto.getQuantity(), dto.getUnit());
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    logger.error("Product not found: productId={}", dto.getProductId());
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
        recipe=recipeRepository.save(recipe);

        logger.info("Recipe created successfully: recipeId={}, productId={}",
                recipe.getId(), product.getId());

        return recipe;
    }


    public List<Recipe> getRecipesByProduct(Long productId) {
        return recipeRepository.findByProductId(productId);
    }

    public Recipe updateRecipe(Long id, RecipeDTO dto) {
        logger.info("Updating recipe: recipeId={}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Recipe not found: recipeId={}", id);
                    return new RuntimeException("Recipe not found");
                });

        recipe.setIngredientName(dto.getIngredientName());
        recipe.setQuantity(dto.getQuantity());
        recipe.setUnit(dto.getUnit());
        recipe = recipeRepository.save(recipe);

        logger.info("Recipe updated successfully: recipeId={}", recipe.getId());
        return recipe;
    }

    public String deleteRecipe(Long id) {
        logger.info("Deleting recipe: recipeId={}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Recipe not found: recipeId={}", id);
                    return new RuntimeException("Recipe not found");
                });

        recipeRepository.delete(recipe);

        logger.warn("Recipe deleted: recipeId={}", id);

        return "Recipe deleted successfully";
    }
}