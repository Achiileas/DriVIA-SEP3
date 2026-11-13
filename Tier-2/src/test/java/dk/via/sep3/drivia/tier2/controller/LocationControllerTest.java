package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.LocationResponse;
import dk.via.sep3.drivia.tier2.service.LocationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocationController controller = new LocationController(locationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // Helper method
    private LocationResponse createLocation(int id, String name) {
        return new LocationResponse(
                id,
                name,
                "Denmark"
        );
    }

    // Z - Zero
    @Test
    void getAllLocationsReturnsEmptyList() throws Exception {
        when(locationService.getAllLocations()).thenReturn(List.of());

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(locationService).getAllLocations();
    }

    // O - One
    @Test
    void getLocationReturnsOneLocation() throws Exception {
        when(locationService.getLocation(1))
                .thenReturn(createLocation(1, "Horsens"));

        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Horsens"))
                .andExpect(jsonPath("$.address").value("Denmark"));

        verify(locationService).getLocation(1);
    }

    // M - Many
    @Test
    void getAllLocationsReturnsMultipleLocations() throws Exception {
        when(locationService.getAllLocations()).thenReturn(List.of(
                createLocation(1, "Horsens"),
                createLocation(2, "Aarhus"),
                createLocation(3, "Copenhagen")
        ));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Horsens"))
                .andExpect(jsonPath("$[1].name").value("Aarhus"))
                .andExpect(jsonPath("$[2].name").value("Copenhagen"));
    }

    // B - Boundary
    @Test
    void getLocationReturnsNotFoundWhenLocationDoesNotExist()
            throws Exception {

        when(locationService.getLocation(999))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/locations/999"))
                .andExpect(status().isNotFound());
    }

    // I - Interface
    @Test
    void getLocationPassesCorrectIdToService() throws Exception {
        when(locationService.getLocation(5))
                .thenReturn(createLocation(5, "Aarhus"));

        mockMvc.perform(get("/api/locations/5"))
                .andExpect(status().isOk());

        verify(locationService).getLocation(5);
    }

    // E - Exceptions
    @Test
    void getAllLocationsReturnsServiceUnavailableWhenTier3IsUnavailable()
            throws Exception {

        when(locationService.getAllLocations())
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getLocationReturnsInternalServerErrorForUnexpectedException()
            throws Exception {

        when(locationService.getLocation(1))
                .thenThrow(new StatusRuntimeException(Status.INTERNAL));

        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isInternalServerError());
    }

    // S - Simple scenarios
    @Test
    void getAllLocationsReturnsCorrectLocationDetails() throws Exception {
        when(locationService.getAllLocations())
                .thenReturn(List.of(
                        new LocationResponse(
                                10,
                                "Billund",
                                "Billund Airport"
                        )
                ));

        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Billund"))
                .andExpect(jsonPath("$[0].address").value("Billund Airport"));
    }
}