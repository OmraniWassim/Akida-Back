package com.akida.ecommerce.repository;

import com.akida.ecommerce.models.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllRootCategories();

    @EntityGraph(attributePaths = {"children", "image"})
    Optional<Category> findWithChildrenById(Long id);
}

