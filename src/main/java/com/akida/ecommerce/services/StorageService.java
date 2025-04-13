package com.akida.ecommerce.services;

import com.akida.ecommerce.models.Category;
import com.akida.ecommerce.models.Image;
import com.akida.ecommerce.models.Product;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface StorageService {
    Image storeProductImage(MultipartFile file, Product product) throws IOException;
    Image storeCategoryImage(MultipartFile file, Category category) throws IOException;
    Resource loadAsResource(String filePath) throws FileNotFoundException;
    void deleteImage(Image image) throws IOException;
    String getImageUrl(String filePath);
}