package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.grpc.AddLocationRequest;
import dk.via.sep3.drivia.grpc.GetAllLocationsRequest;
import dk.via.sep3.drivia.grpc.Location;
import dk.via.sep3.drivia.grpc.LocationIdRequest;
import dk.via.sep3.drivia.grpc.LocationServiceGrpc;
import dk.via.sep3.drivia.tier2.dto.request.LocationRequest;
import dk.via.sep3.drivia.tier2.dto.response.LocationResponse;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LocationServiceImpl implements LocationService{
    private final LocationServiceGrpc.LocationServiceBlockingStub locationStub;

    public LocationServiceImpl( LocationServiceGrpc.LocationServiceBlockingStub locationStub) {
        this.locationStub = locationStub;
    }

    @Override
    public List<LocationResponse> getAllLocations(){
        return locationStub.getAllLocations(GetAllLocationsRequest.newBuilder().build())
                .getLocationsList().stream()
                .map(this::toResponse).toList();
    }

    @Override
    public LocationResponse getLocation( int id ){
        Location location = locationStub.getLocation(
                LocationIdRequest.newBuilder().setId(id).build()
        );
        return toResponse(location);
    }

    @Override
    public LocationResponse addLocation( LocationRequest request ){
        Location location = locationStub.addLocation(
                AddLocationRequest.newBuilder()
                        .setName(request.name())
                        .setAddress(request.address())
                        .build()
        );
        return toResponse(location);
    }

    // The id comes from the URL, so it cannot differ from the location being changed
    @Override
    public LocationResponse updateLocation( int id, LocationRequest request ){
        Location location = locationStub.updateLocation(
                Location.newBuilder().setId(id)
                        .setName(request.name()).setAddress(request.address()).build()
        );
        return toResponse(location);
    }

    @Override
    public void deleteLocation( int id ){
        locationStub.deleteLocation(
                LocationIdRequest.newBuilder().setId(id).build()
        );
    }

    private LocationResponse toResponse( Location location ){
        return new LocationResponse(location.getId(), location.getName(), location.getAddress()
        );
    }
}
