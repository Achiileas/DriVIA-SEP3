package dk.via.sep3.drivia.tier3.dto;

import dk.via.sep3.drivia.tier3.entity.ReservationStatus;

import java.time.LocalDate;

public record ReservationDTO(Long id,
                             Long customerId,
                             Long vehicleId,
                             String vehicleModel,
                             Long locationId,
                             String locationName,
                             LocalDate pickupDate,
                             LocalDate returnDate,
                             ReservationStatus status,
                             int totalPrice) {
}
