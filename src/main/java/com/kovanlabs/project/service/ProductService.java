package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.*;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientsRepository ingredientsRepository;

    public ProductService(ProductRepository productRepository,
                          RecipeRepository recipeRepository ,
                          IngredientsRepository ingredientsRepository) {
        this.productRepository = productRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientsRepository = ingredientsRepository;
    }


    public String createProduct(ProductDTO productDTO) {
//      Specifying Product ID to specify maping the product with recipes
        logger.info("Creating a product with recipe ");
        Product product = productRepository.save(productDTO.getProduct());

        List<Recipe> recipeList = new ArrayList<>();

        for (RecipeDTO recipeDTO : productDTO.getRecipes()) {
            Ingredients ingredients = ingredientsRepository.findByIngredientsNameIgnoreCase(recipeDTO.getIngredientName())
                    .orElseGet(() -> {
                        Ingredients newIngredients = new Ingredients();
                        newIngredients.setIngredientsName(recipeDTO.getIngredientName());
                        return ingredientsRepository.save(newIngredients);
                    });

            Recipe recipe = new Recipe();
            recipe.setProduct(product);
            recipe.setIngredientName(ingredients.getIngredientsName());
            recipe.setQuantity(recipeDTO.getQuantity());

            recipeList.add(recipe);
        }
        recipeRepository.saveAll(recipeList);
        return "Product Created Successfully";
    }
}