package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import dk.via.sep3.drivia.tier2.grpc.CarDataClient;
import dk.via.sep3.drivia.tier3.vehicle.entity.VehicleStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

  private final CarDataClient carDataClient;

  public CarService(CarDataClient carDataClient) {
    this.carDataClient = carDataClient;
  }

  public List<CarResponse> getAllCars() {
    return carDataClient.getAllVehicles()
        .getVehiclesList()
        .stream()
        .map(vehicle -> new CarResponse(
            vehicle.getId(),
            vehicle.getRegistrationNo(),
            vehicle.getModel(),
            LocalDate.parse(vehicle.getProductionDate()),
            vehicle.getHorsepower(),
            vehicle.getCategory(),
            vehicle.getGearbox(),
            vehicle.getPricePerDay(),
            VehicleStatus.valueOf(vehicle.getStatus()),
            vehicle.getLocationId(),
            vehicle.getLocationName()
        ))
        .toList();
  }
}
