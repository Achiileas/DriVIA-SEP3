package dk.via.sep3.drivia.tier3.vehicle.dto;

import dk.via.sep3.drivia.tier3.vehicle.entity.VehicleStatus;
import java.time.LocalDate;

public record VehicleDTO(Long id,
                         String registrationNo,
                         String model,
                         LocalDate productionDate,
                         int horsepower,
                         String category,
                         String gearbox,
                         int pricePerDay,
                         VehicleStatus status,
                         Long locationId,
                         String locationName) {
}
