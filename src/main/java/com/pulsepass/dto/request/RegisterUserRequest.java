package com.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Body de POST /api/users (PRD Fase 3, seccion 11.2).
 *
 * <p>Username o email duplicados y fecha de nacimiento futura son reglas del Service.
 * Las longitudes maximas coinciden con las columnas de V1__create_schema.sql.
 */
public record RegisterUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 60, message = "Username must not exceed 60 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 160, message = "Email must not exceed 160 characters")
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate
) {}
