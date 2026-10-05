package com.pulsepass.repository;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.support.AbstractPostgresIT;
import com.pulsepass.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ticket -> User y Ticket -> Event, con el escenario de la seccion 16.3.
 * FR-TKT-001..FR-TKT-008 / AC-005, AC-008 / QT-006, QT-007, QT-008.
 */
class TicketRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Event festival;
    private User andrea;

    @BeforeEach
    void setUp() {
        Venue venue = venueRepository.saveAndFlush(TestData.marinaConventionCenter());
        festival = eventRepository.saveAndFlush(TestData.caribbeanMusicFest(venue));

        andrea = userRepository.saveAndFlush(TestData.user("andrea", "andrea@pulsepass.co"));
        User carlos = userRepository.saveAndFlush(TestData.user("carlos", "carlos@pulsepass.co"));
        User laura = userRepository.saveAndFlush(TestData.user("laura", "laura@pulsepass.co"));
        User miguel = userRepository.saveAndFlush(TestData.user("miguel", "miguel@pulsepass.co"));

        ticketRepository.saveAllAndFlush(List.of(
                TestData.ticket("TCK-0001", TicketType.VIP, "250000.00", TicketStatus.PAID, andrea, festival),
                TestData.ticket("TCK-0002", TicketType.GENERAL, "120000.00", TicketStatus.PAID, carlos, festival),
                TestData.ticket("TCK-0003", TicketType.GENERAL, "120000.00", TicketStatus.RESERVED, laura, festival),
                TestData.ticket("TCK-0004", TicketType.VIP, "250000.00", TicketStatus.CANCELLED, miguel, festival)
        ));
    }

    @Test
    @DisplayName("QT-006 / FR-TKT-001: el ticket mantiene trazabilidad hacia usuario y evento")
    void ticketLinksUserAndEvent() {
        Ticket ticket = ticketRepository.findByTicketCode("TCK-0001").orElseThrow();

        assertThat(ticket.getUser().getUsername()).isEqualTo("andrea");
        assertThat(ticket.getEvent().getEventCode()).isEqualTo("CMF-2026");
        assertThat(ticket.getPrice()).isEqualByComparingTo("250000.00");
    }

    @Test
    @DisplayName("AC-005 / QT-009: PostgreSQL rechaza un ticketCode duplicado")
    void rejectsDuplicateTicketCode() {
        Ticket duplicate = TestData.ticket("TCK-0001", TicketType.STUDENT, "50000.00",
                TicketStatus.RESERVED, andrea, festival);

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FR-TKT-003: el CHECK impide precios negativos")
    void rejectsNegativePrice() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO tickets (ticket_code, type, price, status, purchase_date, user_id, event_id)
                VALUES ('TCK-NEG', 'GENERAL', -1.00, 'RESERVED', now(), ?, ?)
                """, andrea.getId(), festival.getId()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_tickets_price");
    }

    @Test
    @DisplayName("FR-TKT-006 / QT-007: tickets de un usuario navegando Ticket -> User -> email")
    void findsTicketsByUserEmail() {
        assertThat(ticketRepository.findByUser_EmailIgnoreCase("ANDREA@PULSEPASS.CO")).hasSize(1);
        assertThat(ticketRepository.findByUser_EmailIgnoreCaseAndStatus(
                "andrea@pulsepass.co", TicketStatus.PAID)).hasSize(1);
        assertThat(ticketRepository.findByUser_EmailIgnoreCaseAndStatus(
                "andrea@pulsepass.co", TicketStatus.CANCELLED)).isEmpty();
    }

    @Test
    @DisplayName("FR-TKT-007: solo se recuperan los tickets PAID del evento solicitado")
    void findsPaidTicketsByEventCode() {
        List<Ticket> paid = ticketRepository.findByEvent_EventCodeAndStatus("CMF-2026", TicketStatus.PAID);

        assertThat(paid).extracting(Ticket::getTicketCode)
                .containsExactlyInAnyOrder("TCK-0001", "TCK-0002");
        assertThat(paid).allMatch(t -> t.getStatus() == TicketStatus.PAID);
    }

    @Test
    @DisplayName("AC-008 / FR-TKT-008 / QT-008: el conteo de ventas solo considera PAID")
    void countsOnlyPaidTickets() {
        assertThat(ticketRepository.countPaidTicketsByEventCode("CMF-2026")).isEqualTo(2L);
        assertThat(ticketRepository.sumPaidRevenueByEventCode("CMF-2026"))
                .isEqualByComparingTo(new BigDecimal("370000.00"));
    }

    @Test
    @DisplayName("BR-007 / NFR-008: el precio se guarda como NUMERIC, sin perdida decimal")
    void priceKeepsDecimalPrecision() {
        Ticket ticket = ticketRepository.findByTicketCode("TCK-0002").orElseThrow();

        assertThat(ticket.getPrice().scale()).isEqualTo(2);
        assertThat(ticket.getPrice()).isEqualByComparingTo("120000.00");
    }
}
