package com.akida.ecommerce.services;

import com.akida.ecommerce.models.Category;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public abstract class CategoryService {

    @Transactional
    public abstract Category create(Category category, MultipartFile imageFile) throws IOException;


    @Transactional
    public abstract Category update(Long categoryId, Category updatedCategory, MultipartFile imageFile) throws IOException;

    @Transactional
    public abstract void deleteCategory(Long categoryId) throws IOException;

    public abstract List<Category> getAllCategories();

    @Transactional
    public abstract void deleteCategories(List<Long> categoryIds) throws IOException;
}
