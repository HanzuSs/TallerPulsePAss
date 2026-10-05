package com.pulsepass.repository;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Consultas de tickets.
 * FR-TKT-002, FR-TKT-006, FR-TKT-007, FR-TKT-008, FR-SRC-004.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // -----------------------------------------------------------------
    // Query Methods
    // -----------------------------------------------------------------

    Optional<Ticket> findByTicketCode(String ticketCode);

    /** FR-TKT-006: navegacion Ticket -> User -> email. */
        @Query("""
          SELECT t
          FROM Ticket t
          WHERE LOWER(t.user.email) = LOWER(:email)
          """)
    List<Ticket> findByUser_EmailIgnoreCase(String email);

    /** FR-TKT-006: variante filtrando ademas por estado. */
        @Query("""
          SELECT t
          FROM Ticket t
          WHERE LOWER(t.user.email) = LOWER(:email)
            AND t.status = :status
          """)
    List<Ticket> findByUser_EmailIgnoreCaseAndStatus(String email, TicketStatus status);

    /**
     * FR-TKT-007: tickets de un evento por estado.
     * Se resuelve como Query Method porque es un filtro directo sobre dos
     * atributos navegables; no hace falta JPQL (NFR-007).
     */
    List<Ticket> findByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);

    /** FR-SRC-004: tickets cuyo evento es posterior a una fecha. */
    List<Ticket> findByEvent_EventDateAfterOrderByEvent_EventDateAsc(LocalDateTime fromDate);

    // -----------------------------------------------------------------
    // JPQL
    // -----------------------------------------------------------------

    /** FR-TKT-008: conteo de ventas (solo PAID participa del conteo). */
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.event e
            WHERE e.eventCode = :eventCode
              AND t.status = com.pulsepass.domain.enums.TicketStatus.PAID
            """)
    long countPaidTicketsByEventCode(@Param("eventCode") String eventCode);

    /** Ingreso acumulado por evento considerando solo tickets pagados. */
    @Query("""
            SELECT COALESCE(SUM(t.price), 0)
            FROM Ticket t
            JOIN t.event e
            WHERE e.eventCode = :eventCode
              AND t.status = com.pulsepass.domain.enums.TicketStatus.PAID
            """)
    java.math.BigDecimal sumPaidRevenueByEventCode(@Param("eventCode") String eventCode);
}
