package com.oms.core.controller;

import com.oms.core.dto.request.ProfileUpdateDto;
import com.oms.core.entity.User;
import com.oms.core.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Fetch personal details for an explicitly target User ID block
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('DELIVERY')") // Added DELIVERY support
    public ResponseEntity<?> getUserProfileData(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User account record missing."));
        return ResponseEntity.ok(user);
    }

    // Overwrite metadata, phone details, and home address fields securely
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('DELIVERY')") // Added DELIVERY support
    public ResponseEntity<?> updatePersonalDetails(@PathVariable Long userId, @Valid @RequestBody ProfileUpdateDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: Target User account not found."));

        user.setFirstName(profileDto.firstName());
        user.setLastName(profileDto.lastName());
        user.setPhoneNumber(profileDto.phoneNumber());
        user.setAddress(profileDto.address());

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
}