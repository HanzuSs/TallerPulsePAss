package com.pulsepass.service.impl;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.AuthService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserCredentialRepository credentialRepository, UserRepository userRepository,
                           UserRoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.credentialRepository = credentialRepository; this.userRepository = userRepository;
        this.roleRepository = roleRepository; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
    }

    @Override @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserCredential credential = credentialRepository
                .findByUser_UsernameIgnoreCaseOrUser_EmailIgnoreCase(request.identifier(), request.identifier())
                .filter(c -> c.getUser().isActive())
                .filter(c -> passwordEncoder.matches(request.password(), c.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid identifier or password"));
        return new AuthResponse(jwtService.generateToken(credential.getUserId()), "Bearer", jwtService.getExpirationSeconds());
    }

    @Override @Transactional(readOnly = true)
    public AuthenticatedUserResponse currentUser(Long userId) {
        User user = userRepository.findById(userId).filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid authentication"));
        Set<String> roles = roleRepository.findByUser_Id(userId).stream()
                .map(r -> "ROLE_" + r.getRole().name()).collect(Collectors.toUnmodifiableSet());
        return new AuthenticatedUserResponse(user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
