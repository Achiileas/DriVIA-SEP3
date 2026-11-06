package dk.via.sep3.drivia.tier3.location.service;

import dk.via.sep3.drivia.tier3.location.dto.LocationDTO;
import java.util.List;

public interface LocationService{
    List<LocationDTO> getAllLocations();
    LocationDTO getLocation(Long id);
    LocationDTO addLocation(LocationDTO location);
    LocationDTO updateLocation(LocationDTO location);
    void deleteLocation(Long id);
}
