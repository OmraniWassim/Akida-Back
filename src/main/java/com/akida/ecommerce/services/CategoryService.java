package com.akida.ecommerce.services;

import com.akida.ecommerce.models.Category;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public  interface CategoryService {

    @Transactional
    Category create(Category category, MultipartFile imageFile) throws IOException;


    @Transactional
    Category update(Long categoryId, Category updatedCategory, MultipartFile imageFile) throws IOException;

    @Transactional
    void deleteCategory(Long categoryId) throws IOException;

    List<Category> getAllCategories();

    @Transactional
    void deleteCategories(List<Long> categoryIds) throws IOException;
}
