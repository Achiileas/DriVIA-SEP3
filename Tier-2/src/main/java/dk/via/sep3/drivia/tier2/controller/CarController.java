package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import dk.via.sep3.drivia.tier2.service.CarService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

  private final CarService carService;

  public CarController(CarService carService) {
    this.carService = carService;
  }

  @GetMapping
  public List<CarResponse> getAllCars() {
    return carService.getAllCars();
  }
}