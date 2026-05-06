package com.incidents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTOs for authentication requests and responses.
 * DTOs (Data Transfer Objects) shield internal entities from direct exposure.
 *
 * Interview point: Why not just return the entity directly?
 * - Security: entities may have sensitive fields (hashed password, etc.)
 * - Flexibility: API shape can differ from DB schema
 * - Versioning: you can change entity without breaking API
 */
public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private String message;
    }
}
