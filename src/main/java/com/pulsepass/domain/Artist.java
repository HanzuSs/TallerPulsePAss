package com.pulsepass.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Artista que participa en eventos.
 * FR-ART-001..FR-ART-004 / BR-003.
 */
@Entity
@Table(
        name = "artists",
        uniqueConstraints = @UniqueConstraint(name = "uk_artists_stage_name", columnNames = "stage_name")
)
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FR-ART-002: identificador de negocio unico. */
    @NotBlank
    @Column(name = "stage_name", nullable = false, length = 120)
    private String stageName;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "genre", length = 80)
    private String genre;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Lado inverso de la N:M: el dueno de la relacion es Event. */
    @ManyToMany(mappedBy = "artists")
    private Set<Event> events = new LinkedHashSet<>();

    protected Artist() {
        // requerido por JPA
    }

    public Artist(String stageName, String country, String genre) {
        this.stageName = stageName;
        this.country = country;
        this.genre = genre;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Event> getEvents() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Artist other)) {
            return false;
        }
        return stageName != null && stageName.equals(other.stageName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stageName);
    }

    @Override
    public String toString() {
        return "Artist{stageName='" + stageName + "'}";
    }
}
