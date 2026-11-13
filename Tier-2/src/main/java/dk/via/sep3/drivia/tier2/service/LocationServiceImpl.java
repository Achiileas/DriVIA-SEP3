package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.GetAllLocationsRequest;
import dk.via.sep3.drivia.grpc.Location;
import dk.via.sep3.drivia.grpc.LocationIdRequest;
import dk.via.sep3.drivia.grpc.LocationServiceGrpc;
import dk.via.sep3.drivia.tier2.dto.LocationResponse;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LocationServiceImpl implements LocationService{
    private final LocationServiceGrpc.LocationServiceBlockingStub locationStub;

    public LocationServiceImpl(
            LocationServiceGrpc.LocationServiceBlockingStub locationStub) {
        this.locationStub = locationStub;
    }

    @Override
    public List<LocationResponse> getAllLocations() {

        return locationStub
                .getAllLocations(GetAllLocationsRequest.newBuilder().build())
                .getLocationsList()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LocationResponse getLocation(int id) {

        Location location = locationStub.getLocation(
                LocationIdRequest.newBuilder()
                        .setId(id)
                        .build()
        );

        return toResponse(location);
    }

    private LocationResponse toResponse(Location location) {

        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getAddress()
        );
    }
}
