
package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.request.VehicleCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleUpdateRequest;
import dk.via.sep3.drivia.tier2.dto.response.VehicleResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleService {

  List<VehicleResponse> getAllVehicles();

  VehicleResponse getVehicleById(int id);

  List<VehicleResponse> searchVehicles(
      String model,
      String category,
      String gearbox,
      int locationId
  );

  List<VehicleResponse> getAvailableVehicles(
      int locationId,
      LocalDateTime pickupDate,
      LocalDateTime returnDate
  );

  VehicleResponse addVehicle(VehicleCreateRequest request);

  VehicleResponse updateVehicle(int id, VehicleUpdateRequest request);

  VehicleResponse updateVehicleStatus(int id, String status);

  void removeVehicle(int id);
}
