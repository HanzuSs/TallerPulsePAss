package com.pulsepass.controller;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

@RestController
@RequestMapping("/api")
public class TicketController {
    private final TicketService ticketService;
    public TicketController(TicketService ticketService) { this.ticketService = ticketService; }
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody PurchaseTicketRequest request) {
        Authentication auth = currentAuthentication();
        ticketService.authorizePurchase(request.userEmail(), principalId(auth), roles(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.purchase(request));
    }
    @GetMapping("/tickets/{ticketCode}")
    public ResponseEntity<TicketResponse> findByCode(@PathVariable("ticketCode") String ticketCode) {
        Authentication auth = currentAuthentication();
        ticketService.authorizeTicketRead(ticketCode, principalId(auth), roles(auth));
        return ResponseEntity.ok(ticketService.findByCode(ticketCode));
    }
    @GetMapping("/tickets/by-user")
    public ResponseEntity<List<TicketResponse>> findByUserEmail(@RequestParam("email") String email) {
        Authentication auth = currentAuthentication();
        ticketService.authorizeUserTicketList(email, principalId(auth), roles(auth));
        return ResponseEntity.ok(ticketService.findByUserEmail(email));
    }
    @GetMapping("/events/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> findPaidTicketsByEvent(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(ticketService.findPaidTicketsByEvent(eventCode));
    }
    @PatchMapping("/tickets/{ticketCode}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable("ticketCode") String ticketCode) {
        Authentication auth = currentAuthentication();
        ticketService.authorizeTicketCancel(ticketCode, principalId(auth), roles(auth));
        return ResponseEntity.ok(ticketService.cancel(ticketCode));
    }
    @PatchMapping("/tickets/{ticketCode}/use")
    public ResponseEntity<TicketResponse> markAsUsed(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.markAsUsed(ticketCode));
    }
    private Authentication currentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) throw new AccessDeniedException("Authentication required");
        return auth;
    }
    private Long principalId(Authentication auth) { try { return Long.valueOf(auth.getName()); } catch (NumberFormatException ex) { throw new AccessDeniedException("Invalid principal"); } }
    private Set<String> roles(Authentication auth) { return auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toUnmodifiableSet()); }
}
