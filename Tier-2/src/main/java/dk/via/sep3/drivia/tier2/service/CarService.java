package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.CarResponse;

import java.time.LocalDate;
import java.util.List;

public interface CarService{

  List<CarResponse> getAllCars();
  CarResponse getCarById(int id);

  List<CarResponse> searchCars(
          String model,
          String category,
          String gearbox,
          int locationId
  );

  List<CarResponse> getAvailableCars(
          int locationId,
          LocalDate pickupDate,
          LocalDate returnDate
  );


}
