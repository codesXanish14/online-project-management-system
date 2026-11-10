package com.oms.core.repository;

import com.oms.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status);
    List<Product> findByStatus(String status);
}