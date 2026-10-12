package com.pulsepass.controller;

import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pulsepass.support.ControllerTestData.createEventJson;
import static com.pulsepass.support.ControllerTestData.draftEvent;
import static com.pulsepass.support.ControllerTestData.event;
import static com.pulsepass.support.ControllerTestData.publishedEvent;
import static com.pulsepass.support.ControllerTestData.publishedSummary;
import static com.pulsepass.support.ControllerTestData.solarBeat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de EventController (PRD Fase 3: FR-CTRL-EVT-001..006,
 * TEST-CTRL-EVT-001..009, AC-CTRL003..006).
 *
 * El Service es un mock: aqui solo se prueba la traduccion HTTP (status, JSON,
 * validacion de entrada y errores), no las reglas de negocio.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    // ------------------------------------------------------------------
    // POST /api/events
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-001 / AC-CTRL003: crear evento valido responde 201 con EventResponse")
    void createValidEventReturns201() throws Exception {
        when(eventService.create(any(CreateEventRequest.class))).thenReturn(draftEvent());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.category").value("MUSIC"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.minimumAge").value(18))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        // El JSON del cliente llega al Service convertido en CreateEventRequest.
        ArgumentCaptor<CreateEventRequest> captor = ArgumentCaptor.forClass(CreateEventRequest.class);
        verify(eventService).create(captor.capture());
        CreateEventRequest sent = captor.getValue();
        assertThat(sent.eventCode()).isEqualTo("CMF-2026");
        assertThat(sent.venueCode()).isEqualTo("VEN-SMR-01");
        assertThat(sent.minimumAge()).isEqualTo(18);
        assertThat(sent.eventDate()).isNotNull();
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-002 / AC-CTRL004: request invalido responde 400 con details y no invoca el Service")
    void createInvalidEventReturns400() throws Exception {
        String invalidBody = """
                {
                  "eventCode": "",
                  "name": "Caribbean Music Fest 2026",
                  "category": "MUSIC",
                  "eventDate": "2026-12-20T20:00:00",
                  "minimumAge": 18,
                  "venueCode": ""
                }
                """;

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"))
                .andExpect(jsonPath("$.details.name").doesNotExist());

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("QT-CTRL-010: un body vacio reporta cada campo obligatorio en details")
    void createEmptyBodyReportsEveryRequiredField() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.name").value("Event name is required"))
                .andExpect(jsonPath("$.details.category").value("Category is required"))
                .andExpect(jsonPath("$.details.eventDate").value("Event date is required"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age is required"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"))
                .andExpect(jsonPath("$.details.description").doesNotExist());

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("PRD 11.1: minimumAge negativo responde 400 y no invoca el Service")
    void createNegativeMinimumAgeReturns400() throws Exception {
        String body = createEventJson().replace("\"minimumAge\": 18", "\"minimumAge\": -1");

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.minimumAge")
                        .value("Minimum age must be greater than or equal to 0"));

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("PRD 11.1: description mayor al maximo (1000) responde 400 y no invoca el Service")
    void createTooLongDescriptionReturns400() throws Exception {
        String body = createEventJson().replace("Festival de musica del Caribe", "x".repeat(1001));

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.description")
                        .value("Description must not exceed 1000 characters"));

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("PRD 11.4: JSON mal formado responde 400 con ErrorResponse y no invoca el Service")
    void createMalformedJsonReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"eventCode\": \"CMF-2026\", ")),
                400, "Bad Request", "Malformed JSON request");

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("PRD 11.4: una categoria que no existe en el enum responde 400 y no invoca el Service")
    void createUnknownCategoryReturns400() throws Exception {
        String body = createEventJson().replace("\"category\": \"MUSIC\"", "\"category\": \"PARTY\"");

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Malformed JSON request");

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("Request sin body responde 400 con ErrorResponse y no invoca el Service")
    void createWithoutBodyReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON)),
                400, "Bad Request", "Malformed JSON request");

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("AC-CTRL006: un conflicto de negocio al crear responde 409 con el mensaje del Service")
    void createBusinessRuleViolationReturns409() throws Exception {
        when(eventService.create(any(CreateEventRequest.class)))
                .thenThrow(new BusinessRuleException("Event date must be in the future."));

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventJson())),
                409, "Conflict", "Event date must be in the future.");
    }

    @Test
    @DisplayName("Crear un evento con codigo repetido responde 409")
    void createDuplicateEventReturns409() throws Exception {
        when(eventService.create(any(CreateEventRequest.class)))
                .thenThrow(new DuplicateResourceException("Event code already exists: CMF-2026"));

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventJson())),
                409, "Conflict", "Event code already exists: CMF-2026");
    }

    @Test
    @DisplayName("Crear un evento con un venue inexistente responde 404")
    void createWithUnknownVenueReturns404() throws Exception {
        when(eventService.create(any(CreateEventRequest.class)))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-SMR-01"));

        assertErrorResponse(
                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventJson())),
                404, "Not Found", "Venue not found: VEN-SMR-01");
    }

    // ------------------------------------------------------------------
    // GET /api/events/{eventCode}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-003: evento existente responde 200 con EventResponse")
    void findByCodeReturns200() throws Exception {
        when(eventService.findByCode("CMF-2026"))
                .thenReturn(event(EventStatus.PUBLISHED, List.of(solarBeat())));

        mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.venueName").value("Marina Convention Center"))
                .andExpect(jsonPath("$.artists.length()").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(eventService).findByCode("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-004: evento inexistente responde 404 con ErrorResponse")
    void findByCodeReturns404WhenNotFound() throws Exception {
        when(eventService.findByCode("CMF-2026"))
                .thenThrow(new ResourceNotFoundException("Event not found: CMF-2026"));

        assertErrorResponse(
                mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026")),
                404, "Not Found", "Event not found: CMF-2026")
                .andExpect(jsonPath("$.details").isEmpty());
    }

    // ------------------------------------------------------------------
    // GET /api/events/published
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-005 / UC-CTRL-05: cartelera responde 200 con EventSummaryResponse")
    void findPublishedEventsReturns200() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of(publishedSummary()));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].venueCode").value("VEN-SMR-01"));

        verify(eventService).findPublishedEvents();
        // /published es una ruta literal: no debe interpretarse como {eventCode}.
        verify(eventService, never()).findByCode(any());
    }

    // ------------------------------------------------------------------
    // PATCH /api/events/{eventCode}/publish
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-006 / AC-CTRL005: publicar un evento valido responde 200 con status PUBLISHED")
    void publishReturns200() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(publishedEvent());

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-007 / AC-CTRL006: publicar con conflicto de negocio responde 409")
    void publishBusinessRuleViolationReturns409() throws Exception {
        when(eventService.publish("CMF-2026"))
                .thenThrow(new BusinessRuleException("Only DRAFT events can be published: CMF-2026"));

        assertErrorResponse(
                mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026")),
                409, "Conflict", "Only DRAFT events can be published: CMF-2026")
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("Publicar un evento inexistente responde 404")
    void publishUnknownEventReturns404() throws Exception {
        when(eventService.publish("NOPE-1"))
                .thenThrow(new ResourceNotFoundException("Event not found: NOPE-1"));

        assertErrorResponse(
                mockMvc.perform(patch("/api/events/{eventCode}/publish", "NOPE-1")),
                404, "Not Found", "Event not found: NOPE-1");
    }

    // ------------------------------------------------------------------
    // POST /api/events/{eventCode}/artists/{artistId}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-008 / UC-CTRL-04: asociar un artista responde 200 con el evento actualizado")
    void addArtistReturns200() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L))
                .thenReturn(event(EventStatus.DRAFT, List.of(solarBeat())));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.artists.length()").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    @Test
    @DisplayName("FR-CTRL-EVT-005: asociar un artista ya asociado responde 409")
    void addArtistDuplicateReturns409() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L))
                .thenThrow(new DuplicateResourceException("Artist is already associated with event: CMF-2026"));

        assertErrorResponse(
                mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1)),
                409, "Conflict", "Artist is already associated with event: CMF-2026");
    }

    @Test
    @DisplayName("FR-CTRL-EVT-005: asociar un artista a un evento cerrado responde 409")
    void addArtistToClosedEventReturns409() throws Exception {
        when(eventService.addArtist("CMF-2026", 2L))
                .thenThrow(new BusinessRuleException("Artists cannot be added to a CANCELLED event."));

        assertErrorResponse(
                mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 2)),
                409, "Conflict", "Artists cannot be added to a CANCELLED event.");
    }

    @Test
    @DisplayName("Asociar un artista inexistente responde 404")
    void addUnknownArtistReturns404() throws Exception {
        when(eventService.addArtist("CMF-2026", 99L))
                .thenThrow(new ResourceNotFoundException("Artist not found: 99"));

        assertErrorResponse(
                mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 99)),
                404, "Not Found", "Artist not found: 99");
    }

    @Test
    @DisplayName("Un artistId no numerico responde 400 con ErrorResponse y no invoca el Service")
    void addArtistWithNonNumericIdReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", "abc")),
                400, "Bad Request", "Invalid value for parameter: artistId")
                .andExpect(jsonPath("$.details.artistId").value("Invalid value"));

        verifyNoInteractions(eventService);
    }

    // ------------------------------------------------------------------
    // GET /api/events/by-artist?stageName=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-009: buscar eventos por artista responde 200 con los eventos encontrados")
    void findByArtistReturns200() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(publishedSummary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
        verify(eventService, never()).findByCode(any());
    }

    @Test
    @DisplayName("FR-CTRL-EVT-006: artista sin eventos responde 200 con una lista vacia")
    void findByArtistWithoutEventsReturnsEmptyList() throws Exception {
        when(eventService.findByArtist("Neon Waves")).thenReturn(List.of());

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Neon Waves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Falta el parametro stageName: responde 400 con ErrorResponse y no invoca el Service")
    void findByArtistWithoutParameterReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(get("/api/events/by-artist")),
                400, "Bad Request", "Missing required parameter: stageName")
                .andExpect(jsonPath("$.details.stageName").value("Parameter is required"));

        verifyNoInteractions(eventService);
    }

    // ------------------------------------------------------------------
    // Errores transversales (GlobalExceptionHandler)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DoD: un error inesperado responde 500 con ErrorResponse generico, sin detalles internos")
    void unexpectedErrorReturns500WithoutInternalDetails() throws Exception {
        when(eventService.findByCode("CMF-2026"))
                .thenThrow(new IllegalStateException("jdbc:postgresql://internal-host/pulsepass password=hunter2"));

        String body = assertErrorResponse(
                mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026")),
                500, "Internal Server Error", "Unexpected internal error")
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        // NFR-CTRL-007: ni el mensaje de la excepcion ni datos de infraestructura llegan al cliente.
        assertThat(body).doesNotContain("hunter2").doesNotContain("jdbc").doesNotContain("IllegalStateException");
    }

    @Test
    @DisplayName("Un metodo HTTP no soportado responde 405 con ErrorResponse, no 500")
    void unsupportedMethodReturns405() throws Exception {
        assertErrorResponse(
                mockMvc.perform(put("/api/events")),
                405, "Method Not Allowed", "HTTP method not supported for this endpoint");

        verifyNoInteractions(eventService);
    }
}
