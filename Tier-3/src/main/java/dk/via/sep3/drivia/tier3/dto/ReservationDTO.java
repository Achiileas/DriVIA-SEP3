package dk.via.sep3.drivia.tier3.dto;

import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationDTO( int id, int customerId, int vehicleId, String vehicleModel,
                             int locationId, String locationName, LocalDateTime pickupDate,
                             LocalDateTime returnDate, ReservationStatus status, int totalPrice ){
}
