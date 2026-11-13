package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.*;
import dk.via.sep3.drivia.tier2.dto.LocationResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationServiceGrpc.LocationServiceBlockingStub locationStub;

    private LocationServiceImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(locationStub);
    }

    // Helper method
    private Location createLocation(int id, String name) {
        return Location.newBuilder()
                .setId(id)
                .setName(name)
                .setAddress("Horsens, Denmark")
                .build();
    }

    // Z - Zero
    @Test
    void getAllLocationsReturnsEmptyListWhenNoLocationsExist() {
        when(locationStub.getAllLocations(any(GetAllLocationsRequest.class)))
                .thenReturn(LocationList.newBuilder().build());

        List<LocationResponse> result = locationService.getAllLocations();

        assertTrue(result.isEmpty());
    }

    // O - One
    @Test
    void getLocationReturnsOneLocation() {
        Location location = createLocation(1, "Horsens");

        when(locationStub.getLocation(any(LocationIdRequest.class)))
                .thenReturn(location);

        LocationResponse result = locationService.getLocation(1);

        assertEquals(1, result.id());
        assertEquals("Horsens", result.name());
        assertEquals("Horsens, Denmark", result.address());
    }

    // M - Many
    @Test
    void getAllLocationsReturnsMultipleLocations() {
        LocationList locations = LocationList.newBuilder()
                .addLocations(createLocation(1, "Horsens"))
                .addLocations(createLocation(2, "Aarhus"))
                .addLocations(createLocation(3, "Copenhagen"))
                .build();

        when(locationStub.getAllLocations(any(GetAllLocationsRequest.class)))
                .thenReturn(locations);

        List<LocationResponse> result = locationService.getAllLocations();

        assertEquals(3, result.size());
        assertEquals("Horsens", result.get(0).name());
        assertEquals("Aarhus", result.get(1).name());
        assertEquals("Copenhagen", result.get(2).name());
    }

    // B - Boundary
    @Test
    void getLocationPassesZeroIdToGrpc() {
        when(locationStub.getLocation(any(LocationIdRequest.class)))
                .thenReturn(createLocation(0, "Unknown"));

        locationService.getLocation(0);

        verify(locationStub).getLocation(argThat(request ->
                request.getId() == 0
        ));
    }

    // I - Interface
    @Test
    void getLocationSendsCorrectIdToGrpc() {
        when(locationStub.getLocation(any(LocationIdRequest.class)))
                .thenReturn(createLocation(5, "Aarhus"));

        locationService.getLocation(5);

        verify(locationStub).getLocation(argThat(request ->
                request.getId() == 5
        ));
    }

    // E - Exceptions
    @Test
    void getAllLocationsPropagatesGrpcException() {
        when(locationStub.getAllLocations(any(GetAllLocationsRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        assertThrows(StatusRuntimeException.class,
                () -> locationService.getAllLocations());
    }

    // S - Simple scenarios
    @Test
    void getAllLocationsMapsLocationDetailsCorrectly() {
        Location location = Location.newBuilder()
                .setId(10)
                .setName("Billund")
                .setAddress("Billund Airport")
                .build();

        when(locationStub.getAllLocations(any(GetAllLocationsRequest.class)))
                .thenReturn(LocationList.newBuilder()
                        .addLocations(location)
                        .build());

        List<LocationResponse> result = locationService.getAllLocations();

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).id());
        assertEquals("Billund", result.get(0).name());
        assertEquals("Billund Airport", result.get(0).address());
    }
}