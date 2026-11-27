
package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.request.ReservationCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.ReservationUpdateRequest;
import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import dk.via.sep3.drivia.tier2.service.ReservationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/reservations")
public class CustomerReservationController {

  private final ReservationService reservationService;

  public CustomerReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @PostMapping
  public ResponseEntity<ReservationResponse> createReservation(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody ReservationCreateRequest request) {

    int customerId = getCustomerId(jwt);

    ReservationResponse reservation = reservationService.createReservation(
        customerId,
        request.vehicleId(),
        request.pickupDate(),
        request.returnDate()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
  }

  @GetMapping
  public List<ReservationResponse> getCustomerReservations(
      @AuthenticationPrincipal Jwt jwt) {

    int customerId = getCustomerId(jwt);

    return reservationService.getCustomerReservations(customerId);
  }

  @GetMapping("/{id}")
  public ReservationResponse getReservation(
      @PathVariable int id,
      @AuthenticationPrincipal Jwt jwt) {

    int customerId = getCustomerId(jwt);

    return reservationService.getCustomerReservation(id, customerId);
  }

  @PutMapping("/{id}")
  public ReservationResponse modifyReservation(
      @PathVariable int id,
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody ReservationUpdateRequest request) {

    int customerId = getCustomerId(jwt);

    return reservationService.modifyReservation(
        id,
        customerId,
        request.vehicleId(),
        request.pickupDate(),
        request.returnDate()
    );
  }

  @PutMapping("/{id}/cancel")
  public ReservationResponse cancelReservation(
      @PathVariable int id,
      @AuthenticationPrincipal Jwt jwt) {

    int customerId = getCustomerId(jwt);

    return reservationService.cancelCustomerReservation(id, customerId);
  }

  private int getCustomerId(Jwt jwt) {
    Number userId = jwt.getClaim("userId");
    return userId.intValue();
  }

  // Handle gRPC exceptions
  @ExceptionHandler(StatusRuntimeException.class)
  public ResponseEntity<Void> handleGrpcException(StatusRuntimeException exception) {

    Status.Code code = exception.getStatus().getCode();

    HttpStatus httpStatus = switch (code) {
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
      case ALREADY_EXISTS, FAILED_PRECONDITION -> HttpStatus.CONFLICT;
      case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
      case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };

    return ResponseEntity.status(httpStatus).build();
  }
}
