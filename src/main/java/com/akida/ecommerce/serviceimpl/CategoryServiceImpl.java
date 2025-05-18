package com.akida.ecommerce.serviceimpl;

import com.akida.ecommerce.DTO.CategoryHierarchyDto;
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
        category.setChildren(new ArrayList<>());
        category.setImage(null);

        // If parent ID is set, fetch and attach it
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (savedCategory.getImage() != null) {
                storageService.deleteImage(savedCategory.getImage());
            }

            Image image = storageService.storeCategoryImage(imageFile, savedCategory);
            image.setCategory(savedCategory);
            savedCategory.setImage(image);
            categoryRepository.save(savedCategory);
        }

        return savedCategory;
    }

    @Transactional
    @Override
    public Category update(Long categoryId, Category updatedCategory, MultipartFile imageFile) throws IOException {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        // Check for name conflict
        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new EntityExistsException("Category name '" + updatedCategory.getName() + "' already exists");
        }

        // Update basic fields
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        //  Handle parent update
        if (updatedCategory.getParent() != null && updatedCategory.getParent().getId() != null) {
            // Prevent category being its own parent
            if (updatedCategory.getParent().getId().equals(existingCategory.getId())) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }

            Category newParent = categoryRepository.findById(updatedCategory.getParent().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            existingCategory.setParent(newParent);
        } else {
            existingCategory.setParent(null); // Unlink parent if null
        }

        // Handle image update
        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingCategory.getImage() != null) {
                storageService.deleteImage(existingCategory.getImage());
                existingCategory.setImage(null);
            }

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

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
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

    @Override
    public List<CategoryHierarchyDto> getFullHierarchy() {
        List<Category> rootCategories = categoryRepository.findAllRootCategories();
        return mapCategoriesToDto(rootCategories);
    }

    private List<CategoryHierarchyDto> mapCategoriesToDto(List<Category> categories) {
        return categories.stream()
                .map(this::convertToHierarchyDto)
                .collect(Collectors.toList());
    }

    private CategoryHierarchyDto convertToHierarchyDto(Category category) {
        CategoryHierarchyDto dto = new CategoryHierarchyDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        if (category.getImage() != null) {
            dto.setImagePath(category.getImage().getThumbnailPath());
        }

        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(mapCategoriesToDto(category.getChildren()));
        } else {
            dto.setChildren(new ArrayList<>());
        }

        return dto;
    }

}
