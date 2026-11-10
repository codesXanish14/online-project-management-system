package com.oms.core.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String sku,
    String barcode,
    String description,
    BigDecimal price,
    BigDecimal gst,
    BigDecimal discount,
    Integer stock,
    String status,
    Long categoryId,
    String categoryName,
    List<String> imageUrls
) {}