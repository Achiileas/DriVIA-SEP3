package dk.via.sep3.drivia.tier2.service;

import dk.via.sep3.drivia.tier2.dto.response.LocationResponse;

import java.util.List;

public interface LocationService {
    List<LocationResponse> getAllLocations();
    LocationResponse getLocation(int id);
}
