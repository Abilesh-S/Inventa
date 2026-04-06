package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByProductId(Long productId);

    @Transactional
    void deleteByProductId(Long productId);
}