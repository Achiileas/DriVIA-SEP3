package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import dk.via.sep3.drivia.tier2.grpc.CarDataClient;
import org.springframework.stereotype.Service;

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
            vehicle.getModel(),
            vehicle.getCategory(),
            vehicle.getLocationName()
        ))
        .toList();
  }
}