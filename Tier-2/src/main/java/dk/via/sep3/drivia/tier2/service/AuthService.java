package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.request.RegisterRequest;
import dk.via.sep3.drivia.tier2.dto.request.LoginRequest;
import dk.via.sep3.drivia.tier2.dto.response.AuthResponse;

public interface AuthService {

  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);
}
