package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.ReservationDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    ReservationDTO createReservation(Long customerId, Long vehicleId, LocalDate pickupDate, LocalDate returnDate);

    ReservationDTO getReservation(Long id);

    List<ReservationDTO> getAllReservations();

    List<ReservationDTO> getCustomerReservations(Long customerId);

    ReservationDTO modifyReservation(Long id, Long vehicleId, LocalDate pickupDate, LocalDate returnDate);

    ReservationDTO cancelReservation(Long id);

    ReservationDTO approveReservation(Long id);

    ReservationDTO pickupReservation(Long id);

    ReservationDTO returnReservation(Long id);
}
