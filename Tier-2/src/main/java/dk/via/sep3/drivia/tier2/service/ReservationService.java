package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {
    List<ReservationResponse> getAllReservations();

    ReservationResponse getReservation(int id);

    ReservationResponse approveReservation(int id);

    ReservationResponse pickupReservation(int id);

    ReservationResponse returnReservation(int id);
}
