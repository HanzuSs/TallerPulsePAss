package com.pulsepass.service;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthenticatedUserResponse currentUser(Long userId);
}
