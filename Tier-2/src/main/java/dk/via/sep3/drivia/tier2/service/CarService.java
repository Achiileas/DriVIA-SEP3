package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import java.util.List;

public interface CarService{

  List<CarResponse> getAllCars();
}
