package com.oms.core.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
    @NotBlank(message = "Product name cannot be blank")
    String name,
    
    @NotBlank(message = "SKU is required")
    String sku,
    
    String barcode,
    String description,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    BigDecimal price,
    
    BigDecimal gst,
    BigDecimal discount,
    
    @NotNull(message = "Stock level is required")
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock,
    
    @NotNull(message = "Category ID is required")
    Long categoryId,
    
    List<String> imageUrls
) {}