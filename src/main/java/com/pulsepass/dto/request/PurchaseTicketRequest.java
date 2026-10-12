package com.pulsepass.dto.request;

import com.pulsepass.domain.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body de POST /api/tickets (PRD Fase 3, seccion 11.3).
 *
 * <p>Usuario activo, evento publicado, edad minima y capacidad son reglas del Service.
 */
public record PurchaseTicketRequest(
        @NotBlank(message = "User email is required")
        @Email(message = "User email must be a valid address")
        @Size(max = 160, message = "User email must not exceed 160 characters")
        String userEmail,

        @NotBlank(message = "Event code is required")
        @Size(max = 50, message = "Event code must not exceed 50 characters")
        String eventCode,

        @NotNull(message = "Ticket type is required")
        TicketType type
) {}
