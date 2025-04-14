package com.akida.ecommerce.serviceimpl;

import com.akida.ecommerce.exceptions.EntityExistsException;
import com.akida.ecommerce.exceptions.EntityNotFoundException;
import com.akida.ecommerce.models.Category;
import com.akida.ecommerce.models.Image;
import com.akida.ecommerce.repository.CategoryRepository;
import com.akida.ecommerce.services.CategoryService;
import com.akida.ecommerce.services.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    @Transactional
    @Override
    public Category create(Category category, MultipartFile imageFile) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (categoryRepository.existsByName(category.getName())) {
            throw new EntityExistsException("Category with name '" + category.getName() + "' already exists");
        }

        category.setProducts(new ArrayList<>());
        category.setImage(null);

        Category savedCategory = categoryRepository.save(category);

        // Handle image upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (savedCategory.getImage() != null) {
                storageService.deleteImage(savedCategory.getImage());
            }

            Image image = storageService.storeCategoryImage(imageFile,category);
            image.setCategory(savedCategory);
            savedCategory.setImage(image);
            categoryRepository.save(savedCategory); // Update with image
        }

        return savedCategory;
    }

    @Transactional
    @Override
    public Category update(Long categoryId, Category updatedCategory, MultipartFile imageFile) throws IOException {
        // Find existing category
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        // Check if name is being changed and if new name already exists
        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
                throw new EntityExistsException("Category name '" + updatedCategory.getName() + "' already exists");
            }


        // Update basic fields
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        // Handle image update
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (existingCategory.getImage() != null) {
                storageService.deleteImage(existingCategory.getImage());
                existingCategory.setImage(null);
            }

            // Store new image
            Image newImage = storageService.storeCategoryImage(imageFile, existingCategory);
            newImage.setCategory(existingCategory);
            existingCategory.setImage(newImage);
        }

        return categoryRepository.save(existingCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }

        // Delete the image file if exists
        if (category.getImage() != null) {
            storageService.deleteImage(category.getImage());
        }

        // Delete the category (image entity will be deleted due to orphanRemoval)
        categoryRepository.delete(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteCategories(List<Long> categoryIds) throws IOException {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // Validate all categories exist
        if (categories.size() != categoryIds.size()) {
            Set<Long> foundIds = categories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = categoryIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new EntityNotFoundException("Categories not found: " + missingIds);
        }

        // Check for categories with products
        List<Category> categoriesWithProducts = categories.stream()
                .filter(c -> !c.getProducts().isEmpty())
                .toList();

        if (!categoriesWithProducts.isEmpty()) {
            throw new IllegalStateException("Cannot delete categories with products: " +
                    categoriesWithProducts.stream()
                            .map(Category::getId)
                            .toList());
        }

        // Delete all images first
        for (Category category : categories) {
            if (category.getImage() != null) {
                storageService.deleteImage(category.getImage());
            }
        }

        // Bulk delete categories
        categoryRepository.deleteAllInBatch(categories);
    }

}
