
package dk.via.sep3.drivia.tier2.dto.request;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
    int customerId,
    int vehicleId,
    LocalDateTime pickupDate,
    LocalDateTime returnDate
) {}
