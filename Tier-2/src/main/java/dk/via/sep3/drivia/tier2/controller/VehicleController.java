package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.response.VehicleResponse;
import dk.via.sep3.drivia.tier2.service.VehicleService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dk.via.sep3.drivia.tier2.dto.request.VehicleCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleUpdateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleStatusRequest;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController
{

  private final VehicleService vehicleService;

  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  @GetMapping
  public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
    try {
      return ResponseEntity.ok(vehicleService.getAllVehicles());
    } catch (StatusRuntimeException e) {
      // UNAVAILABLE means Tier 3 is not running or cannot be reached
      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable int id) {
    try {
      return ResponseEntity.ok(vehicleService.getVehicleById(id));
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
  public ResponseEntity<List<VehicleResponse>> searchVehicles(
          @RequestParam(required = false) String model,
          @RequestParam(required = false) String category,
          @RequestParam(required = false) String gearbox,
          @RequestParam(defaultValue = "0") int locationId) {

    try {
      return ResponseEntity.ok(
              vehicleService.searchVehicles(model, category, gearbox, locationId)
      );
    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/available")
  public ResponseEntity<List<VehicleResponse>> getAvailableVehicles(
          @RequestParam int locationId,
          @RequestParam LocalDate pickupDate,
          @RequestParam LocalDate returnDate) {

    try {
      return ResponseEntity.ok(
              vehicleService.getAvailableVehicles(locationId, pickupDate, returnDate)
      );
    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping
  public ResponseEntity<VehicleResponse> addVehicle(
      @RequestBody VehicleCreateRequest request) {

    try {
      VehicleResponse vehicle = vehicleService.addVehicle(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }

      if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable int id,
      @RequestBody VehicleUpdateRequest request) {

    try {
      return ResponseEntity.ok(vehicleService.updateVehicle(id, request));

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        return ResponseEntity.notFound().build();
      }

      if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
        return ResponseEntity.badRequest().build();
      }

      if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<VehicleResponse> updateVehicleStatus(
      @PathVariable int id,
      @RequestBody VehicleStatusRequest request) {

    try {
      return ResponseEntity.ok(
          vehicleService.updateVehicleStatus(id, request.status())
      );

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        return ResponseEntity.notFound().build();
      }

      if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removeVehicle(@PathVariable int id) {

    try {
      vehicleService.removeVehicle(id);
      return ResponseEntity.noContent().build();

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
      }

      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        return ResponseEntity.notFound().build();
      }

      if (e.getStatus().getCode() == Status.Code.FAILED_PRECONDITION) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }

      return ResponseEntity.internalServerError().build();
    }
  }
}