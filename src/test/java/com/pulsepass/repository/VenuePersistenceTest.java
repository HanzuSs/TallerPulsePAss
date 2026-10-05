package com.pulsepass.repository;

import com.pulsepass.domain.Venue;
import com.pulsepass.support.AbstractPostgresIT;
import com.pulsepass.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FR-VEN-001..FR-VEN-003 / AC-001 / QT-009.
 */
class VenuePersistenceTest extends AbstractPostgresIT {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("AC-001: un venue valido se persiste y se recupera por codigo")
    void persistAndFindByCode() {
        venueRepository.saveAndFlush(TestData.marinaConventionCenter());

        Venue found = venueRepository.findByCode("VEN-SMR-01").orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getCity()).isEqualTo("Santa Marta");
        assertThat(found.getCapacity()).isGreaterThan(0);
        assertThat(found.isActive()).isTrue();
    }

    @Test
    @DisplayName("FR-VEN-002 / QT-009: PostgreSQL rechaza un codigo de venue duplicado")
    void rejectsDuplicateCode() {
        venueRepository.saveAndFlush(TestData.marinaConventionCenter());

        Venue duplicate = TestData.venue("VEN-SMR-01", "Barranquilla", 800);

        assertThatThrownBy(() -> venueRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FR-VEN-003: PostgreSQL rechaza capacidad igual o menor que cero")
    void rejectsNonPositiveCapacity() {
        // Se inserta por JDBC a proposito: asi se verifica el CHECK de la base
        // y no la validacion de Bean Validation, que actuaria antes.
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO venues (code, name, city, address, capacity, active)
                VALUES ('VEN-ZERO', 'Sin capacidad', 'Santa Marta', 'N/A', 0, TRUE)
                """))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_venues_capacity");
    }
}
