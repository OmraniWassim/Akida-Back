package com.akida.ecommerce.controller;

import com.akida.ecommerce.models.Product;
import com.akida.ecommerce.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/secured/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") @Valid Product product,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) throws IOException {


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product, imageFiles));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") @Valid Product product,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestPart(value = "deletedImageIds", required = false) String deletedImageIdsJson
    ) throws IOException {
        List<Long> deletedIds = new ArrayList<>();
        if (deletedImageIdsJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            deletedIds = Arrays.asList(mapper.readValue(deletedImageIdsJson, Long[].class));
        }

        return ResponseEntity.ok(productService.updateProduct(id, product, imageFiles, deletedIds));
    }


    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long id) {
        return new ResponseEntity<>(productService.getProductsByCategory(id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
