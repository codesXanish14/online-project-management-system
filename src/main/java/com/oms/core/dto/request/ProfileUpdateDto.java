package com.oms.core.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateDto(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String phoneNumber,
    @NotBlank String address
) {}