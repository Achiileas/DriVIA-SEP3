package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    // Employee operations
    List<ReservationResponse> getAllReservations();

    ReservationResponse getReservation(int id);

    ReservationResponse approveReservation(int id);

    ReservationResponse pickupReservation(int id);

    ReservationResponse returnReservation(int id);

    // Customer operations
    ReservationResponse createReservation(
        int customerId,
        int vehicleId,
        LocalDate pickupDate,
        LocalDate returnDate
    );

    List<ReservationResponse> getCustomerReservations(int customerId);

    ReservationResponse modifyReservation(
        int id,
        int vehicleId,
        LocalDate pickupDate,
        LocalDate returnDate
    );

    ReservationResponse cancelReservation(int id);
}
