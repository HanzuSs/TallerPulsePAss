package com.pulsepass.repository;

import com.pulsepass.support.AbstractPostgresIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QT-001 y QT-002: Flyway construye el esquema desde cero e Hibernate
 * arranca en modo validate sin crear ni modificar tablas.
 */
class FlywayMigrationIT extends AbstractPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("QT-001: V1, V2 y V3 se aplican en orden desde una base vacia")
    void migrationsAreApplied() {
        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                String.class);

        assertThat(versions).containsExactly("1", "2", "3");
    }

    @Test
    @DisplayName("QT-002: el contexto carga con ddl-auto=validate, luego el esquema coincide con las entidades")
    void hibernateValidatesSchema() {
        // Si el mapeo no coincidiera con las migraciones, el contexto de Spring
        // no habria arrancado y esta prueba nunca se ejecutaria.
        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                ORDER BY table_name
                """,
                String.class);

        assertThat(tables).contains(
                "artists", "event_artists", "events", "tickets", "user_profiles", "users", "venues");
    }

    @Test
    @DisplayName("V2 carga el catalogo inicial de artistas")
    void initialArtistsAreSeeded() {
        List<String> names = jdbcTemplate.queryForList(
                "SELECT stage_name FROM artists ORDER BY stage_name", String.class);

        assertThat(names).contains(
                "Caribbean Sound", "Digital Pulse", "Neon Waves", "Ocean Drive", "Solar Beat");
    }

    @Test
    @DisplayName("FR-EVT-006: V3 agrega streaming_url nullable sin tocar V1")
    void streamingUrlColumnExists() {
        var column = jdbcTemplate.queryForMap(
                """
                SELECT data_type, character_maximum_length, is_nullable
                FROM information_schema.columns
                WHERE table_name = 'events' AND column_name = 'streaming_url'
                """);

        assertThat(column.get("data_type")).isEqualTo("character varying");
        assertThat(column.get("character_maximum_length")).isEqualTo(500);
        assertThat(column.get("is_nullable")).isEqualTo("YES");
    }
}
