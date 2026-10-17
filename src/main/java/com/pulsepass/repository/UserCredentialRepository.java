package com.pulsepass.repository;

import com.pulsepass.domain.UserCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByUser_UsernameIgnoreCaseOrUser_EmailIgnoreCase(String username, String email);
}
