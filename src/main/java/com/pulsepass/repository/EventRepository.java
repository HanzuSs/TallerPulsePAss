package com.pulsepass.repository;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Consultas de cartelera y descubrimiento.
 * FR-EVT-005, FR-VEN-004, FR-ART-004, FR-SRC-001..FR-SRC-003.
 *
 * NFR-007: lo simple se resuelve con Query Methods; lo que necesita JOIN,
 * DISTINCT o varias asociaciones se expresa con JPQL legible.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    // -----------------------------------------------------------------
    // Query Methods
    // -----------------------------------------------------------------

    /** FR-EVT-002: busqueda por identificador de negocio. */
    Optional<Event> findByEventCode(String eventCode);

    /** FR-EVT-005: cartelera de eventos publicados en orden cronologico. */
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    /** FR-VEN-004: navegacion de relacion Event -> Venue -> code. */
    List<Event> findByVenue_Code(String venueCode);

    /** Eventos publicados de una ciudad posteriores a una fecha. */
    List<Event> findByStatusAndVenue_CityIgnoreCaseAndEventDateAfterOrderByEventDateAsc(
            EventStatus status, String city, LocalDateTime fromDate);

    // -----------------------------------------------------------------
    // JPQL
    // -----------------------------------------------------------------

    /**
     * FR-ART-004 / FR-SRC-001: eventos en los que participa un artista.
     * DISTINCT evita duplicados generados por el JOIN sobre la N:M.
     */
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.artists a
            WHERE a.stageName = :stageName
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByArtistStageName(@Param("stageName") String stageName);

    /**
     * FR-SRC-002: eventos de una ciudad en los que participa un artista.
     * Combina dos asociaciones distintas (venue y artists).
     */
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE LOWER(v.city) = LOWER(:city)
              AND LOWER(a.stageName) = LOWER(:stageName)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByCityAndArtist(@Param("city") String city,
                                          @Param("stageName") String stageName);

    /**
     * FR-SRC-003: eventos recomendados.
     * Publicados, posteriores a una fecha, en una ciudad y cuyo artista
     * contenga un texto (case-insensitive). DISTINCT + orden por fecha.
     */
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE e.status = :status
              AND e.eventDate > :fromDate
              AND LOWER(v.city) = LOWER(:city)
              AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
            ORDER BY e.eventDate ASC
            """)
    List<Event> findRecommendedEvents(@Param("status") EventStatus status,
                                      @Param("fromDate") LocalDateTime fromDate,
                                      @Param("city") String city,
                                      @Param("artistText") String artistText);

    /** Sobrecarga de conveniencia: la recomendacion siempre es sobre PUBLISHED. */
    default List<Event> findRecommendedEvents(LocalDateTime fromDate, String city, String artistText) {
        return findRecommendedEvents(EventStatus.PUBLISHED, fromDate, city, artistText);
    }

    /** Conteo de artistas asociados a un evento, util para verificar la N:M. */
    @Query("""
            SELECT COUNT(a)
            FROM Event e
            JOIN e.artists a
            WHERE e.eventCode = :eventCode
            """)
    long countArtistsByEventCode(@Param("eventCode") String eventCode);
}
