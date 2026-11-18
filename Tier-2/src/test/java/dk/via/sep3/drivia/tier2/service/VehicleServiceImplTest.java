package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.response.VehicleResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest
{

    @Mock
    private VehicleServiceGrpc.VehicleServiceBlockingStub vehicleStub;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleStub);
    }

    // Helper method
    private Vehicle createVehicle(int id, String model) {
        return Vehicle.newBuilder()
                .setId(id)
                .setRegistrationNo("AB12345")
                .setModel(model)
                .setProductionDate("2022-01-01")
                .setHorsepower(150)
                .setCategory("SUV")
                .setGearbox("Automatic")
                .setPricePerDay(500)
                .setStatus("AVAILABLE")
                .setLocationId(1)
                .setLocationName("Horsens")
                .build();
    }

    // Z - Zero
    @Test
    void getAllVehiclesReturnsEmptyListWhenNoVehiclesExist() {
        when(vehicleStub.getAllVehicles(any(GetAllVehiclesRequest.class)))
                .thenReturn(VehicleList.newBuilder().build());

        List<VehicleResponse> result = vehicleService.getAllVehicles();

        assertTrue(result.isEmpty());
    }

    // O - One
    @Test
    void getVehicleByIdReturnsOneVehicle() {
        Vehicle vehicle = createVehicle(1, "Toyota Corolla");

        when(vehicleStub.getVehicle(any(VehicleIdRequest.class)))
                .thenReturn(vehicle);

        VehicleResponse result = vehicleService.getVehicleById(1);

        assertEquals(1, result.id());
        assertEquals("Toyota Corolla", result.model());
        assertEquals("Horsens", result.locationName());
    }

    // M - Many
    @Test
    void getAllVehiclesReturnsMultipleVehicles() {
        VehicleList vehicles = VehicleList.newBuilder()
                .addVehicles(createVehicle(1, "Toyota Corolla"))
                .addVehicles(createVehicle(2, "BMW X5"))
                .addVehicles(createVehicle(3, "Audi A4"))
                .build();

        when(vehicleStub.getAllVehicles(any(GetAllVehiclesRequest.class)))
                .thenReturn(vehicles);

        List<VehicleResponse> result = vehicleService.getAllVehicles();

        assertEquals(3, result.size());
        assertEquals("Toyota Corolla", result.get(0).model());
        assertEquals("BMW X5", result.get(1).model());
        assertEquals("Audi A4", result.get(2).model());
    }

    // B - Boundary
    @Test
    void searchVehiclesConvertsNullFiltersToEmptyStrings() {
        when(vehicleStub.searchVehicles(any(SearchVehiclesRequest.class)))
                .thenReturn(VehicleList.newBuilder().build());

        vehicleService.searchVehicles(null, null, null, 0);

        verify(vehicleStub).searchVehicles(argThat(request ->
                request.getModel().isEmpty()
                        && request.getCategory().isEmpty()
                        && request.getGearbox().isEmpty()
                        && request.getLocationId() == 0
        ));
    }

    // I - Interface
    @Test
    void getAvailableVehiclesSendsCorrectRequestToGrpc() {
        LocalDate pickup = LocalDate.of(2026, 12, 1);
        LocalDate returnDate = LocalDate.of(2026, 12, 5);

        when(vehicleStub.getAvailableVehicles(any(AvailabilityRequest.class)))
                .thenReturn(VehicleList.newBuilder().build());

        vehicleService.getAvailableVehicles(1, pickup, returnDate);

        verify(vehicleStub).getAvailableVehicles(argThat(request ->
                request.getLocationId() == 1
                        && request.getPickupDate().equals("2026-12-01")
                        && request.getReturnDate().equals("2026-12-05")
        ));
    }

    // E - Exceptions
    @Test
    void getAllVehiclesPropagatesGrpcException() {
        when(vehicleStub.getAllVehicles(any(GetAllVehiclesRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        assertThrows(StatusRuntimeException.class,
                () -> vehicleService.getAllVehicles());
    }

    // S - Simple scenarios
    @Test
    void searchVehiclesReturnsMatchingVehicles() {
        VehicleList vehicles = VehicleList.newBuilder()
                .addVehicles(createVehicle(1, "Toyota Corolla"))
                .build();

        when(vehicleStub.searchVehicles(any(SearchVehiclesRequest.class)))
                .thenReturn(vehicles);

        List<VehicleResponse> result =
                vehicleService.searchVehicles("Toyota", "SUV", "Automatic", 1);

        assertEquals(1, result.size());
        assertEquals("Toyota Corolla", result.get(0).model());
    }
}