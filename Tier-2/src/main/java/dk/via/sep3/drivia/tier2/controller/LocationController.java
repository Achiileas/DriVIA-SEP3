package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.request.LocationRequest;
import dk.via.sep3.drivia.tier2.dto.response.LocationResponse;
import dk.via.sep3.drivia.tier2.service.LocationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;
    public LocationController( LocationService locationService ){
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations(){
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocation( @PathVariable int id ){
        return ResponseEntity.ok(locationService.getLocation(id));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> addLocation( @RequestBody LocationRequest request ){
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.addLocation(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation( @PathVariable int id, @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation( @PathVariable int id ){
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    // Turns the gRPC error from Tier 3 into the matching HTTP status for every endpoint above
    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Void> handleGrpcException(StatusRuntimeException exception) {

        Status.Code code = exception.getStatus().getCode();

        HttpStatus httpStatus = switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;                       // unknown location id
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;              // missing name or address
            case FAILED_PRECONDITION -> HttpStatus.CONFLICT;              // location still has cars or reservations
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;           // Tier 3 is not running
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(httpStatus).build();
    }
}
