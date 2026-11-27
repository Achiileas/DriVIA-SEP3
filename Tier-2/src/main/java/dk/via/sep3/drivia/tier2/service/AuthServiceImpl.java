
package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.request.RegisterRequest;
import dk.via.sep3.drivia.tier2.dto.request.LoginRequest;
import dk.via.sep3.drivia.tier2.dto.response.AuthResponse;

import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserServiceGrpc.UserServiceBlockingStub userStub;
  private final JwtService jwtService;

  public AuthServiceImpl(
      UserServiceGrpc.UserServiceBlockingStub userStub,
      JwtService jwtService) {
    this.userStub = userStub;
    this.jwtService = jwtService;
  }

  @Override
  public AuthResponse register(RegisterRequest request) {

    User user = userStub.register(
        dk.via.sep3.drivia.grpc.RegisterRequest.newBuilder()
            .setName(request.name())
            .setEmail(request.email())
            .setPassword(request.password())
            .setPhoneNumber(request.phoneNumber())
            .build()
    );

    return toResponse(user);
  }

  @Override
  public AuthResponse login(LoginRequest request) {

    User user = userStub.login(
        dk.via.sep3.drivia.grpc.LoginRequest.newBuilder()
            .setEmail(request.email())
            .setPassword(request.password())
            .build()
    );

    return toResponse(user);
  }

  private AuthResponse toResponse(User user) {

    String token = jwtService.generateToken(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole()
    );

    return new AuthResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole(),
        token
    );
  }
}
