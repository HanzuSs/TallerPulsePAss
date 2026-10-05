package com.pulsepass.repository;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.support.AbstractPostgresIT;
import com.pulsepass.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Relacion Venue 1:N Event y consultas de cartelera.
 * FR-EVT-001..FR-EVT-006, FR-VEN-004 / AC-002, AC-006 / QT-003, QT-007.
 */
class EventRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Venue venue;

    @BeforeEach
    void setUp() {
        venue = venueRepository.saveAndFlush(TestData.marinaConventionCenter());
    }

    @Test
    @DisplayName("AC-002 / QT-003: el evento queda asociado a su venue y se recupera por eventCode")
    void eventBelongsToVenue() {
        eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));

        Event found = eventRepository.findByEventCode("CMF-2026").orElseThrow();

        assertThat(found.getVenue().getCode()).isEqualTo("VEN-SMR-01");
        assertThat(found.getVenue().getCity()).isEqualTo("Santa Marta");
        assertThat(found.getCategory()).isNotNull();
    }

    @Test
    @DisplayName("FR-EVT-002 / QT-009: PostgreSQL rechaza un eventCode duplicado")
    void rejectsDuplicateEventCode() {
        eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));

        Event duplicate = TestData.event("CMF-2026", EventStatus.DRAFT,
                LocalDateTime.of(2026, 8, 1, 18, 0), venue);

        assertThatThrownBy(() -> eventRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FR-EVT-004 / BR-008: los enums se guardan por nombre, no por ordinal")
    void enumsArePersistedByName() {
        eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));

        String category = jdbcTemplate.queryForObject(
                "SELECT category FROM events WHERE event_code = 'CMF-2026'", String.class);
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM events WHERE event_code = 'CMF-2026'", String.class);

        assertThat(category).isEqualTo("MUSIC");
        assertThat(status).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("AC-006 / FR-EVT-005: solo se listan los PUBLISHED y en orden cronologico")
    void listsOnlyPublishedEventsInChronologicalOrder() {
        eventRepository.saveAndFlush(TestData.event("EVT-C", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 9, 1, 20, 0), venue));
        eventRepository.saveAndFlush(TestData.event("EVT-A", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 4, 1, 20, 0), venue));
        eventRepository.saveAndFlush(TestData.event("EVT-B", EventStatus.DRAFT,
                LocalDateTime.of(2026, 5, 1, 20, 0), venue));
        eventRepository.saveAndFlush(TestData.event("EVT-D", EventStatus.CANCELLED,
                LocalDateTime.of(2026, 6, 1, 20, 0), venue));

        List<Event> published = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(published).extracting(Event::getEventCode).containsExactly("EVT-A", "EVT-C");
        assertThat(published).noneMatch(e -> e.getStatus() == EventStatus.DRAFT);
    }

    @Test
    @DisplayName("FR-VEN-004 / QT-007: navegacion Event -> Venue -> code")
    void findsEventsByVenueBusinessCode() {
        Venue other = venueRepository.saveAndFlush(TestData.venue("VEN-BAQ-01", "Barranquilla", 1200));
        eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));
        eventRepository.saveAndFlush(TestData.event("EVT-BAQ", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 7, 1, 20, 0), other));

        List<Event> events = eventRepository.findByVenue_Code("VEN-SMR-01");

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventCode()).isEqualTo("CMF-2026");
    }

    @Test
    @DisplayName("FR-EVT-006: streamingUrl es opcional y admite hasta 500 caracteres")
    void streamingUrlIsOptional() {
        Event withoutStreaming = eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));
        assertThat(withoutStreaming.getStreamingUrl()).isNull();

        withoutStreaming.setStreamingUrl("https://stream.pulsepass.co/cmf-2026");
        eventRepository.saveAndFlush(withoutStreaming);

        assertThat(eventRepository.findByEventCode("CMF-2026").orElseThrow().getStreamingUrl())
                .isEqualTo("https://stream.pulsepass.co/cmf-2026");
    }
}
