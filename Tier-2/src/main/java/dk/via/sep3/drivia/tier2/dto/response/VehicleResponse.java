package dk.via.sep3.drivia.tier2.dto.response;

import java.time.LocalDate;

public record VehicleResponse(
        int id,
        String registrationNo,
        String model,
        LocalDate productionDate,
        int horsepower,
        String category,
        String gearbox,
        int pricePerDay,
        String status,
        int locationId,
        String locationName
) {
}