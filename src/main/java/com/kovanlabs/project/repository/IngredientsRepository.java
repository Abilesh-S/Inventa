package com.kovanlabs.project.repository;

import com.kovanlabs.project.model.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientsRepository extends JpaRepository<Ingredients, Long> {
    public Optional<Ingredients> findByIngredientsNameIgnoreCase(String name);
}
