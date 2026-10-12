package com.pulsepass.controller;

import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pulsepass.support.ControllerTestData.barranquillaArena;
import static com.pulsepass.support.ControllerTestData.marinaConventionCenter;
import static com.pulsepass.support.ErrorResponseAssertions.assertErrorResponse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de VenueController (PRD Fase 3: FR-CTRL-VEN-001/002,
 * TEST-CTRL-VEN-001..003, AC-CTRL001/002).
 *
 * El Service es un mock: no hay PostgreSQL ni logica real (NFR-CTRL-002).
 */
@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
    @DisplayName("TEST-CTRL-VEN-001 / AC-CTRL001: venue existente responde 200 con VenueResponse")
    void findByCodeReturns200() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(marinaConventionCenter());

        mockMvc.perform(get("/api/venues/{code}", "VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-002 / AC-CTRL002: venue inexistente responde 404 con ErrorResponse")
    void findByCodeReturns404WhenNotFound() throws Exception {
        when(venueService.findByCode("VEN-NOPE-99"))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-NOPE-99"));

        assertErrorResponse(
                mockMvc.perform(get("/api/venues/{code}", "VEN-NOPE-99")),
                404, "Not Found", "Venue not found: VEN-NOPE-99")
                .andExpect(jsonPath("$.details").isEmpty());

        verify(venueService).findByCode("VEN-NOPE-99");
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-003: venues activos responde 200 con la coleccion de VenueResponse")
    void findActiveVenuesReturns200() throws Exception {
        when(venueService.findActiveVenues())
                .thenReturn(List.of(marinaConventionCenter(), barranquillaArena()));

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[1].code").value("VEN-BAQ-01"));

        verify(venueService).findActiveVenues();
        // /active es una ruta literal: no debe interpretarse como {code}.
        verify(venueService, never()).findByCode(anyString());
    }

    @Test
    @DisplayName("FR-CTRL-VEN-002: sin venues activos responde 200 con una lista vacia")
    void findActiveVenuesReturnsEmptyList() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of());

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
