package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.request.RegisterRequest;
import dk.via.sep3.drivia.tier2.dto.request.LoginRequest;
import dk.via.sep3.drivia.tier2.dto.response.AuthResponse;
import dk.via.sep3.drivia.tier2.service.AuthService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @RequestBody RegisterRequest request) {

    AuthResponse response = authService.register(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @RequestBody LoginRequest request) {

    AuthResponse response = authService.login(request);

    return ResponseEntity.ok(response);
  }

  // Handle gRPC exceptions
  @ExceptionHandler(StatusRuntimeException.class)
  public ResponseEntity<Void> handleGrpcException(
      StatusRuntimeException exception) {

    Status.Code code = exception.getStatus().getCode();

    HttpStatus httpStatus = switch (code) {
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
      case ALREADY_EXISTS, FAILED_PRECONDITION -> HttpStatus.CONFLICT;
      case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
      case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
      case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };

    return ResponseEntity.status(httpStatus).build();
  }
}
