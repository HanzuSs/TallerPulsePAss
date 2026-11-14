package com.pulsepass.repository;

import com.pulsepass.domain.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// ...


public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByUser_UsernameIgnoreCaseOrUser_EmailIgnoreCase(String username, String email);

@Query("""
    SELECT c
    FROM UserCredential c
    WHERE LOWER(c.user.username) = LOWER(:identifier)
       OR LOWER(c.user.email) = LOWER(:identifier)
    """)
Optional<UserCredential> findByIdentifier(
        @Param("identifier") String identifier
);}

