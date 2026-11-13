package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.CarResponse;
import dk.via.sep3.drivia.tier2.service.CarService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarControllerTest {

    @Mock
    private CarService carService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CarController controller = new CarController(carService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // Helper method
    private CarResponse createCar(int id, String model) {
        return new CarResponse(
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
    void getAllCarsReturnsEmptyList() throws Exception {
        when(carService.getAllCars()).thenReturn(List.of());

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(carService).getAllCars();
    }

    // O - One
    @Test
    void getCarByIdReturnsOneCar() throws Exception {
        when(carService.getCarById(1))
                .thenReturn(createCar(1, "Toyota Corolla"));

        mockMvc.perform(get("/api/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.model").value("Toyota Corolla"));

        verify(carService).getCarById(1);
    }

    // M - Many
    @Test
    void getAllCarsReturnsMultipleCars() throws Exception {
        when(carService.getAllCars()).thenReturn(List.of(
                createCar(1, "Toyota Corolla"),
                createCar(2, "BMW X5")
        ));

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].model").value("Toyota Corolla"))
                .andExpect(jsonPath("$[1].model").value("BMW X5"));
    }

    // B - Boundary
    @Test
    void getCarByIdReturnsNotFoundWhenCarDoesNotExist() throws Exception {
        when(carService.getCarById(999))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/cars/999"))
                .andExpect(status().isNotFound());
    }

    // I - Interface
    @Test
    void searchCarsPassesParametersToService() throws Exception {
        when(carService.searchCars("Toyota", "SUV", "Automatic", 1))
                .thenReturn(List.of(createCar(1, "Toyota Corolla")));

        mockMvc.perform(get("/api/cars/search")
                        .param("model", "Toyota")
                        .param("category", "SUV")
                        .param("gearbox", "Automatic")
                        .param("locationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(carService).searchCars("Toyota", "SUV", "Automatic", 1);
    }

    @Test
    void getAvailableCarsPassesParametersToService() throws Exception {
        LocalDate pickup = LocalDate.of(2026, 12, 1);
        LocalDate returnDate = LocalDate.of(2026, 12, 5);

        when(carService.getAvailableCars(1, pickup, returnDate))
                .thenReturn(List.of(createCar(1, "Toyota Corolla")));

        mockMvc.perform(get("/api/cars/available")
                        .param("locationId", "1")
                        .param("pickupDate", "2026-12-01")
                        .param("returnDate", "2026-12-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(carService).getAvailableCars(1, pickup, returnDate);
    }

    // E - Exceptions
    @Test
    void getAllCarsReturnsServiceUnavailableWhenTier3IsUnavailable()
            throws Exception {

        when(carService.getAllCars())
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getCarByIdReturnsInternalServerErrorForUnexpectedException()
            throws Exception {

        when(carService.getCarById(1))
                .thenThrow(new StatusRuntimeException(Status.INTERNAL));

        mockMvc.perform(get("/api/cars/1"))
                .andExpect(status().isInternalServerError());
    }

    // S - Simple scenarios
    @Test
    void searchCarsReturnsEmptyListWhenNoMatchesExist() throws Exception {
        when(carService.searchCars("Spaceship", null, null, 0))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/cars/search")
                        .param("model", "Spaceship"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}