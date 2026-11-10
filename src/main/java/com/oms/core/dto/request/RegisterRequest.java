package com.oms.core.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RegisterRequest(
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    String username,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 50)
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    String password,

    Set<String> roles
) {}