
package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.response.VehicleResponse;
import dk.via.sep3.drivia.tier2.dto.request.VehicleCreateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleUpdateRequest;
import dk.via.sep3.drivia.tier2.dto.request.VehicleStatusRequest;
import dk.via.sep3.drivia.tier2.service.VehicleService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        VehicleController controller = new VehicleController(vehicleService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .build();
    }

    // Helper method
    private VehicleResponse createCar(int id, String model) {
        return new VehicleResponse(
            id,
            "AB12345",
            model,
            LocalDate.of(2022, 1, 1),
            150,
            "SUV",
            "Automatic",
            500,
            "AVAILABLE",
            1,
            "Horsens"
        );
    }

    // Z - Zero
    @Test
    void getAllVehiclesReturnsEmptyList() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        verify(vehicleService).getAllVehicles();
    }

    // O - One
    @Test
    void getCarByIdReturnsOneVehicle() throws Exception {
        when(vehicleService.getVehicleById(1))
            .thenReturn(createCar(1, "Toyota Corolla"));

        mockMvc.perform(get("/api/vehicles/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Toyota Corolla"));

        verify(vehicleService).getVehicleById(1);
    }

    // M - Many
    @Test
    void getAllVehiclesReturnsMultipleVehicles() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of(
            createCar(1, "Toyota Corolla"),
            createCar(2, "BMW X5")
        ));

        mockMvc.perform(get("/api/vehicles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].model").value("Toyota Corolla"))
            .andExpect(jsonPath("$[1].model").value("BMW X5"));
    }

    // B - Boundary
    @Test
    void getCarByIdReturnsNotFoundWhenVehicleDoesNotExist() throws Exception {
        when(vehicleService.getVehicleById(999))
            .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/vehicles/999"))
            .andExpect(status().isNotFound());
    }

    // I - Interface
    @Test
    void searchVehiclesPassesParametersToService() throws Exception {
        when(vehicleService.searchVehicles("Toyota", "SUV", "Automatic", 1))
            .thenReturn(List.of(createCar(1, "Toyota Corolla")));

        mockMvc.perform(get("/api/vehicles/search")
                .param("model", "Toyota")
                .param("category", "SUV")
                .param("gearbox", "Automatic")
                .param("locationId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        verify(vehicleService).searchVehicles("Toyota", "SUV", "Automatic", 1);
    }

    @Test
    void getAvailableVehiclesPassesParametersToService() throws Exception {
        LocalDateTime pickup = LocalDateTime.of(2026, 12, 1, 10, 0);
        LocalDateTime returnDate = LocalDateTime.of(2026, 12, 5, 10, 0);

        when(vehicleService.getAvailableVehicles(1, pickup, returnDate))
            .thenReturn(List.of(createCar(1, "Toyota Corolla")));

        mockMvc.perform(get("/api/vehicles/available")
                .param("locationId", "1")
                .param("pickupDate", "2026-12-01T10:00")
                .param("returnDate", "2026-12-05T10:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        verify(vehicleService).getAvailableVehicles(1, pickup, returnDate);
    }

    // E - Exceptions
    @Test
    void getAllVehiclesReturnsServiceUnavailableWhenTier3IsUnavailable()
        throws Exception {

        when(vehicleService.getAllVehicles())
            .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(get("/api/vehicles"))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getVehicleByIdReturnsInternalServerErrorForUnexpectedException()
        throws Exception {

        when(vehicleService.getVehicleById(1))
            .thenThrow(new StatusRuntimeException(Status.INTERNAL));

        mockMvc.perform(get("/api/vehicles/1"))
            .andExpect(status().isInternalServerError());
    }

    // S - Simple scenarios
    @Test
    void searchVehiclesReturnsEmptyListWhenNoMatchesExist() throws Exception {
        when(vehicleService.searchVehicles("Spaceship", null, null, 0))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles/search")
                .param("model", "Spaceship"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    // CREATE - POST
    @Test
    void addVehicleReturnsCreatedWhenVehicleIsAdded() throws Exception {
        VehicleCreateRequest request = new VehicleCreateRequest(
            "AB12345",
            "Toyota Corolla",
            LocalDate.of(2022, 1, 1),
            150,
            "SUV",
            "Automatic",
            500,
            1
        );

        when(vehicleService.addVehicle(request))
            .thenReturn(createCar(1, "Toyota Corolla"));

        mockMvc.perform(post("/api/vehicles")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Toyota Corolla"));

        verify(vehicleService).addVehicle(request);
    }

    @Test
    void addVehicleReturnsConflictWhenRegistrationAlreadyExists()
        throws Exception {

        when(vehicleService.addVehicle(any(VehicleCreateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.ALREADY_EXISTS));

        mockMvc.perform(post("/api/vehicles")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void addVehicleReturnsBadRequestWhenDataIsInvalid()
        throws Exception {

        when(vehicleService.addVehicle(any(VehicleCreateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(post("/api/vehicles")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addVehicleReturnsServiceUnavailableWhenTier3IsUnavailable()
        throws Exception {

        when(vehicleService.addVehicle(any(VehicleCreateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(post("/api/vehicles")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isServiceUnavailable());
    }

    // UPDATE - PUT
    @Test
    void updateVehicleReturnsUpdatedVehicle() throws Exception {
        VehicleUpdateRequest request = new VehicleUpdateRequest(
            "AB12345",
            "Toyota Corolla",
            LocalDate.of(2022, 1, 1),
            150,
            "SUV",
            "Automatic",
            500,
            1
        );

        when(vehicleService.updateVehicle(1, request))
            .thenReturn(createCar(1, "Toyota Corolla"));

        mockMvc.perform(put("/api/vehicles/1")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Toyota Corolla"));

        verify(vehicleService).updateVehicle(1, request);
    }

    @Test
    void updateVehicleReturnsNotFoundWhenVehicleDoesNotExist()
        throws Exception {

        when(vehicleService.updateVehicle(
            eq(999),
            any(VehicleUpdateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(put("/api/vehicles/999")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateVehicleReturnsBadRequestWhenDataIsInvalid()
        throws Exception {

        when(vehicleService.updateVehicle(
            eq(1),
            any(VehicleUpdateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(put("/api/vehicles/1")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateVehicleReturnsServiceUnavailableWhenTier3IsUnavailable()
        throws Exception {

        when(vehicleService.updateVehicle(
            eq(1),
            any(VehicleUpdateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(put("/api/vehicles/1")
                .contentType("application/json")
                .content("""
                    {
                        "registrationNo": "AB12345",
                        "model": "Toyota Corolla",
                        "productionDate": "2022-01-01",
                        "horsepower": 150,
                        "category": "SUV",
                        "gearbox": "Automatic",
                        "pricePerDay": 500,
                        "locationId": 1
                    }
                    """))
            .andExpect(status().isServiceUnavailable());
    }


    // UPDATE STATUS - PUT
    @Test
    void updateVehicleStatusReturnsUpdatedVehicle() throws Exception {
        when(vehicleService.updateVehicleStatus(1, "UNAVAILABLE"))
            .thenReturn(createCar(1, "Toyota Corolla"));

        mockMvc.perform(put("/api/vehicles/1/status")
                .contentType("application/json")
                .content("""
                    {
                        "status": "UNAVAILABLE"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Toyota Corolla"));

        verify(vehicleService).updateVehicleStatus(1, "UNAVAILABLE");
    }

    @Test
    void updateVehicleStatusReturnsNotFoundWhenVehicleDoesNotExist()
        throws Exception {

        when(vehicleService.updateVehicleStatus(999, "UNAVAILABLE"))
            .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(put("/api/vehicles/999/status")
                .contentType("application/json")
                .content("""
                    {
                        "status": "UNAVAILABLE"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateVehicleStatusReturnsBadRequestWhenStatusIsInvalid()
        throws Exception {

        when(vehicleService.updateVehicleStatus(1, "INVALID"))
            .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(put("/api/vehicles/1/status")
                .contentType("application/json")
                .content("""
                    {
                        "status": "INVALID"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateVehicleStatusReturnsServiceUnavailableWhenTier3IsUnavailable()
        throws Exception {

        when(vehicleService.updateVehicleStatus(1, "UNAVAILABLE"))
            .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(put("/api/vehicles/1/status")
                .contentType("application/json")
                .content("""
                    {
                        "status": "UNAVAILABLE"
                    }
                    """))
            .andExpect(status().isServiceUnavailable());
    }

    // DELETE - DELETE
    @Test
    void deleteVehicleReturnsNoContentWhenVehicleIsRemoved() throws Exception {

        mockMvc.perform(delete("/api/vehicles/1"))
            .andExpect(status().isNoContent());

        verify(vehicleService).removeVehicle(1);
    }

    @Test
    void deleteVehicleReturnsNotFoundWhenVehicleDoesNotExist()
        throws Exception {

        doThrow(new StatusRuntimeException(Status.NOT_FOUND))
            .when(vehicleService).removeVehicle(999);

        mockMvc.perform(delete("/api/vehicles/999"))
            .andExpect(status().isNotFound());

        verify(vehicleService).removeVehicle(999);
    }

    @Test
    void deleteVehicleReturnsConflictWhenRemovalIsNotAllowed()
        throws Exception {

        doThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION))
            .when(vehicleService).removeVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
            .andExpect(status().isConflict());

        verify(vehicleService).removeVehicle(1);
    }

    @Test
    void deleteVehicleReturnsServiceUnavailableWhenTier3IsUnavailable()
        throws Exception {

        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
            .when(vehicleService).removeVehicle(1);

        mockMvc.perform(delete("/api/vehicles/1"))
            .andExpect(status().isServiceUnavailable());

        verify(vehicleService).removeVehicle(1);
    }

    @Test
    void searchVehiclesReturnsBadRequestWhenInvalidArgument() throws Exception {
        when(vehicleService.searchVehicles("Toyota", null, null, 0))
            .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(get("/api/vehicles/search")
                .param("model", "Toyota"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableVehiclesReturnsBadRequestWhenDatesAreInvalid()
        throws Exception {

        LocalDateTime pickup = LocalDateTime.of(2026, 12, 5, 10, 0);
        LocalDateTime returnDate = LocalDateTime.of(2026, 12, 1, 10, 0);

        when(vehicleService.getAvailableVehicles(1, pickup, returnDate))
            .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(get("/api/vehicles/available")
                .param("locationId", "1")
                .param("pickupDate", "2026-12-05T10:00")
                .param("returnDate", "2026-12-01T10:00"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateVehicleReturnsConflictWhenRegistrationAlreadyExists()
        throws Exception {

        when(vehicleService.updateVehicle(
            eq(1),
            any(VehicleUpdateRequest.class)))
            .thenThrow(new StatusRuntimeException(Status.ALREADY_EXISTS));

        mockMvc.perform(put("/api/vehicles/1")
                .contentType("application/json")
                .content("""
                {
                    "registrationNo": "AB12345",
                    "model": "Toyota Corolla",
                    "productionDate": "2022-01-01",
                    "horsepower": 150,
                    "category": "SUV",
                    "gearbox": "Automatic",
                    "pricePerDay": 500,
                    "locationId": 1
                }
                """))
            .andExpect(status().isConflict());
    }
}
