package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.VehicleDTO;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import dk.via.sep3.drivia.tier3.repository.LocationRepository;
import dk.via.sep3.drivia.tier3.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// White-box tests for VehicleServiceImpl, grouped by ZOMBIES
@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private VehicleServiceImpl vehicleService;
    private Location horsens;
    private Location aarhus;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, locationRepository, reservationRepository);

        horsens = new Location("Horsens", "Banegaardsgade 2");
        ReflectionTestUtils.setField(horsens, "id", 1);
        aarhus = new Location("Aarhus", "Ny Banegaardsgade 1");
        ReflectionTestUtils.setField(aarhus, "id", 2);
    }

    // ---------- helpers ----------

    private Vehicle vehicle(int id, String registrationNo, String model, String category, String gearbox,
                            Location location) {
        Vehicle vehicle = new Vehicle(registrationNo, model, LocalDate.of(2021, 3, 15), 100,
                category, gearbox, 400, location);
        ReflectionTestUtils.setField(vehicle, "id", id);
        return vehicle;
    }

    private VehicleDTO dto(LocalDate productionDate, int horsepower, int pricePerDay) {
        return new VehicleDTO(0, "AB12345", "Toyota Yaris", productionDate, horsepower,
                "Small", "Manual", pricePerDay, null, 1, null);
    }

    private VehicleDTO validDto() {
        return dto(LocalDate.of(2021, 3, 15), 92, 350);
    }

    private VehicleDTO dtoWithText(String registrationNo, String model, String category, String gearbox) {
        return new VehicleDTO(0, registrationNo, model, LocalDate.of(2021, 3, 15), 92,
                category, gearbox, 350, null, 1, null);
    }

    private void addingIsPossible() {
        when(vehicleRepository.existsByRegistrationNo("AB12345")).thenReturn(false);
        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(call -> call.getArgument(0));
    }

    // ---------- Z: Zero ----------

    @Test
    void noVehiclesGivesEmptyList() {
        when(vehicleRepository.findByStatusNot(VehicleStatus.REMOVED)).thenReturn(List.of());

        assertTrue(vehicleService.getAllVehicles().isEmpty());
    }

    @Test
    void searchWithoutFiltersReturnsAllVehicles() {
        when(vehicleRepository.findByStatusNot(VehicleStatus.REMOVED)).thenReturn(List.of(
                vehicle(1, "AB1", "Toyota Yaris", "Small", "Manual", horsens),
                vehicle(2, "AB2", "VW Golf", "Compact", "Manual", horsens),
                vehicle(3, "AB3", "Kia EV6", "Electric", "Automatic", aarhus)));

        assertEquals(3, vehicleService.searchVehicles(null, null, null, 0).size());
    }

    // ---------- O: One ----------

    @Test
    void addVehicleWithValidDataIsAvailable() {
        addingIsPossible();

        VehicleDTO result = vehicleService.addVehicle(validDto());

        assertEquals(VehicleStatus.AVAILABLE, result.status());
        assertEquals("Horsens", result.locationName());
        assertEquals(1, result.locationId());
    }

    // ---------- M: Many ----------

    @Test
    void searchWithSeveralFiltersReturnsOnlyMatchingVehicles() {
        when(vehicleRepository.findByStatusNot(VehicleStatus.REMOVED)).thenReturn(List.of(
                vehicle(1, "AB1", "VW Golf", "Compact", "Manual", horsens),
                vehicle(2, "AB2", "VW Golf", "Compact", "Automatic", horsens),
                vehicle(3, "AB3", "VW Golf", "Compact", "Manual", aarhus),
                vehicle(4, "AB4", "Toyota Yaris", "Small", "Manual", horsens)));

        List<VehicleDTO> result = vehicleService.searchVehicles("golf", "compact", "manual", 1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).id());
    }

    // ---------- B: Boundaries ----------

    @Test
    void horsepowerZeroIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> vehicleService.addVehicle(dto(LocalDate.of(2021, 3, 15), 0, 350)));
    }

    @Test
    void horsepowerOneIsAccepted() {
        addingIsPossible();

        assertEquals(1, vehicleService.addVehicle(dto(LocalDate.of(2021, 3, 15), 1, 350)).horsepower());
    }

    @Test
    void pricePerDayZeroIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> vehicleService.addVehicle(dto(LocalDate.of(2021, 3, 15), 92, 0)));
    }

    @Test
    void pricePerDayOneIsAccepted() {
        addingIsPossible();

        assertEquals(1, vehicleService.addVehicle(dto(LocalDate.of(2021, 3, 15), 92, 1)).pricePerDay());
    }

    @Test
    void productionDateTodayIsAccepted() {
        addingIsPossible();

        assertEquals(TODAY, vehicleService.addVehicle(dto(TODAY, 92, 350)).productionDate());
    }

    @Test
    void productionDateTomorrowIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> vehicleService.addVehicle(dto(TODAY.plusDays(1), 92, 350)));
    }

    // ---------- I: Interfaces ----------

    @Test
    void removeVehicleOnlyMarksItAsRemoved() {
        Vehicle yaris = vehicle(1, "AB1", "Toyota Yaris", "Small", "Manual", horsens);
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(yaris));
        when(reservationRepository.existsByVehicleIdAndStatusIn(1, ReservationStatus.BLOCKING)).thenReturn(false);

        vehicleService.removeVehicle(1);

        assertEquals(VehicleStatus.REMOVED, yaris.getStatus());
        verify(vehicleRepository).save(yaris);
        verify(vehicleRepository, never()).delete(any());
        verify(vehicleRepository, never()).deleteById(any());
    }

    @Test
    void availabilityAsksTheRepositoryForTheRightStatuses() {
        LocalDate pickup = TODAY.plusDays(1);
        LocalDate returnDate = TODAY.plusDays(3);
        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        when(vehicleRepository.findAvailable(eq(1), eq(pickup), eq(returnDate), any(), any())).thenReturn(List.of());

        vehicleService.getAvailableVehicles(1, pickup, returnDate);

        verify(vehicleRepository).findAvailable(1, pickup, returnDate,
                List.of(VehicleStatus.AVAILABLE, VehicleStatus.RENTED), ReservationStatus.BLOCKING);
    }

    // ---------- E: Exceptions ----------

    @Test
    void duplicateRegistrationNumberIsRefusedOnAdd() {
        when(vehicleRepository.existsByRegistrationNo("AB12345")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> vehicleService.addVehicle(validDto()));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void duplicateRegistrationNumberIsRefusedOnUpdate() {
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(vehicle(1, "AB1", "Toyota Yaris", "Small", "Manual", horsens)));
        when(vehicleRepository.existsByRegistrationNoAndIdNot("AB12345", 1)).thenReturn(true);
        VehicleDTO update = new VehicleDTO(1, "AB12345", "Toyota Yaris", LocalDate.of(2021, 3, 15), 92,
                "Small", "Manual", 350, null, 1, null);

        assertThrows(IllegalStateException.class, () -> vehicleService.updateVehicle(update));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void blankTextFieldsAreRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.addVehicle(dtoWithText(null, "Toyota Yaris", "Small", "Manual")));
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.addVehicle(dtoWithText("AB12345", " ", "Small", "Manual")));
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.addVehicle(dtoWithText("AB12345", "Toyota Yaris", "", "Manual")));
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.addVehicle(dtoWithText("AB12345", "Toyota Yaris", "Small", "  ")));
        verifyNoInteractions(vehicleRepository, locationRepository);
    }

    @Test
    void statusCannotBeSetToRemoved() {
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.updateVehicleStatus(1, VehicleStatus.REMOVED));
        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void vehicleWithActiveReservationsCannotBeRemoved() {
        Vehicle yaris = vehicle(1, "AB1", "Toyota Yaris", "Small", "Manual", horsens);
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(yaris));
        when(reservationRepository.existsByVehicleIdAndStatusIn(1, ReservationStatus.BLOCKING)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> vehicleService.removeVehicle(1));
        assertEquals(VehicleStatus.AVAILABLE, yaris.getStatus());
    }

    @Test
    void removedVehicleIsNotFound() {
        Vehicle yaris = vehicle(1, "AB1", "Toyota Yaris", "Small", "Manual", horsens);
        yaris.setStatus(VehicleStatus.REMOVED);
        when(vehicleRepository.findById(1)).thenReturn(Optional.of(yaris));

        assertThrows(NoSuchElementException.class, () -> vehicleService.getVehicle(1));
    }

    @Test
    void availabilityWithPickupInThePastIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> vehicleService.getAvailableVehicles(1, TODAY.minusDays(1), TODAY.plusDays(1)));
    }

    // ---------- S: Simple scenarios ----------

    @Test
    void addedVehicleCanBeReadBack() {
        when(vehicleRepository.existsByRegistrationNo("AB12345")).thenReturn(false);
        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        Vehicle[] saved = new Vehicle[1];
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(call -> {
            saved[0] = call.getArgument(0);
            ReflectionTestUtils.setField(saved[0], "id", 10);
            return saved[0];
        });

        VehicleDTO added = vehicleService.addVehicle(validDto());
        when(vehicleRepository.findById(10)).thenReturn(Optional.of(saved[0]));
        VehicleDTO found = vehicleService.getVehicle(added.id());

        assertEquals(added, found);
        assertEquals("AB12345", found.registrationNo());
        assertEquals("Toyota Yaris", found.model());
    }
}
