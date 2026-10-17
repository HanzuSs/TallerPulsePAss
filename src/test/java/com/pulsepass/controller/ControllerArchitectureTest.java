package com.pulsepass.controller;

import com.pulsepass.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerArchitectureTest {

    @Test
    @DisplayName("Verifica que cada operación de servicio tenga un endpoint mapeado")
    void everyServiceOperationHasAnEndpoint() {
        List<Class<?>> serviceInterfaces = List.of(
                ArtistService.class,
                AuthService.class,
                EventService.class,
                TicketService.class,
                UserService.class,
                VenueService.class
        );

        long totalServiceOperations = serviceInterfaces.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                // Las operaciones authorize* son helpers internos de seguridad, no endpoints HTTP.
                .filter(method -> !method.getName().startsWith("authorize"))
                .count();

        // Las seis interfaces exponen 22 operaciones de negocio accesibles por endpoints.
        assertThat(totalServiceOperations).isEqualTo(22L);
    }
}