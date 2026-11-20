
package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.request.ReservationCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.ReservationUpdateRequest;
import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import dk.via.sep3.drivia.tier2.service.ReservationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
      @RequestBody ReservationCreateRequest request) {

    ReservationResponse reservation = reservationService.createReservation(
        request.customerId(),
        request.vehicleId(),
        request.pickupDate(),
        request.returnDate()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
  }

  @GetMapping
  public List<ReservationResponse> getCustomerReservations(
      @RequestParam int customerId) {

    return reservationService.getCustomerReservations(customerId);
  }

  @GetMapping("/{id}")
  public ReservationResponse getReservation(@PathVariable int id) {
    return reservationService.getReservation(id);
  }

  @PutMapping("/{id}")
  public ReservationResponse modifyReservation(
      @PathVariable int id,
      @RequestBody ReservationUpdateRequest request) {

    return reservationService.modifyReservation(
        id,
        request.vehicleId(),
        request.pickupDate(),
        request.returnDate()
    );
  }

  @PutMapping("/{id}/cancel")
  public ReservationResponse cancelReservation(@PathVariable int id) {
    return reservationService.cancelReservation(id);
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
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };

    return ResponseEntity.status(httpStatus).build();
  }
}
