package com.akida.ecommerce.services;

import com.akida.ecommerce.models.Product;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    @Transactional
    Product createProduct(Product product, List<MultipartFile> imageFiles) throws IOException;

    List<Product> getAllProducts();
}
