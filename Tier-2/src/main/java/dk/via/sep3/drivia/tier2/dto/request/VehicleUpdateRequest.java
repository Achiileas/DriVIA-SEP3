package dk.via.sep3.drivia.tier2.dto.request;

import java.time.LocalDate;

public record VehicleUpdateRequest(
    String registrationNo,
    String model,
    LocalDate productionDate,
    int horsepower,
    String category,
    String gearbox,
    int pricePerDay,
    int locationId
) {}