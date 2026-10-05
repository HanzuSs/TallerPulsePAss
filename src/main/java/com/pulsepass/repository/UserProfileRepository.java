package com.pulsepass.repository;

import com.pulsepass.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** FR-USR-003, FR-USR-004. */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** Query Method navegando la relacion 1:1. */
    Optional<UserProfile> findByUser_Username(String username);

    Optional<UserProfile> findByUser_EmailIgnoreCase(String email);
}
