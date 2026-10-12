package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.EventCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Body de POST /api/events (PRD Fase 3, seccion 11.1).
 *
 * <p>Solo validacion estructural (obligatorio, longitud, minimo). Que la fecha sea futura,
 * que el venue exista y este activo o que el codigo no se repita son reglas del Service.
 * Las longitudes maximas coinciden con las columnas de V1__create_schema.sql.
 */
public record CreateEventRequest(
        @NotBlank(message = "Event code is required")
        @Size(max = 50, message = "Event code must not exceed 50 characters")
        String eventCode,

        @NotBlank(message = "Event name is required")
        @Size(max = 150, message = "Event name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Category is required")
        EventCategory category,

        @NotNull(message = "Event date is required")
        LocalDateTime eventDate,

        @NotNull(message = "Minimum age is required")
        @Min(value = 0, message = "Minimum age must be greater than or equal to 0")
        Integer minimumAge,

        @NotBlank(message = "Venue code is required")
        @Size(max = 50, message = "Venue code must not exceed 50 characters")
        String venueCode
) {}
