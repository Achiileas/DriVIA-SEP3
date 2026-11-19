package dk.via.sep3.drivia.tier2.dto.response;

import java.time.LocalDate;

public record ReservationResponse(
        int id,
        int customerId,
        int vehicleId,
        String vehicleModel,
        int locationId,
        String locationName,
        LocalDate pickupDate,
        LocalDate returnDate,
        String status,
        int totalPrice

) {
}
