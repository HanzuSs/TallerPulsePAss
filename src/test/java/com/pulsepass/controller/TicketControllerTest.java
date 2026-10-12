package com.pulsepass.controller;

import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.TicketService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pulsepass.support.ControllerTestData.purchaseTicketJson;
import static com.pulsepass.support.ControllerTestData.ticket;
import static com.pulsepass.support.ErrorResponseAssertions.assertErrorResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de TicketController (PRD Fase 3: FR-CTRL-TKT-001..006,
 * TEST-CTRL-TKT-001..011, AC-CTRL009..014).
 *
 * Edad minima, capacidad, usuario activo y transiciones de estado son reglas del
 * Service: aqui el mock lanza la excepcion y se comprueba que el Controller la
 * traduzca al codigo HTTP correcto, sin reimplementar la regla (CTRL-001).
 */
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    // ------------------------------------------------------------------
    // POST /api/tickets
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-001 / AC-CTRL009: compra valida responde 201 con TicketResponse PAID")
    void purchaseValidTicketReturns201() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-0001"))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.userEmail").value("andrea@example.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        // El JSON del cliente llega al Service convertido en PurchaseTicketRequest.
        ArgumentCaptor<PurchaseTicketRequest> captor = ArgumentCaptor.forClass(PurchaseTicketRequest.class);
        verify(ticketService).purchase(captor.capture());
        PurchaseTicketRequest sent = captor.getValue();
        assertThat(sent.userEmail()).isEqualTo("andrea@example.com");
        assertThat(sent.eventCode()).isEqualTo("CMF-2026");
        assertThat(sent.type()).hasToString("GENERAL");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-002: request invalido responde 400 con details y no invoca el Service")
    void purchaseInvalidRequestReturns400() throws Exception {
        String invalidBody = """
                {
                  "userEmail": "no-es-un-email",
                  "eventCode": "",
                  "type": null
                }
                """;

        assertErrorResponse(
                mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.userEmail").value("User email must be a valid address"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("QT-CTRL-010: un body vacio reporta cada campo obligatorio en details")
    void purchaseEmptyBodyReportsEveryRequiredField() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.userEmail").value("User email is required"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("PRD 11.4: un tipo de ticket que no existe en el enum responde 400 y no invoca el Service")
    void purchaseUnknownTicketTypeReturns400() throws Exception {
        String body = purchaseTicketJson().replace("\"type\": \"GENERAL\"", "\"type\": \"PLATINUM\"");

        assertErrorResponse(
                mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Malformed JSON request");

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-003: usuario inexistente responde 404 con ErrorResponse")
    void purchaseUnknownUserReturns404() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@example.com"));

        assertErrorResponse(
                mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson())),
                404, "Not Found", "User not found: andrea@example.com");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-004 / AC-CTRL010: regla de negocio (edad minima) responde 409 con el mensaje del Service")
    void purchaseBusinessRuleViolationReturns409() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new BusinessRuleException("User does not meet minimum age."));

        assertErrorResponse(
                mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseTicketJson())),
                409, "Conflict", "User does not meet minimum age.")
                .andExpect(jsonPath("$.details").isEmpty());
    }

    // ------------------------------------------------------------------
    // GET /api/tickets/{ticketCode}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-005: consultar un ticket existente responde 200 con TicketResponse")
    void findByCodeReturns200() throws Exception {
        when(ticketService.findByCode("TKT-0001")).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TKT-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-0001"))
                .andExpect(jsonPath("$.eventName").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(ticketService).findByCode("TKT-0001");
    }

    @Test
    @DisplayName("FR-CTRL-TKT-002: ticket inexistente responde 404 con ErrorResponse")
    void findByCodeReturns404WhenNotFound() throws Exception {
        when(ticketService.findByCode("TKT-9999"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: TKT-9999"));

        assertErrorResponse(
                mockMvc.perform(get("/api/tickets/{ticketCode}", "TKT-9999")),
                404, "Not Found", "Ticket not found: TKT-9999");
    }

    // ------------------------------------------------------------------
    // GET /api/tickets/by-user?email=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-006 / UC-CTRL-08: tickets de un usuario responde 200 con la coleccion")
    void findByUserEmailReturns200() throws Exception {
        when(ticketService.findByUserEmail("andrea@example.com"))
                .thenReturn(List.of(ticket("TKT-0002", TicketStatus.CANCELLED), ticket("TKT-0001", TicketStatus.PAID)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticketCode").value("TKT-0002"))
                .andExpect(jsonPath("$[1].ticketCode").value("TKT-0001"));

        verify(ticketService).findByUserEmail("andrea@example.com");
        // /by-user es una ruta literal: no debe interpretarse como {ticketCode}.
        verify(ticketService, never()).findByCode(any());
    }

    @Test
    @DisplayName("Falta el parametro email: responde 400 con ErrorResponse y no invoca el Service")
    void findByUserEmailWithoutParameterReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(get("/api/tickets/by-user")),
                400, "Bad Request", "Missing required parameter: email")
                .andExpect(jsonPath("$.details.email").value("Parameter is required"));

        verifyNoInteractions(ticketService);
    }

    // ------------------------------------------------------------------
    // GET /api/events/{eventCode}/tickets/paid
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-007: tickets PAID de un evento responde 200 solo con tickets pagados")
    void findPaidTicketsByEventReturns200() throws Exception {
        when(ticketService.findPaidTicketsByEvent("CMF-2026"))
                .thenReturn(List.of(ticket("TKT-0001", TicketStatus.PAID), ticket("TKT-0003", TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].status").value("PAID"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    // ------------------------------------------------------------------
    // PATCH /api/tickets/{ticketCode}/cancel
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-008 / AC-CTRL011: cancelar un ticket PAID responde 200 con status CANCELLED")
    void cancelValidTicketReturns200() throws Exception {
        when(ticketService.cancel("TKT-0001")).thenReturn(ticket(TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-0001"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TKT-0001");
        verify(ticketService, never()).markAsUsed(any());
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-009 / AC-CTRL012: cancelar un ticket USED responde 409")
    void cancelInvalidTransitionReturns409() throws Exception {
        when(ticketService.cancel("TKT-0001"))
                .thenThrow(new BusinessRuleException("Only PAID tickets can be cancelled."));

        assertErrorResponse(
                mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-0001")),
                409, "Conflict", "Only PAID tickets can be cancelled.");
    }

    @Test
    @DisplayName("Cancelar un ticket inexistente responde 404")
    void cancelUnknownTicketReturns404() throws Exception {
        when(ticketService.cancel("TKT-9999"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: TKT-9999"));

        assertErrorResponse(
                mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-9999")),
                404, "Not Found", "Ticket not found: TKT-9999");
    }

    // ------------------------------------------------------------------
    // PATCH /api/tickets/{ticketCode}/use
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-010 / AC-CTRL013 / UC-CTRL-10: usar un ticket PAID responde 200 con status USED")
    void useValidTicketReturns200() throws Exception {
        when(ticketService.markAsUsed("TKT-0001")).thenReturn(ticket(TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-0001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-0001"))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TKT-0001");
        verify(ticketService, never()).cancel(any());
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-011 / AC-CTRL014: usar un ticket CANCELLED responde 409")
    void useInvalidTransitionReturns409() throws Exception {
        when(ticketService.markAsUsed("TKT-0001"))
                .thenThrow(new BusinessRuleException("Only PAID tickets can be marked as used."));

        assertErrorResponse(
                mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-0001")),
                409, "Conflict", "Only PAID tickets can be marked as used.");
    }

    @Test
    @DisplayName("Usar un ticket inexistente responde 404")
    void useUnknownTicketReturns404() throws Exception {
        when(ticketService.markAsUsed("TKT-9999"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: TKT-9999"));

        assertErrorResponse(
                mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-9999")),
                404, "Not Found", "Ticket not found: TKT-9999");
    }
}
