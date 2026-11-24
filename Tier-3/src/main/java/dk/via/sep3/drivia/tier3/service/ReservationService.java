package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.ReservationDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService{

    ReservationDTO createReservation( int customerId, int vehicleId, LocalDateTime pickupDate, LocalDateTime returnDate );

    ReservationDTO getReservation( int id );

    List<ReservationDTO> getAllReservations();

    List<ReservationDTO> getCustomerReservations( int customerId );

    ReservationDTO modifyReservation( int id, int vehicleId, LocalDateTime pickupDate, LocalDateTime returnDate );

    ReservationDTO cancelReservation( int id );

    ReservationDTO approveReservation( int id );

    ReservationDTO pickupReservation( int id );

    ReservationDTO returnReservation( int id );
}
