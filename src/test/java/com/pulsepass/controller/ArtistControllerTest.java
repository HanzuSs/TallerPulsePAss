package com.pulsepass.controller;

import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.pulsepass.security.JwtService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pulsepass.support.ControllerTestData.caribbeanSound;
import static com.pulsepass.support.ControllerTestData.neonWaves;
import static com.pulsepass.support.ControllerTestData.solarBeat;
import static com.pulsepass.support.ErrorResponseAssertions.assertErrorResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de ArtistController (PRD Fase 3: FR-CTRL-ART-001..003,
 * TEST-CTRL-ART-001..004).
 */
@WebMvcTest(ArtistController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private ArtistService artistService;

    @Test
    @DisplayName("TEST-CTRL-ART-001: buscar artista por id responde 200 con ArtistResponse")
    void findByIdReturns200() throws Exception {
        when(artistService.findById(1L)).thenReturn(solarBeat());

        mockMvc.perform(get("/api/artists/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.country").value("Colombia"))
                .andExpect(jsonPath("$.genre").value("Electronica"))
                .andExpect(jsonPath("$.active").value(true));

        verify(artistService).findById(1L);
    }

    @Test
    @DisplayName("TEST-CTRL-ART-002: id inexistente responde 404 con ErrorResponse")
    void findByIdReturns404WhenNotFound() throws Exception {
        when(artistService.findById(99L)).thenThrow(new ResourceNotFoundException("Artist not found: 99"));

        assertErrorResponse(
                mockMvc.perform(get("/api/artists/{id}", 99)),
                404, "Not Found", "Artist not found: 99");
    }

    @Test
    @DisplayName("Un id no numerico responde 400 con ErrorResponse y no invoca el Service")
    void findByIdReturns400WhenIdIsNotNumeric() throws Exception {
        assertErrorResponse(
                mockMvc.perform(get("/api/artists/{id}", "abc")),
                400, "Bad Request", "Invalid value for parameter: id")
                .andExpect(jsonPath("$.details.id").value("Invalid value"));

        verify(artistService, never()).findById(any());
    }

    @Test
    @DisplayName("TEST-CTRL-ART-003: buscar por stageName responde 200 con ArtistResponse")
    void findByStageNameReturns200() throws Exception {
        when(artistService.findByStageName("Neon Waves")).thenReturn(neonWaves());

        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Neon Waves"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stageName").value("Neon Waves"))
                .andExpect(jsonPath("$.country").value("Mexico"));

        verify(artistService).findByStageName("Neon Waves");
        verify(artistService, never()).findById(any());
    }

    @Test
    @DisplayName("FR-CTRL-ART-002: stageName inexistente responde 404 con ErrorResponse")
    void findByStageNameReturns404WhenNotFound() throws Exception {
        when(artistService.findByStageName("Nadie"))
                .thenThrow(new ResourceNotFoundException("Artist not found: Nadie"));

        assertErrorResponse(
                mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Nadie")),
                404, "Not Found", "Artist not found: Nadie");
    }

    @Test
    @DisplayName("Falta el parametro stageName: responde 400 con ErrorResponse y no invoca el Service")
    void findByStageNameReturns400WhenParameterIsMissing() throws Exception {
        assertErrorResponse(
                mockMvc.perform(get("/api/artists/by-stage-name")),
                400, "Bad Request", "Missing required parameter: stageName")
                .andExpect(jsonPath("$.details.stageName").value("Parameter is required"));

        verify(artistService, never()).findByStageName(any());
    }

    @Test
    @DisplayName("TEST-CTRL-ART-004: artistas activos responde 200 con la coleccion de ArtistResponse")
    void findActiveArtistsReturns200() throws Exception {
        when(artistService.findActiveArtists())
                .thenReturn(List.of(caribbeanSound(), neonWaves(), solarBeat()));

        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].stageName").value("Caribbean Sound"))
                .andExpect(jsonPath("$[1].stageName").value("Neon Waves"))
                .andExpect(jsonPath("$[2].stageName").value("Solar Beat"));

        verify(artistService).findActiveArtists();
        // /active es una ruta literal: no debe interpretarse como {id}.
        verify(artistService, never()).findById(any());
    }
}
