package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.request.LocationRequest;
import dk.via.sep3.drivia.tier2.dto.response.LocationResponse;

import java.util.List;

public interface LocationService{ List<LocationResponse> getAllLocations();
    LocationResponse getLocation(int id); LocationResponse addLocation(LocationRequest request);
    LocationResponse updateLocation(int id, LocationRequest request); void deleteLocation(int id);
}
