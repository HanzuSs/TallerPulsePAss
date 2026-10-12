package com.pulsepass.support;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.dto.response.VenueResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos de referencia de las pruebas de Controller (PRD Fase 3, seccion 23).
 *
 * Mantiene el escenario de las fases anteriores: venue VEN-SMR-01, evento CMF-2026,
 * artistas Solar Beat / Neon Waves / Caribbean Sound y la usuaria Andrea.
 * Los Services son mocks: aqui solo se construyen las respuestas que devolverian
 * y los bodies JSON que enviaria un cliente.
 */
public final class ControllerTestData {

    public static final String VENUE_CODE = "VEN-SMR-01";
    public static final String VENUE_NAME = "Marina Convention Center";
    public static final String EVENT_CODE = "CMF-2026";
    public static final String EVENT_NAME = "Caribbean Music Fest 2026";
    public static final String ANDREA_EMAIL = "andrea@example.com";
    public static final String TICKET_CODE = "TKT-0001";

    public static final LocalDateTime FESTIVAL_DATE = LocalDateTime.of(2026, 12, 20, 20, 0);

    private ControllerTestData() {
    }

    // ------------------------------------------------------------------
    // Responses que devolveria el Service
    // ------------------------------------------------------------------

    public static VenueResponse marinaConventionCenter() {
        return new VenueResponse(1L, VENUE_CODE, VENUE_NAME, "Santa Marta",
                "Av. del Ferrocarril 10-25", 3, true);
    }

    public static VenueResponse barranquillaArena() {
        return new VenueResponse(2L, "VEN-BAQ-01", "Barranquilla Arena", "Barranquilla",
                null, 800, true);
    }

    public static ArtistResponse solarBeat() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronica", true);
    }

    public static ArtistResponse neonWaves() {
        return new ArtistResponse(2L, "Neon Waves", "Mexico", "Synthpop", true);
    }

    public static ArtistResponse caribbeanSound() {
        return new ArtistResponse(3L, "Caribbean Sound", "Colombia", "Tropical", true);
    }

    public static EventResponse event(EventStatus eventStatus, List<ArtistResponse> artists) {
        return new EventResponse(10L, EVENT_CODE, EVENT_NAME, "Festival de musica del Caribe",
                EventCategory.MUSIC, eventStatus, FESTIVAL_DATE, 18, VENUE_CODE, VENUE_NAME, artists);
    }

    public static EventResponse draftEvent() {
        return event(EventStatus.DRAFT, List.of());
    }

    public static EventResponse publishedEvent() {
        return event(EventStatus.PUBLISHED, List.of());
    }

    public static EventSummaryResponse publishedSummary() {
        return new EventSummaryResponse(10L, EVENT_CODE, EVENT_NAME, EventCategory.MUSIC,
                EventStatus.PUBLISHED, FESTIVAL_DATE, VENUE_CODE, VENUE_NAME);
    }

    public static UserResponse andrea() {
        return new UserResponse(1L, "andrea", ANDREA_EMAIL, true, "Andrea", "Gomez",
                "+57 300 000 0000", "Santa Marta", LocalDate.of(1998, 5, 20));
    }

    public static TicketResponse ticket(String ticketCode, TicketStatus ticketStatus) {
        return new TicketResponse(100L, ticketCode, TicketType.GENERAL, new BigDecimal("100.00"),
                ticketStatus, LocalDateTime.of(2026, 10, 1, 9, 30), ANDREA_EMAIL, EVENT_CODE, EVENT_NAME);
    }

    public static TicketResponse ticket(TicketStatus ticketStatus) {
        return ticket(TICKET_CODE, ticketStatus);
    }

    // ------------------------------------------------------------------
    // Bodies JSON validos que enviaria un cliente
    // ------------------------------------------------------------------

    public static String createEventJson() {
        return """
                {
                  "eventCode": "CMF-2026",
                  "name": "Caribbean Music Fest 2026",
                  "description": "Festival de musica del Caribe",
                  "category": "MUSIC",
                  "eventDate": "2026-12-20T20:00:00",
                  "minimumAge": 18,
                  "venueCode": "VEN-SMR-01"
                }
                """;
    }

    public static String registerUserJson() {
        return """
                {
                  "username": "andrea",
                  "email": "andrea@example.com",
                  "firstName": "Andrea",
                  "lastName": "Gomez",
                  "phone": "+57 300 000 0000",
                  "city": "Santa Marta",
                  "birthDate": "1998-05-20"
                }
                """;
    }

    public static String purchaseTicketJson() {
        return """
                {
                  "userEmail": "andrea@example.com",
                  "eventCode": "CMF-2026",
                  "type": "GENERAL"
                }
                """;
    }
}
