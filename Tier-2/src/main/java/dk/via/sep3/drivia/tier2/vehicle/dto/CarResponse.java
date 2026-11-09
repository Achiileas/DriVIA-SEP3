package dk.via.sep3.drivia.tier2.vehicle.dto;

public record CarResponse(
    long id,
    String model,
    String category,
    String location
) {}