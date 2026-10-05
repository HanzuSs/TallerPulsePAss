package com.pulsepass.repository;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.support.AbstractPostgresIT;
import com.pulsepass.support.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Relacion User 1:1 UserProfile.
 * FR-USR-001..FR-USR-004 / AC-004 / QT-004, QT-009.
 */
class UserProfileIT extends AbstractPostgresIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("FR-USR-002 / QT-009: username y email son unicos en PostgreSQL")
    void rejectsDuplicateIdentity() {
        userRepository.saveAndFlush(TestData.user("andrea", "andrea@pulsepass.co"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(TestData.user("andrea", "otra@pulsepass.co")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("FR-USR-004 / QT-004: el perfil se recupera navegando la relacion 1:1")
    void persistsUserWithProfile() {
        User user = TestData.user("carlos", "carlos@pulsepass.co");
        user.setProfile(TestData.profile("Carlos", "Mendoza"));
        userRepository.saveAndFlush(user);

        UserProfile profile = userProfileRepository.findByUser_Username("carlos").orElseThrow();

        assertThat(profile.getFirstName()).isEqualTo("Carlos");
        assertThat(profile.getCity()).isEqualTo("Santa Marta");
        assertThat(profile.getUser().getEmail()).isEqualTo("carlos@pulsepass.co");
    }

    @Test
    @DisplayName("AC-004 / BR-004: la BD impide un segundo perfil para el mismo usuario")
    void rejectsSecondProfileForSameUser() {
        User user = TestData.user("laura", "laura@pulsepass.co");
        user.setProfile(TestData.profile("Laura", "Gomez"));
        User saved = userRepository.saveAndFlush(user);

        UserProfile second = TestData.profile("Laura", "Duplicada");
        second.setUser(saved);

        assertThatThrownBy(() -> userProfileRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Busqueda de usuario por email ignorando mayusculas")
    void findsUserByEmailIgnoringCase() {
        userRepository.saveAndFlush(TestData.user("miguel", "miguel@pulsepass.co"));

        assertThat(userRepository.findByEmailIgnoreCase("MIGUEL@PULSEPASS.CO")).isPresent();
    }
}
