package com.pulsepass.controller;

import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.service.EventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de eventos (PRD Fase 3, secciones 7.2 y 8.2).
 *
 * <p>Las rutas literales ({@code /published}, {@code /by-artist}) tienen prioridad
 * sobre la plantilla {@code /{eventCode}}, por eso conviven sin ambiguedad.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** FR-CTRL-EVT-001: la validacion estructural corre antes de llegar al Service. 201 Created. */
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    /** FR-CTRL-EVT-002: 200 si existe, 404 si no. */
    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> findByCode(@PathVariable("eventCode") String eventCode) {
        EventResponse event = eventService.findByCode(eventCode);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean privileged = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
        boolean authenticated = auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String);
        if (event.status() != com.pulsepass.domain.enums.EventStatus.PUBLISHED && !privileged) {
            if (!authenticated) throw new org.springframework.security.access.AccessDeniedException("Event is not public");
            throw new org.springframework.security.access.AccessDeniedException("Event is not public");
        }
        return ResponseEntity.ok(event);
    }

    /** FR-CTRL-EVT-003: cartelera de eventos publicados. */
    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> findPublishedEvents() {
        return ResponseEntity.ok(eventService.findPublishedEvents());
    }

    /** FR-CTRL-EVT-004: 200 si la transicion es valida, 409 si el Service la rechaza. */
    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(eventService.publish(eventCode));
    }

    /** FR-CTRL-EVT-005: 200 si se asocia, 409 si hay conflicto (duplicado o evento cerrado). */
    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> addArtist(@PathVariable("eventCode") String eventCode,
                                                   @PathVariable("artistId") Long artistId) {
        return ResponseEntity.ok(eventService.addArtist(eventCode, artistId));
    }

    /** FR-CTRL-EVT-006: eventos en los que participa el artista indicado por nombre artistico. */
    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> findByArtist(@RequestParam("stageName") String stageName) {
        List<EventSummaryResponse> events = eventService.findByArtist(stageName);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean privileged = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
        if (privileged) return ResponseEntity.ok(events);
        return ResponseEntity.ok(events.stream().filter(e -> e.status() == com.pulsepass.domain.enums.EventStatus.PUBLISHED).toList());
    }
}
