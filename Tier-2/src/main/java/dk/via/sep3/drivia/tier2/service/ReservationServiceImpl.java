
package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationServiceGrpc.ReservationServiceBlockingStub reservationStub;

    public ReservationServiceImpl(
        ReservationServiceGrpc.ReservationServiceBlockingStub reservationStub) {
        this.reservationStub = reservationStub;
    }

    // Employee operations

    @Override
    public List<ReservationResponse> getAllReservations() {
        return reservationStub
            .getAllReservations(GetAllReservationsRequest.newBuilder().build())
            .getReservationsList()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public ReservationResponse getReservation(int id) {
        Reservation reservation = reservationStub.getReservation(
            ReservationIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse approveReservation(int id) {
        Reservation reservation = reservationStub.approveReservation(
            ReservationIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse pickupReservation(int id) {
        Reservation reservation = reservationStub.pickupReservation(
            ReservationIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse returnReservation(int id) {
        Reservation reservation = reservationStub.returnReservation(
            ReservationIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(reservation);
    }

    // Customer operations

    @Override
    public ReservationResponse createReservation(
        int customerId,
        int vehicleId,
        LocalDateTime pickupDate,
        LocalDateTime returnDate) {

        Reservation reservation = reservationStub.createReservation(
            CreateReservationRequest.newBuilder()
                .setCustomerId(customerId)
                .setVehicleId(vehicleId)
                .setPickupDate(pickupDate.toString())
                .setReturnDate(returnDate.toString())
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse getCustomerReservation(int id, int customerId) {
        Reservation reservation = reservationStub.getCustomerReservation(
            CustomerReservationIdRequest.newBuilder()
                .setId(id)
                .setCustomerId(customerId)
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getCustomerReservations(int customerId) {
        return reservationStub
            .getCustomerReservations(
                CustomerIdRequest.newBuilder()
                    .setCustomerId(customerId)
                    .build()
            )
            .getReservationsList()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public ReservationResponse modifyReservation(
        int id,
        int customerId,
        int vehicleId,
        LocalDateTime pickupDate,
        LocalDateTime returnDate) {

        Reservation reservation = reservationStub.modifyReservation(
            ModifyReservationRequest.newBuilder()
                .setId(id)
                .setCustomerId(customerId)
                .setVehicleId(vehicleId)
                .setPickupDate(pickupDate.toString())
                .setReturnDate(returnDate.toString())
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse cancelCustomerReservation(int id, int customerId) {
        Reservation reservation = reservationStub.cancelCustomerReservation(
            CustomerReservationIdRequest.newBuilder()
                .setId(id)
                .setCustomerId(customerId)
                .build()
        );

        return toResponse(reservation);
    }

    // Convert gRPC Reservation to REST response

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getCustomerId(),
            reservation.getVehicleId(),
            reservation.getVehicleModel(),
            reservation.getLocationId(),
            reservation.getLocationName(),
            LocalDateTime.parse(reservation.getPickupDate()),
            LocalDateTime.parse(reservation.getReturnDate()),
            reservation.getStatus(),
            new BigDecimal(reservation.getTotalPrice())
        );
    }
}