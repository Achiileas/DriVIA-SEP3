package dk.via.sep3.drivia.tier3.dto;

import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import java.time.LocalDate;

public record VehicleDTO( int id, String registrationNo, String model,
                         LocalDate productionDate, int horsepower, String category,
                         String gearbox, int pricePerDay, VehicleStatus status, int locationId,
                         String locationName ){
}
