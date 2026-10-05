package com.pulsepass.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base de todas las pruebas de integracion.
 *
 * NFR-004 / NFR-005 / QT-001: se levanta PostgreSQL real con Testcontainers,
 * nunca H2 y sin depender de una instalacion local del estudiante.
 *
 * El contenedor es un singleton: se inicia una sola vez para toda la suite
 * y Ryuk lo elimina al terminar la JVM.
 */
@SpringBootTest
@Transactional
public abstract class AbstractPostgresIT {

        protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("pulsepass")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
