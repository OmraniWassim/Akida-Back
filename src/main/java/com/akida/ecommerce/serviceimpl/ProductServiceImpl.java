package com.akida.ecommerce.serviceimpl;

import com.akida.ecommerce.exceptions.EntityExistsException;
import com.akida.ecommerce.exceptions.EntityNotFoundException;
import com.akida.ecommerce.models.Category;
import com.akida.ecommerce.models.Image;
import com.akida.ecommerce.models.Product;
import com.akida.ecommerce.repository.CategoryRepository;
import com.akida.ecommerce.repository.ProductRepository;
import com.akida.ecommerce.services.ProductService;
import com.akida.ecommerce.services.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl  implements ProductService {

    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Product createProduct(Product product, List<MultipartFile> imageFiles) throws IOException {
        // Validate input
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        // Check if product with same ref already exists
        if (productRepository.existsByReference(product.getName())) {
            throw new EntityExistsException("Product with reference '" + product.getReference() + "' already exists");
        }

        // Initialize collections
        product.setImages(new ArrayList<>());

        // Verify category exists
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            product.setCategory(category);
        }

        // Save product first (without images)
        Product savedProduct = productRepository.save(product);

        // Handle image uploads
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    Image image = storageService.storeProductImage(file, savedProduct);
                    savedProduct.getImages().add(image);
                }
            }
            productRepository.save(savedProduct); // Update with images
        }

        return savedProduct;
    }

    @Transactional
    @Override
    public Product updateProduct(Long productId, Product updatedProduct, List<MultipartFile> imageFiles, List<Long> deletedImageIds) throws IOException {
        // 1. Find existing product with images
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        if (!existingProduct.getReference().equals(updatedProduct.getReference()) &&
                productRepository.existsByReference(updatedProduct.getReference())) {
            throw new EntityExistsException("Product reference '" + updatedProduct.getReference() + "' already exists");
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setReference(updatedProduct.getReference());
        existingProduct.setDiscount(updatedProduct.getDiscount());
        existingProduct.setInventoryStatus(updatedProduct.getInventoryStatus());



        if (updatedProduct.getCategory() != null ) {
            Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Iterator<Image> iterator = existingProduct.getImages().iterator();
        while (iterator.hasNext()) {
            Image image = iterator.next();
            if (deletedImageIds.contains(image.getId())) {
                storageService.deleteImage(image);
                iterator.remove();
            }
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    Image newImage = storageService.storeProductImage(file, existingProduct);
                    existingProduct.getImages().add(newImage);
                }
            }
        }

        return productRepository.save(existingProduct);
    }
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }


}
