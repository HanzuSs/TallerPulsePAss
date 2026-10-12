package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de artistas (PRD Fase 3, secciones 7.3 y 8.3).
 */
@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    /** FR-CTRL-ART-001: 200 si existe, 404 si no. Un id no numerico responde 400. */
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }

    /** FR-CTRL-ART-002: busqueda por nombre artistico; 200 o 404. */
    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> findByStageName(@RequestParam("stageName") String stageName) {
        return ResponseEntity.ok(artistService.findByStageName(stageName));
    }

    /** FR-CTRL-ART-003: artistas activos. */
    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> findActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }
}
