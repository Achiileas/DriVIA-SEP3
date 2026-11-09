package dk.via.sep3.drivia.tier2.vehicle.dto;

import java.time.LocalDate;

public record CarResponse(
        long id,
        String registrationNo,
        String model,
        LocalDate productionDate,
        int horsepower,
        String category,
        String gearbox,
        int pricePerDay,
        String status,
        Long locationId,
        String locationName
) {
}