package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationServiceGrpc.ReservationServiceBlockingStub reservationStub;

    public ReservationServiceImpl(
            ReservationServiceGrpc.ReservationServiceBlockingStub reservationStub) {
        this.reservationStub = reservationStub;
    }

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

    @Override
    public ReservationResponse createReservation(
        int customerId,
        int vehicleId,
        LocalDate pickupDate,
        LocalDate returnDate) {

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
        int vehicleId,
        LocalDate pickupDate,
        LocalDate returnDate) {

        Reservation reservation = reservationStub.modifyReservation(
            ModifyReservationRequest.newBuilder()
                .setId(id)
                .setVehicleId(vehicleId)
                .setPickupDate(pickupDate.toString())
                .setReturnDate(returnDate.toString())
                .build()
        );

        return toResponse(reservation);
    }

    @Override
    public ReservationResponse cancelReservation(int id) {
        Reservation reservation = reservationStub.cancelReservation(
            ReservationIdRequest.newBuilder()
                .setId(id)
                .build()
        );

        return toResponse(reservation);
    }


    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerId(),
                reservation.getVehicleId(),
                reservation.getVehicleModel(),
                reservation.getLocationId(),
                reservation.getLocationName(),
                LocalDate.parse(reservation.getPickupDate()),
                LocalDate.parse(reservation.getReturnDate()),
                reservation.getStatus(),
                reservation.getTotalPrice()
        );
    }
}
