package com.pulsepass.support;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.domain.Venue;
import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Datos de referencia del escenario descrito en la seccion 16 del PRD.
 * Las pruebas construyen objetos nuevos en cada test para no depender
 * del orden de ejecucion.
 */
public final class TestData {

    public static final LocalDateTime FESTIVAL_DATE = LocalDateTime.of(2026, 3, 14, 20, 0);

    private TestData() {
    }

    public static Venue marinaConventionCenter() {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta",
                "Av. del Ferrocarril 10-25", 5000);
    }

    public static Venue venue(String code, String city, int capacity) {
        return new Venue(code, "Venue " + code, city, "Direccion " + code, capacity);
    }

    public static Event caribbeanMusicFest(Venue venue) {
        return new Event("CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, FESTIVAL_DATE, venue);
    }

    public static Event event(String code, EventStatus status, LocalDateTime date, Venue venue) {
        return new Event(code, "Evento " + code, EventCategory.MUSIC, status, date, venue);
    }

    public static Artist artist(String stageName) {
        return new Artist(stageName, "Colombia", "Electronica");
    }

    public static User user(String username, String email) {
        return new User(username, email);
    }

    public static UserProfile profile(String firstName, String lastName) {
        return new UserProfile(firstName, lastName, "+57 300 000 0000", "Santa Marta",
                LocalDate.of(1998, 5, 20));
    }

    public static Ticket ticket(String code,
                                TicketType type,
                                String price,
                                TicketStatus status,
                                User user,
                                Event event) {
        return new Ticket(code, type, new BigDecimal(price), status,
                LocalDateTime.of(2026, 1, 10, 9, 30), user, event);
    }
}
