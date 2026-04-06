package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.RecipeDTO;
import com.kovanlabs.project.model.Recipe;
import com.kovanlabs.project.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public Recipe addRecipe(@RequestBody RecipeDTO dto) {
        return recipeService.addRecipe(dto);
    }


    @GetMapping("/product/{productId}")
    public List<Recipe> getRecipes(@PathVariable Long productId) {
        return recipeService.getRecipesByProduct(productId);
    }

    @PutMapping("/{id}")
    public Recipe update(@PathVariable Long id,
                         @RequestBody RecipeDTO dto) {
        return recipeService.updateRecipe(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return recipeService.deleteRecipe(id);
    }
}