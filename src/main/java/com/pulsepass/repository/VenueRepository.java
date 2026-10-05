package com.pulsepass.repository;

import com.pulsepass.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** FR-VEN-001, FR-VEN-002. */
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /** Query Method: busqueda por identificador de negocio. */
    Optional<Venue> findByCode(String code);

    boolean existsByCode(String code);
}
