package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pulsepass.support.ControllerTestData.andrea;
import static com.pulsepass.support.ControllerTestData.registerUserJson;
import static com.pulsepass.support.ErrorResponseAssertions.assertErrorResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de UserController (PRD Fase 3: FR-CTRL-USR-001..003,
 * TEST-CTRL-USR-001..005, AC-CTRL007/008).
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // ------------------------------------------------------------------
    // POST /api/users
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-USR-001 / AC-CTRL007: registrar un usuario valido responde 201 con UserResponse")
    void registerValidUserReturns201() throws Exception {
        when(userService.register(any(RegisterUserRequest.class))).thenReturn(andrea());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.firstName").value("Andrea"))
                .andExpect(jsonPath("$.birthDate").value("1998-05-20"));

        // El JSON del cliente llega al Service convertido en RegisterUserRequest.
        ArgumentCaptor<RegisterUserRequest> captor = ArgumentCaptor.forClass(RegisterUserRequest.class);
        verify(userService).register(captor.capture());
        RegisterUserRequest sent = captor.getValue();
        assertThat(sent.username()).isEqualTo("andrea");
        assertThat(sent.email()).isEqualTo("andrea@example.com");
        assertThat(sent.birthDate()).hasToString("1998-05-20");
    }

    @Test
    @DisplayName("TEST-CTRL-USR-002: email invalido responde 400 con details y no invoca el Service")
    void registerInvalidEmailReturns400() throws Exception {
        String body = registerUserJson().replace("\"email\": \"andrea@example.com\"", "\"email\": \"no-es-un-email\"");

        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.email").value("Email must be a valid address"))
                .andExpect(jsonPath("$.details.username").doesNotExist());

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("QT-CTRL-010: un body vacio reporta cada campo obligatorio en details")
    void registerEmptyBodyReportsEveryRequiredField() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")),
                400, "Bad Request", "Validation failed")
                .andExpect(jsonPath("$.details.username").value("Username is required"))
                .andExpect(jsonPath("$.details.email").value("Email is required"))
                .andExpect(jsonPath("$.details.firstName").value("First name is required"))
                .andExpect(jsonPath("$.details.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.details.birthDate").value("Birth date is required"))
                .andExpect(jsonPath("$.details.phone").doesNotExist())
                .andExpect(jsonPath("$.details.city").doesNotExist());

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("PRD 11.4: una fecha de nacimiento ilegible responde 400 y no invoca el Service")
    void registerUnreadableBirthDateReturns400() throws Exception {
        String body = registerUserJson().replace("1998-05-20", "20/05/1998");

        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)),
                400, "Bad Request", "Malformed JSON request");

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("Un Content-Type distinto de JSON responde 415 con ErrorResponse y no invoca el Service")
    void registerWithUnsupportedContentTypeReturns415() throws Exception {
        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("andrea")),
                415, "Unsupported Media Type", "Content type not supported, use application/json");

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("TEST-CTRL-USR-003: username duplicado responde 409 con ErrorResponse")
    void registerDuplicateUsernameReturns409() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserJson())),
                409, "Conflict", "Username already exists: andrea")
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("AC-CTRL008: email duplicado responde 409 sin exponer detalles internos")
    void registerDuplicateEmailReturns409WithoutInternalDetails() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already exists: andrea@example.com"));

        String body = assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserJson())),
                409, "Conflict", "Email already exists: andrea@example.com")
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("Exception").doesNotContain("at com.pulsepass");
    }

    @Test
    @DisplayName("Una regla de negocio del Service al registrar responde 409")
    void registerBusinessRuleViolationReturns409() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new BusinessRuleException("Birth date cannot be in the future."));

        assertErrorResponse(
                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerUserJson())),
                409, "Conflict", "Birth date cannot be in the future.");
    }

    // ------------------------------------------------------------------
    // GET /api/users/by-email?email=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-USR-004: consultar por email responde 200 con UserResponse")
    void findByEmailReturns200() throws Exception {
        when(userService.findByEmail("andrea@example.com")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@example.com"))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByEmail("andrea@example.com");
    }

    @Test
    @DisplayName("FR-CTRL-USR-002: email inexistente responde 404 con ErrorResponse")
    void findByEmailReturns404WhenNotFound() throws Exception {
        when(userService.findByEmail("nadie@example.com"))
                .thenThrow(new ResourceNotFoundException("User not found: nadie@example.com"));

        assertErrorResponse(
                mockMvc.perform(get("/api/users/by-email").param("email", "nadie@example.com")),
                404, "Not Found", "User not found: nadie@example.com");
    }

    @Test
    @DisplayName("Falta el parametro email: responde 400 con ErrorResponse y no invoca el Service")
    void findByEmailWithoutParameterReturns400() throws Exception {
        assertErrorResponse(
                mockMvc.perform(get("/api/users/by-email")),
                400, "Bad Request", "Missing required parameter: email")
                .andExpect(jsonPath("$.details.email").value("Parameter is required"));

        verifyNoInteractions(userService);
    }

    // ------------------------------------------------------------------
    // GET /api/users/by-username?username=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-USR-005: consultar por username responde 200 con UserResponse")
    void findByUsernameReturns200() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"));

        verify(userService).findByUsername("andrea");
    }

    @Test
    @DisplayName("FR-CTRL-USR-003: username inexistente responde 404 con ErrorResponse")
    void findByUsernameReturns404WhenNotFound() throws Exception {
        when(userService.findByUsername("nadie"))
                .thenThrow(new ResourceNotFoundException("User not found: nadie"));

        assertErrorResponse(
                mockMvc.perform(get("/api/users/by-username").param("username", "nadie")),
                404, "Not Found", "User not found: nadie");
    }
}
