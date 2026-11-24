package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {


    @Mock
    private ReservationServiceGrpc.ReservationServiceBlockingStub reservationStub;

    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(reservationStub);
    }

    // Helper method
    private Reservation createReservation(int id, String status) {
        return Reservation.newBuilder()
                .setId(id)
                .setCustomerId(1)
                .setVehicleId(2)
                .setVehicleModel("Toyota Corolla")
                .setLocationId(1)
                .setLocationName("Horsens")
                .setPickupDate("2026-12-01T10:00")
                .setReturnDate("2026-12-05T10:00")
                .setStatus(status)
                .setTotalPrice(2000)
                .build();
    }

    // Z - Zero
    @Test
    void getAllReservationsReturnsEmptyListWhenNoReservationsExist() {
        when(reservationStub.getAllReservations(any(GetAllReservationsRequest.class)))
                .thenReturn(ReservationList.newBuilder().build());

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertTrue(result.isEmpty());
    }

    // O - One
    @Test
    void getReservationReturnsOneReservation() {
        Reservation reservation = createReservation(1, "PENDING");

        when(reservationStub.getReservation(any(ReservationIdRequest.class)))
                .thenReturn(reservation);

        ReservationResponse result = reservationService.getReservation(1);

        assertEquals(1, result.id());
        assertEquals(1, result.customerId());
        assertEquals("Toyota Corolla", result.vehicleModel());
        assertEquals("Horsens", result.locationName());
        assertEquals("PENDING", result.status());
    }

    // M - Many
    @Test
    void getAllReservationsReturnsMultipleReservations() {
        ReservationList reservations = ReservationList.newBuilder()
                .addReservations(createReservation(1, "PENDING"))
                .addReservations(createReservation(2, "APPROVED"))
                .addReservations(createReservation(3, "ACTIVE"))
                .build();

        when(reservationStub.getAllReservations(any(GetAllReservationsRequest.class)))
                .thenReturn(reservations);

        List<ReservationResponse> result = reservationService.getAllReservations();

        assertEquals(3, result.size());
        assertEquals("PENDING", result.get(0).status());
        assertEquals("APPROVED", result.get(1).status());
        assertEquals("ACTIVE", result.get(2).status());
    }

    // B - Boundary
    @Test
    void getReservationSendsCorrectIdToGrpc() {
        when(reservationStub.getReservation(any(ReservationIdRequest.class)))
                .thenReturn(createReservation(10, "PENDING"));

        reservationService.getReservation(10);

        verify(reservationStub).getReservation(argThat(request ->
                request.getId() == 10
        ));
    }

    // I - Interface
    @Test
    void reservationResponseContainsCorrectDatesAndPrice() {
        Reservation reservation = createReservation(1, "APPROVED");

        when(reservationStub.getReservation(any(ReservationIdRequest.class)))
                .thenReturn(reservation);

        ReservationResponse result = reservationService.getReservation(1);

        assertEquals(LocalDateTime.of(2026, 12, 1, 10, 0), result.pickupDate());
        assertEquals(LocalDateTime.of(2026, 12, 5, 10, 0), result.returnDate());
        assertEquals(2000, result.totalPrice());
    }

    // E - Exceptions
    @Test
    void getAllReservationsPropagatesGrpcException() {
        when(reservationStub.getAllReservations(any(GetAllReservationsRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        assertThrows(StatusRuntimeException.class,
                () -> reservationService.getAllReservations());
    }

    // S - Simple scenarios
    @Test
    void approveReservationReturnsApprovedReservation() {
        Reservation reservation = createReservation(1, "APPROVED");

        when(reservationStub.approveReservation(any(ReservationIdRequest.class)))
                .thenReturn(reservation);

        ReservationResponse result = reservationService.approveReservation(1);

        assertEquals(1, result.id());
        assertEquals("APPROVED", result.status());

        verify(reservationStub).approveReservation(argThat(request ->
                request.getId() == 1
        ));
    }

    @Test
    void pickupReservationReturnsActiveReservation() {
        Reservation reservation = createReservation(1, "ACTIVE");

        when(reservationStub.pickupReservation(any(ReservationIdRequest.class)))
                .thenReturn(reservation);

        ReservationResponse result = reservationService.pickupReservation(1);

        assertEquals("ACTIVE", result.status());

        verify(reservationStub).pickupReservation(argThat(request ->
                request.getId() == 1
        ));
    }

    @Test
    void returnReservationReturnsCompletedReservation() {
        Reservation reservation = createReservation(1, "COMPLETED");

        when(reservationStub.returnReservation(any(ReservationIdRequest.class)))
                .thenReturn(reservation);

        ReservationResponse result = reservationService.returnReservation(1);

        assertEquals("COMPLETED", result.status());

        verify(reservationStub).returnReservation(argThat(request ->
                request.getId() == 1
        ));
    }
}
