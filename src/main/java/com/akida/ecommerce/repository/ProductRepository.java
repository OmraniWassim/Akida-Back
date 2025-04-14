package com.akida.ecommerce.repository;

import com.akida.ecommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByReference(String reference);
}