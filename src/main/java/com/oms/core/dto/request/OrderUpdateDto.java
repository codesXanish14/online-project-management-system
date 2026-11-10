package com.oms.core.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderUpdateDto(
    @NotBlank String customerFullName,
    @NotBlank String phoneNumber,
    @NotBlank String shippingAddress
) {}