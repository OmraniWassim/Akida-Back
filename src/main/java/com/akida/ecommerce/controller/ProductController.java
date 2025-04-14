package com.akida.ecommerce.controller;

import com.akida.ecommerce.models.Product;
import com.akida.ecommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
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

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }
}
