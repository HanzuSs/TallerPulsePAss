package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.service.VenueService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de venues (PRD Fase 3, secciones 7.1 y 8.1).
 *
 * <p>Solo traduce HTTP a llamadas de {@link VenueService}: sin reglas de negocio
 * (CTRL-001), sin Repository (CTRL-002) y sin entidades en la respuesta (CTRL-003).
 * Los errores los traduce {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /** FR-CTRL-VEN-001: 200 si existe, 404 si no. */
    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> findByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(venueService.findByCode(code));
    }

    /** FR-CTRL-VEN-002: 200 con la coleccion de venues activos. */
    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> findActiveVenues() {
        return ResponseEntity.ok(venueService.findActiveVenues());
    }
}
