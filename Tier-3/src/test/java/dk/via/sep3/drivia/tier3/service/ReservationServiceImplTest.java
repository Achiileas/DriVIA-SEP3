package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.ReservationDTO;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.entity.Reservation;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import dk.via.sep3.drivia.tier3.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// White-box tests for ReservationServiceImpl, grouped by ZOMBIES
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private ReservationServiceImpl reservationService;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        // The repositories are injected through the constructor, so mocks can be passed in
        reservationService = new ReservationServiceImpl(reservationRepository, vehicleRepository);

        Location horsens = new Location("Horsens", "Banegaardsgade 2");
        ReflectionTestUtils.setField(horsens, "id", 1);
        vehicle = new Vehicle("AB12345", "Toyota Yaris", LocalDate.of(2021, 3, 15), 92,
                "Small", "Manual", 350, horsens);
        ReflectionTestUtils.setField(vehicle, "id", 1);
    }

    // ---------- helpers ----------

    private void vehicleCanBeBooked() {
        when(vehicleRepository.findByIdForUpdate(1)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.existsOverlapping(anyInt(), anyInt(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(call -> call.getArgument(0));
    }

    private Reservation reservationWithStatus(ReservationStatus status, LocalDate pickupDate) {
        Reservation reservation = new Reservation(7, vehicle, pickupDate, pickupDate.plusDays(2), 700);
        ReflectionTestUtils.setField(reservation, "id", 5);
        reservation.setStatus(status);
        return reservation;
    }

    private void reservationExists(Reservation reservation) {
        when(reservationRepository.findById(5)).thenReturn(Optional.of(reservation));
    }

    private void saveReturnsArgument() {
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(call -> call.getArgument(0));
    }

    // ---------- Z: Zero ----------

    @Test
    void customerWithNoReservationsGetsEmptyList() {
        when(reservationRepository.findByCustomerIdOrderByPickupDateDesc(7)).thenReturn(List.of());

        assertTrue(reservationService.getCustomerReservations(7).isEmpty());
    }

    // ---------- O: One ----------

    @Test
    void createReservationWithValidDatesIsPendingWithCorrectPrice() {
        vehicleCanBeBooked();

        ReservationDTO result = reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(4));

        assertEquals(ReservationStatus.PENDING, result.status());
        assertEquals(3 * 350, result.totalPrice());
        assertEquals(7, result.customerId());
        assertEquals("Toyota Yaris", result.vehicleModel());
        assertEquals("Horsens", result.locationName());
    }

    // ---------- M: Many ----------

    @Test
    void getAllReservationsReturnsEveryReservation() {
        when(reservationRepository.findAllByOrderByPickupDateDesc()).thenReturn(List.of(
                reservationWithStatus(ReservationStatus.PENDING, TODAY),
                reservationWithStatus(ReservationStatus.APPROVED, TODAY.plusDays(5)),
                reservationWithStatus(ReservationStatus.COMPLETED, TODAY.minusDays(10))));

        assertEquals(3, reservationService.getAllReservations().size());
    }

    // ---------- B: Boundaries ----------

    @Test
    void pickupTodayIsAllowed() {
        vehicleCanBeBooked();

        ReservationDTO result = reservationService.createReservation(7, 1, TODAY, TODAY.plusDays(1));

        assertEquals(TODAY, result.pickupDate());
    }

    @Test
    void pickupYesterdayIsRefusedBeforeTheVehicleIsLocked() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(7, 1, TODAY.minusDays(1), TODAY.plusDays(1)));

        // The dates are checked first, so the database is never touched
        verifyNoInteractions(vehicleRepository, reservationRepository);
    }

    @Test
    void returnOnTheSameDayAsPickupIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(1)));
    }

    @Test
    void returnTheNextDayCostsOneDay() {
        vehicleCanBeBooked();

        ReservationDTO result = reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(2));

        assertEquals(350, result.totalPrice());
    }

    @Test
    void pickupBeforeThePickupDateIsRefused() {
        reservationExists(reservationWithStatus(ReservationStatus.APPROVED, TODAY.plusDays(1)));

        assertThrows(IllegalStateException.class, () -> reservationService.pickupReservation(5));
    }

    @Test
    void pickupOnThePickupDateWorks() {
        reservationExists(reservationWithStatus(ReservationStatus.APPROVED, TODAY));
        saveReturnsArgument();

        ReservationDTO result = reservationService.pickupReservation(5);

        assertEquals(ReservationStatus.ACTIVE, result.status());
    }

    // ---------- I: Interfaces ----------

    @Test
    void createReservationLocksTheVehicleAndSaves() {
        vehicleCanBeBooked();

        reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(3));

        verify(vehicleRepository).findByIdForUpdate(1);
        verify(vehicleRepository, never()).findById(anyInt());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservationChecksOverlapWithoutExcludingAnyReservation() {
        vehicleCanBeBooked();
        LocalDate pickup = TODAY.plusDays(1);
        LocalDate returnDate = TODAY.plusDays(3);

        reservationService.createReservation(7, 1, pickup, returnDate);

        verify(reservationRepository).existsOverlapping(eq(1), eq(0), eq(pickup), eq(returnDate),
                eq(ReservationStatus.BLOCKING));
    }

    @Test
    void modifyReservationExcludesItselfFromTheOverlapCheck() {
        reservationExists(reservationWithStatus(ReservationStatus.APPROVED, TODAY.plusDays(1)));
        vehicleCanBeBooked();
        LocalDate pickup = TODAY.plusDays(2);
        LocalDate returnDate = TODAY.plusDays(5);

        ReservationDTO result = reservationService.modifyReservation(5, 1, pickup, returnDate);

        verify(reservationRepository).existsOverlapping(eq(1), eq(5), eq(pickup), eq(returnDate),
                eq(ReservationStatus.BLOCKING));
        assertEquals(ReservationStatus.PENDING, result.status());
        assertEquals(3 * 350, result.totalPrice());
    }

    // ---------- E: Exceptions ----------

    @Test
    void unknownVehicleThrowsNotFound() {
        when(vehicleRepository.findByIdForUpdate(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> reservationService.createReservation(7, 99, TODAY.plusDays(1), TODAY.plusDays(2)));
    }

    @Test
    void unknownReservationThrowsNotFound() {
        when(reservationRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> reservationService.getReservation(99));
    }

    @ParameterizedTest
    @EnumSource(value = VehicleStatus.class, names = {"UNAVAILABLE", "REMOVED"})
    void vehicleThatCannotBeReservedIsRefused(VehicleStatus status) {
        vehicle.setStatus(status);
        when(vehicleRepository.findByIdForUpdate(1)).thenReturn(Optional.of(vehicle));

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(2)));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void overlappingBookingIsRefused() {
        when(vehicleRepository.findByIdForUpdate(1)).thenReturn(Optional.of(vehicle));
        when(reservationRepository.existsOverlapping(anyInt(), anyInt(), any(), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(7, 1, TODAY.plusDays(1), TODAY.plusDays(2)));
        verify(reservationRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"APPROVED", "ACTIVE", "COMPLETED", "CANCELLED"})
    void approveOnlyWorksForPendingReservations(ReservationStatus status) {
        reservationExists(reservationWithStatus(status, TODAY.plusDays(1)));

        assertThrows(IllegalStateException.class, () -> reservationService.approveReservation(5));
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"PENDING", "ACTIVE", "COMPLETED", "CANCELLED"})
    void pickupOnlyWorksForApprovedReservations(ReservationStatus status) {
        reservationExists(reservationWithStatus(status, TODAY));

        assertThrows(IllegalStateException.class, () -> reservationService.pickupReservation(5));
    }

    @ParameterizedTest
    @EnumSource(value = ReservationStatus.class, names = {"PENDING", "APPROVED", "COMPLETED", "CANCELLED"})
    void returnOnlyWorksForActiveReservations(ReservationStatus status) {
        reservationExists(reservationWithStatus(status, TODAY));

        assertThrows(IllegalStateException.class, () -> reservationService.returnReservation(5));
    }

    @Test
    void completedReservationCannotBeCancelled() {
        reservationExists(reservationWithStatus(ReservationStatus.COMPLETED, TODAY.minusDays(5)));

        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(5));
    }

    @Test
    void completedReservationCannotBeModified() {
        reservationExists(reservationWithStatus(ReservationStatus.COMPLETED, TODAY.minusDays(5)));

        assertThrows(IllegalStateException.class,
                () -> reservationService.modifyReservation(5, 1, TODAY.plusDays(1), TODAY.plusDays(2)));
    }

    // ---------- S: Simple scenarios ----------

    @Test
    void fullLifecycleFromCreateToReturn() {
        vehicleCanBeBooked();
        reservationService.createReservation(7, 1, TODAY, TODAY.plusDays(2));

        Reservation saved = reservationWithStatus(ReservationStatus.PENDING, TODAY);
        reservationExists(saved);

        assertEquals(ReservationStatus.APPROVED, reservationService.approveReservation(5).status());

        assertEquals(ReservationStatus.ACTIVE, reservationService.pickupReservation(5).status());
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());

        assertEquals(ReservationStatus.COMPLETED, reservationService.returnReservation(5).status());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }

    @Test
    void pendingReservationCanBeCancelled() {
        reservationExists(reservationWithStatus(ReservationStatus.PENDING, TODAY.plusDays(3)));
        saveReturnsArgument();

        assertEquals(ReservationStatus.CANCELLED, reservationService.cancelReservation(5).status());
    }
}
