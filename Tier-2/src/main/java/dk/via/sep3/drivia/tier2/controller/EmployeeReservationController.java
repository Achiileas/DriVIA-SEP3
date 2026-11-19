package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import dk.via.sep3.drivia.tier2.service.ReservationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/reservations")
public class EmployeeReservationController {

    private final ReservationService reservationService;

    public EmployeeReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable int id) {
        return reservationService.getReservation(id);
    }

    @PutMapping("/{id}/approve")
    public ReservationResponse approveReservation(@PathVariable int id) {
        return reservationService.approveReservation(id);
    }

    @PutMapping("/{id}/pickup")
    public ReservationResponse pickupReservation(@PathVariable int id) {
        return reservationService.pickupReservation(id);
    }

    @PutMapping("/{id}/return")
    public ReservationResponse returnReservation(@PathVariable int id) {
        return reservationService.returnReservation(id);
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