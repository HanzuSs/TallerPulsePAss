package com.pulsepass.controller;

import com.pulsepass.service.ArtistService;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import com.pulsepass.service.UserService;
import com.pulsepass.service.VenueService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reglas de arquitectura del PRD Fase 3, verificadas por reflexion (sin contexto
 * de Spring, sin base de datos):
 *
 * AC-CTRL015 / CTRL-002: ningun Controller accede a un Repository.
 * AC-CTRL016 / CTRL-003: ningun endpoint recibe ni devuelve entidades JPA.
 * DoD: todos los handlers responden ResponseEntity y se cubren los 20 metodos de Service.
 */
class ControllerArchitectureTest {

    private static final String DOMAIN_PACKAGE = "com.pulsepass.domain";
    private static final String REPOSITORY_PACKAGE = "com.pulsepass.repository";
    private static final String SERVICE_PACKAGE = "com.pulsepass.service";

    private static final List<Class<?>> CONTROLLERS = List.of(
            VenueController.class,
            EventController.class,
            ArtistController.class,
            UserController.class,
            TicketController.class);

    private static final List<Class<?>> SERVICES = List.of(
            VenueService.class,
            EventService.class,
            ArtistService.class,
            UserService.class,
            TicketService.class);

    @Test
    @DisplayName("Los cinco Controllers son @RestController")
    void controllersAreRestControllers() {
        assertThat(CONTROLLERS).hasSize(5);
        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.isAnnotationPresent(RestController.class))
                    .as("%s debe estar anotado con @RestController", controller.getSimpleName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("AC-CTRL015 / CTRL-002: ningun Controller depende de un Repository")
    void controllersDoNotDependOnRepositories() {
        for (Class<?> controller : CONTROLLERS) {
            for (Class<?> dependency : dependenciesOf(controller)) {
                assertThat(dependency.getName())
                        .as("%s no debe depender de un Repository", controller.getSimpleName())
                        .doesNotStartWith(REPOSITORY_PACKAGE);
            }
        }
    }

    @Test
    @DisplayName("DoD / NFR-CTRL-004: los Controllers dependen exclusivamente de interfaces de Service")
    void controllersDependOnlyOnServiceInterfaces() {
        for (Class<?> controller : CONTROLLERS) {
            Set<Class<?>> dependencies = dependenciesOf(controller);
            assertThat(dependencies)
                    .as("%s debe tener al menos una dependencia (su Service)", controller.getSimpleName())
                    .isNotEmpty();
            for (Class<?> dependency : dependencies) {
                assertThat(dependency.getName())
                        .as("dependencia de %s", controller.getSimpleName())
                        .startsWith(SERVICE_PACKAGE + ".");
                assertThat(dependency.isInterface())
                        .as("%s debe depender de la interfaz, no de la implementacion", dependency.getSimpleName())
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("AC-CTRL016 / CTRL-003: ningun endpoint recibe ni devuelve entidades JPA")
    void endpointsDoNotExposeEntities() {
        for (Method handler : handlerMethods()) {
            Set<String> typeNames = new HashSet<>();
            collectTypeNames(handler.getGenericReturnType(), typeNames);
            for (Type parameterType : handler.getGenericParameterTypes()) {
                collectTypeNames(parameterType, typeNames);
            }
            for (String typeName : typeNames) {
                assertThat(typeName)
                        .as("%s.%s no debe usar entidades del dominio", handler.getDeclaringClass().getSimpleName(), handler.getName())
                        .doesNotStartWith(DOMAIN_PACKAGE + ".Venue")
                        .doesNotStartWith(DOMAIN_PACKAGE + ".Event")
                        .doesNotStartWith(DOMAIN_PACKAGE + ".Artist")
                        .doesNotStartWith(DOMAIN_PACKAGE + ".User")
                        .doesNotStartWith(DOMAIN_PACKAGE + ".Ticket");
            }
        }
    }

    @Test
    @DisplayName("DoD: todos los handlers responden un ResponseEntity")
    void handlersReturnResponseEntity() {
        for (Method handler : handlerMethods()) {
            assertThat(handler.getReturnType())
                    .as("%s.%s debe devolver ResponseEntity", handler.getDeclaringClass().getSimpleName(), handler.getName())
                    .isEqualTo(ResponseEntity.class);
        }
    }

    @Test
    @DisplayName("PRD seccion 9 / DoD: los 20 metodos de Service tienen un endpoint HTTP")
    void everyServiceOperationHasAnEndpoint() {
        long serviceOperations = SERVICES.stream()
                .flatMap(service -> Arrays.stream(service.getDeclaredMethods()))
                .filter(method -> !method.isSynthetic())
                .count();
        long endpoints = handlerMethods().size();

        assertThat(serviceOperations)
                .as("operaciones publicas de Service definidas en la Fase 2")
                .isEqualTo(20);
        assertThat(endpoints)
                .as("endpoints expuestos por los cinco Controllers")
                .isEqualTo(serviceOperations);
    }

    // ------------------------------------------------------------------
    // Utilidades de reflexion
    // ------------------------------------------------------------------

    /** Tipos de los campos y de los parametros de constructor del controller. */
    private static Set<Class<?>> dependenciesOf(Class<?> controller) {
        Set<Class<?>> dependencies = new HashSet<>();
        for (Field field : controller.getDeclaredFields()) {
            if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                dependencies.add(field.getType());
            }
        }
        for (var constructor : controller.getDeclaredConstructors()) {
            dependencies.addAll(Arrays.asList(constructor.getParameterTypes()));
        }
        return dependencies;
    }

    /** Metodos publicos mapeados con @GetMapping, @PostMapping o @PatchMapping. */
    private static List<Method> handlerMethods() {
        return CONTROLLERS.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(GetMapping.class)
                        || method.isAnnotationPresent(PostMapping.class)
                        || method.isAnnotationPresent(PatchMapping.class))
                .toList();
    }

    /** Recorre genericos y comodines: ResponseEntity&lt;List&lt;X&gt;&gt; aporta ResponseEntity, List y X. */
    private static void collectTypeNames(Type type, Set<String> names) {
        if (type instanceof Class<?> clazz) {
            names.add(clazz.getName());
        } else if (type instanceof ParameterizedType parameterized) {
            collectTypeNames(parameterized.getRawType(), names);
            for (Type argument : parameterized.getActualTypeArguments()) {
                collectTypeNames(argument, names);
            }
        } else if (type instanceof WildcardType wildcard) {
            for (Type bound : wildcard.getUpperBounds()) {
                collectTypeNames(bound, names);
            }
            for (Type bound : wildcard.getLowerBounds()) {
                collectTypeNames(bound, names);
            }
        }
    }
}
