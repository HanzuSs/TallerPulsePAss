package com.pulsepass.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contrato uniforme de error de la API (PRD Fase 3, seccion 13, NFR-CTRL-003).
 *
 * <p>Todo error HTTP que produce la aplicacion usa esta estructura:
 * <ul>
 *   <li>{@code timestamp}: momento en que se genero el error.</li>
 *   <li>{@code status}: codigo HTTP numerico (400, 404, 409, 500...).</li>
 *   <li>{@code error}: frase estandar del codigo ("Not Found", "Conflict"...).</li>
 *   <li>{@code message}: descripcion legible, sin detalles internos (NFR-CTRL-007).</li>
 *   <li>{@code details}: un par campo/problema en errores de validacion; vacio en el resto.</li>
 * </ul>
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {}
