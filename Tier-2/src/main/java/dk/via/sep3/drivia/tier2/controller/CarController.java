package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import dk.via.sep3.drivia.tier2.service.CarService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

  private final CarService carService;

  public CarController(CarService carService) {
    this.carService = carService;
  }

  @GetMapping
  public ResponseEntity<List<CarResponse>> getAllCars() {
    try {
      return ResponseEntity.ok(carService.getAllCars());
    } catch (StatusRuntimeException e) {
      // UNAVAILABLE means Tier 3 is not running or cannot be reached
      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<CarResponse> getCarById(@PathVariable int id) {
    try {
      return ResponseEntity.ok(carService.getCarById(id));
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/search")
  public ResponseEntity<List<CarResponse>> searchCars(
          @RequestParam(required = false) String model,
          @RequestParam(required = false) String category,
          @RequestParam(required = false) String gearbox,
          @RequestParam(defaultValue = "0") int locationId) {

    try {
      return ResponseEntity.ok(
              carService.searchCars(model, category, gearbox, locationId)
      );
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/available")
  public ResponseEntity<List<CarResponse>> getAvailableCars(
          @RequestParam int locationId,
          @RequestParam LocalDate pickupDate,
          @RequestParam LocalDate returnDate) {

    try {
      return ResponseEntity.ok(
              carService.getAvailableCars(locationId, pickupDate, returnDate)
      );
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

}