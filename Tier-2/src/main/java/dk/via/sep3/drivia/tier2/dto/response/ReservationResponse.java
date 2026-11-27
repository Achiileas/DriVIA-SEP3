package dk.via.sep3.drivia.tier2.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(
    int id,
    int customerId,
    int vehicleId,
    String vehicleModel,
    int locationId,
    String locationName,
    LocalDateTime pickupDate,
    LocalDateTime returnDate,
    String status,
    BigDecimal totalPrice
) {
}