package dk.via.sep3.drivia.tier2.dto.request;

import java.time.LocalDate;

public record ReservationUpdateRequest(
    int vehicleId,
    LocalDate pickupDate,
    LocalDate returnDate
) {}