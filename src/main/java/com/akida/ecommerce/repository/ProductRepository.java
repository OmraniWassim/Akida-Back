package com.akida.ecommerce.repository;

import com.akida.ecommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_Id(Long id);

    boolean existsByReference(String reference);
}