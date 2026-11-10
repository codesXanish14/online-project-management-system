package com.oms.core.controller;

import com.oms.core.dto.request.ProductRequest;
import com.oms.core.dto.response.ProductResponse;
import com.oms.core.entity.Category;
import com.oms.core.entity.Product;
import com.oms.core.mapper.ProductMapper;
import com.oms.core.repository.CategoryRepository;
import com.oms.core.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductController(ProductRepository productRepository, 
                             CategoryRepository categoryRepository,
                             ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    // Public Endpoint: Customer storefront browsing active inventory items
    @GetMapping
    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByStatus("ACTIVE").stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Admin Endpoint: Fetch ALL products (including draft/archived/out of stock items) for management tables
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponse> getAllProductsForAdmin() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Public Endpoint: Get active items matching a specific category context
    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getProductsByCategory(@PathVariable Long categoryId) {
        return productRepository.findByCategoryIdAndStatus(categoryId, "ACTIVE").stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Public Endpoint: Fetch structural details for a single target product
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with id: " + id));
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    // Admin Endpoint: Register a product alongside an array of direct image URLs
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new RuntimeException("Error: Assigned Category not found"));

        Product product = productMapper.toEntity(productRequest);
        product.setCategory(category);

        if (productRequest.imageUrls() != null) {
            product.setImageUrls(productRequest.imageUrls());
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(productMapper.toResponse(savedProduct));
    }

    // Admin Endpoint: Alter inventory metrics, structural pricing, or alternate image URLs
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with id: " + id));

        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new RuntimeException("Error: Category not found"));

        product.setName(productRequest.name());
        product.setSku(productRequest.sku());
        product.setBarcode(productRequest.barcode());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        product.setGst(productRequest.gst());
        product.setDiscount(productRequest.discount());
        product.setStock(productRequest.stock());
        product.setCategory(category);

        if (productRequest.imageUrls() != null) {
            product.setImageUrls(productRequest.imageUrls());
        }

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(productMapper.toResponse(updatedProduct));
    }

    // Admin Endpoint: Permanent structural erasure of an inventory item by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with id: " + id));
        
        productRepository.delete(product);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully from inventory."));
    }
}