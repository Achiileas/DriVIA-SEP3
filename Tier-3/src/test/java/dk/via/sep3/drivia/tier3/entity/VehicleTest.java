package dk.via.sep3.drivia.tier3.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for Vehicle.canBeReserved(), covering every VehicleStatus
class VehicleTest {

    private Vehicle newVehicle() {
        return new Vehicle("AB12345", "Toyota Yaris", LocalDate.of(2021, 3, 15), 92,
                "Small", "Manual", 350, new Location("Horsens", "Banegaardsgade 2"));
    }

    @Test
    void newVehicleIsAvailable() {
        assertEquals(VehicleStatus.AVAILABLE, newVehicle().getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = VehicleStatus.class, names = {"AVAILABLE", "RENTED"})
    void canBeReservedWhenAvailableOrRented(VehicleStatus status) {
        Vehicle vehicle = newVehicle();
        vehicle.setStatus(status);

        assertTrue(vehicle.canBeReserved());
    }

    @ParameterizedTest
    @EnumSource(value = VehicleStatus.class, names = {"UNAVAILABLE", "REMOVED"})
    void cannotBeReservedWhenUnavailableOrRemoved(VehicleStatus status) {
        Vehicle vehicle = newVehicle();
        vehicle.setStatus(status);

        assertFalse(vehicle.canBeReserved());
    }
}
