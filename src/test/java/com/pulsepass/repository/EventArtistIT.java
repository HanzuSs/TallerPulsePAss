package com.pulsepass.repository;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
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
 * Relacion Event N:M Artist.
 * FR-ART-002..FR-ART-004, FR-SRC-001 / AC-003, AC-007 / QT-005, QT-008.
 */
class EventArtistIT extends AbstractPostgresIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

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
    @DisplayName("FR-ART-002: la BD rechaza un stageName duplicado")
    void rejectsDuplicateStageName() {
        // Solar Beat ya existe: lo inserto la migracion V2.
        assertThatThrownBy(() -> artistRepository.saveAndFlush(TestData.artist("Solar Beat")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AC-003 / QT-005: un evento con tres artistas persiste la asociacion sin duplicar pares")
    void persistsManyToManyWithoutDuplicates() {
        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();
        Artist caribbean = artistRepository.findByStageName("Caribbean Sound").orElseThrow();

        Event festival = TestData.caribbeanMusicFest(venue);
        festival.addArtist(solarBeat);
        festival.addArtist(neonWaves);
        festival.addArtist(caribbean);
        // Intento deliberado de repetir el mismo par evento-artista:
        festival.addArtist(solarBeat);

        eventRepository.saveAndFlush(festival);

        Long rows = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM event_artists ea
                JOIN events e ON e.id = ea.event_id
                WHERE e.event_code = 'CMF-2026'
                """, Long.class);

        assertThat(rows).isEqualTo(3L);
        assertThat(eventRepository.countArtistsByEventCode("CMF-2026")).isEqualTo(3L);
    }

    @Test
    @DisplayName("La PK compuesta de event_artists impide insertar el par dos veces")
    void compositeKeyRejectsDuplicatePair() {
        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Event festival = TestData.caribbeanMusicFest(venue);
        festival.addArtist(solarBeat);
        Event saved = eventRepository.saveAndFlush(festival);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO event_artists (event_id, artist_id) VALUES (?, ?)",
                saved.getId(), solarBeat.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AC-007 / FR-ART-004 / QT-008: la consulta JPQL devuelve cada evento una sola vez")
    void findsEventsByArtistWithoutDuplicates() {
        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();

        Event first = TestData.event("EVT-1", com.pulsepass.domain.enums.EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 4, 1, 20, 0), venue);
        first.addArtist(solarBeat);
        first.addArtist(neonWaves);

        Event second = TestData.event("EVT-2", com.pulsepass.domain.enums.EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 5, 1, 20, 0), venue);
        second.addArtist(solarBeat);

        Event withoutSolarBeat = TestData.event("EVT-3", com.pulsepass.domain.enums.EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 6, 1, 20, 0), venue);
        withoutSolarBeat.addArtist(neonWaves);

        eventRepository.saveAllAndFlush(List.of(first, second, withoutSolarBeat));

        List<Event> events = eventRepository.findEventsByArtistStageName("Solar Beat");

        assertThat(events).extracting(Event::getEventCode).containsExactly("EVT-1", "EVT-2");
        assertThat(events).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("FR-ART-004: un artista participa en varios eventos (lado inverso de la N:M)")
    void artistParticipatesInSeveralEvents() {
        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();

        Event first = TestData.event("EVT-10", com.pulsepass.domain.enums.EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 4, 1, 20, 0), venue);
        Event second = TestData.event("EVT-11", com.pulsepass.domain.enums.EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 5, 1, 20, 0), venue);
        first.addArtist(solarBeat);
        second.addArtist(solarBeat);
        eventRepository.saveAllAndFlush(List.of(first, second));

        assertThat(eventRepository.findEventsByArtistStageName("Solar Beat")).hasSize(2);
    }
}
