package com.pulsepass.controller;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de tickets (PRD Fase 3, secciones 7.5 y 8.5).
 *
 * <p>El prefijo comun es {@code /api} y no {@code /api/tickets} porque el contrato
 * del PRD ubica la consulta de tickets pagados bajo el recurso evento
 * ({@code GET /api/events/{eventCode}/tickets/paid}) y la sirve este controller,
 * que es quien depende de {@link TicketService} (NFR-CTRL-004).
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * FR-CTRL-TKT-001: 201 Created. Edad minima, capacidad, usuario activo y estado del
     * evento son reglas del Service; el Controller solo traduce su resultado (404 / 409).
     */
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody PurchaseTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.purchase(request));
    }

    /** FR-CTRL-TKT-002: 200 o 404. */
    @GetMapping("/tickets/{ticketCode}")
    public ResponseEntity<TicketResponse> findByCode(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.findByCode(ticketCode));
    }

    /** FR-CTRL-TKT-003: tickets de un usuario identificado por email. */
    @GetMapping("/tickets/by-user")
    public ResponseEntity<List<TicketResponse>> findByUserEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(ticketService.findByUserEmail(email));
    }

    /** FR-CTRL-TKT-004: tickets PAID de un evento. */
    @GetMapping("/events/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> findPaidTicketsByEvent(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(ticketService.findPaidTicketsByEvent(eventCode));
    }

    /** FR-CTRL-TKT-005: PAID pasa a CANCELLED (200); cualquier otra transicion responde 409. */
    @PatchMapping("/tickets/{ticketCode}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.cancel(ticketCode));
    }

    /** FR-CTRL-TKT-006: PAID pasa a USED (200); cualquier otra transicion responde 409. */
    @PatchMapping("/tickets/{ticketCode}/use")
    public ResponseEntity<TicketResponse> markAsUsed(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.markAsUsed(ticketCode));
    }
}
