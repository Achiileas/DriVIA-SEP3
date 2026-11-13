package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.LocationDTO;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.repository.LocationRepository;
import dk.via.sep3.drivia.tier3.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// White-box tests for LocationServiceImpl, grouped by ZOMBIES
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private LocationServiceImpl locationService;
    private Location horsens;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(locationRepository, vehicleRepository, reservationRepository);

        horsens = new Location("Horsens", "Banegaardsgade 2");
        ReflectionTestUtils.setField(horsens, "id", 1);
    }

    // ---------- Z: Zero ----------

    @Test
    void noLocationsGivesEmptyList() {
        when(locationRepository.findAll()).thenReturn(List.of());

        assertTrue(locationService.getAllLocations().isEmpty());
    }

    // ---------- O: One ----------

    @Test
    void addLocationWithNameAndAddressWorks() {
        when(locationRepository.save(any(Location.class))).thenAnswer(call -> call.getArgument(0));

        LocationDTO result = locationService.addLocation(new LocationDTO(0, "Horsens", "Banegaardsgade 2"));

        assertEquals("Horsens", result.name());
        assertEquals("Banegaardsgade 2", result.address());
        verify(locationRepository).save(any(Location.class));
    }

    // ---------- M: Many ----------

    @Test
    void twoLocationsAreBothReturned() {
        when(locationRepository.findAll()).thenReturn(List.of(horsens, new Location("Aarhus", "Ny Banegaardsgade 1")));

        assertEquals(2, locationService.getAllLocations().size());
    }

    // ---------- B: Boundaries ----------

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void missingOrBlankNameIsRefused(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> locationService.addLocation(new LocationDTO(0, name, "Banegaardsgade 2")));
        verifyNoInteractions(locationRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void missingOrBlankAddressIsRefused(String address) {
        assertThrows(IllegalArgumentException.class,
                () -> locationService.addLocation(new LocationDTO(0, "Horsens", address)));
        verifyNoInteractions(locationRepository);
    }

    // ---------- E: Exceptions ----------

    @Test
    void locationWithVehiclesCannotBeDeleted() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        when(vehicleRepository.existsByLocation_Id(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> locationService.deleteLocation(1));
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void locationWithReservationsCannotBeDeleted() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        when(vehicleRepository.existsByLocation_Id(1)).thenReturn(false);
        when(reservationRepository.existsByLocationId(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> locationService.deleteLocation(1));
        verify(locationRepository, never()).delete(any());
    }

    @Test
    void unknownLocationThrowsNotFound() {
        when(locationRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> locationService.getLocation(99));
        assertThrows(NoSuchElementException.class, () -> locationService.deleteLocation(99));
    }

    // ---------- S: Simple scenarios ----------

    @Test
    void addUpdateAndDeleteLocation() {
        when(locationRepository.save(any(Location.class))).thenAnswer(call -> call.getArgument(0));
        locationService.addLocation(new LocationDTO(0, "Horsens", "Banegaardsgade 2"));

        when(locationRepository.findById(1)).thenReturn(Optional.of(horsens));
        LocationDTO updated = locationService.updateLocation(new LocationDTO(1, "Horsens Station", "Banegaardsgade 4"));
        assertEquals("Horsens Station", updated.name());
        assertEquals("Banegaardsgade 4", updated.address());

        when(vehicleRepository.existsByLocation_Id(1)).thenReturn(false);
        when(reservationRepository.existsByLocationId(1)).thenReturn(false);
        locationService.deleteLocation(1);
        verify(locationRepository).delete(horsens);
    }
}
