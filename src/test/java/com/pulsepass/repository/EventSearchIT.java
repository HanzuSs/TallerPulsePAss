package com.pulsepass.repository;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.support.AbstractPostgresIT;
import com.pulsepass.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Descubrimiento y consultas combinadas.
 * FR-SRC-001..FR-SRC-004 / QT-008.
 */
class EventSearchIT extends AbstractPostgresIT {

    private static final LocalDateTime REFERENCE = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private Venue santaMarta;
    private Venue barranquilla;

    @BeforeEach
    void setUp() {
        santaMarta = venueRepository.saveAndFlush(TestData.marinaConventionCenter());
        barranquilla = venueRepository.saveAndFlush(TestData.venue("VEN-BAQ-01", "Barranquilla", 1200));

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();

        Event smrMarch = TestData.event("SMR-03", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 3, 10, 20, 0), santaMarta);
        smrMarch.addArtist(solarBeat);

        Event smrJune = TestData.event("SMR-06", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 6, 10, 20, 0), santaMarta);
        smrJune.addArtist(solarBeat);
        smrJune.addArtist(neonWaves);

        Event smrDraft = TestData.event("SMR-DRAFT", EventStatus.DRAFT,
                LocalDateTime.of(2026, 7, 10, 20, 0), santaMarta);
        smrDraft.addArtist(solarBeat);

        Event baqMay = TestData.event("BAQ-05", EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 5, 10, 20, 0), barranquilla);
        baqMay.addArtist(solarBeat);

        eventRepository.saveAllAndFlush(List.of(smrMarch, smrJune, smrDraft, baqMay));
    }

    @Test
    @DisplayName("FR-SRC-002: filtra por venue.city y artist.stageName")
    void findsEventsByCityAndArtist() {
        List<Event> events = eventRepository.findEventsByCityAndArtist("santa marta", "solar beat");

        assertThat(events).extracting(Event::getEventCode)
                .containsExactly("SMR-03", "SMR-06", "SMR-DRAFT");
        assertThat(events).allMatch(e -> e.getVenue().getCity().equals("Santa Marta"));
    }

    @Test
    @DisplayName("FR-SRC-003: recomendados = PUBLISHED + posteriores a una fecha + ciudad + artista parcial")
    void findsRecommendedEvents() {
        List<Event> events = eventRepository.findRecommendedEvents(
                LocalDateTime.of(2026, 4, 1, 0, 0), "Santa Marta", "solar");

        assertThat(events).extracting(Event::getEventCode).containsExactly("SMR-06");
        assertThat(events).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("FR-SRC-003: la busqueda por artista es case-insensitive y parcial")
    void recommendationIsCaseInsensitive() {
        List<Event> upper = eventRepository.findRecommendedEvents(REFERENCE, "SANTA MARTA", "SOLAR BEAT");
        List<Event> partial = eventRepository.findRecommendedEvents(REFERENCE, "santa marta", "beat");

        assertThat(upper).extracting(Event::getEventCode).containsExactly("SMR-03", "SMR-06");
        assertThat(partial).extracting(Event::getEventCode).containsExactly("SMR-03", "SMR-06");
    }

    @Test
    @DisplayName("FR-SRC-004: los tickets de eventos futuros quedan ordenados cronologicamente")
    void findsTicketsOfFutureEvents() {
        User andrea = userRepository.saveAndFlush(TestData.user("andrea", "andrea@pulsepass.co"));
        Event june = eventRepository.findByEventCode("SMR-06").orElseThrow();
        Event march = eventRepository.findByEventCode("SMR-03").orElseThrow();

        ticketRepository.saveAllAndFlush(List.of(
                TestData.ticket("TCK-J", TicketType.VIP, "250000.00", TicketStatus.PAID, andrea, june),
                TestData.ticket("TCK-M", TicketType.GENERAL, "120000.00", TicketStatus.PAID, andrea, march)
        ));

        List<Ticket> tickets = ticketRepository
                .findByEvent_EventDateAfterOrderByEvent_EventDateAsc(LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(tickets).extracting(Ticket::getTicketCode).containsExactly("TCK-M", "TCK-J");
    }
}
