package dk.via.sep3.drivia.tier3.location.service;

import dk.via.sep3.drivia.tier3.location.dto.LocationDTO;
import dk.via.sep3.drivia.tier3.location.entity.Location;
import dk.via.sep3.drivia.tier3.location.repository.LocationRepository;
import dk.via.sep3.drivia.tier3.reservation.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LocationServiceImpl implements LocationService{

    private final LocationRepository locationRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;

    public LocationServiceImpl( LocationRepository locationRepository,
                               VehicleRepository vehicleRepository,
                               ReservationRepository reservationRepository) {
        this.locationRepository = locationRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional( readOnly = true )
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDTO getLocation(Long id){
        return toDTO(findLocation(id));
    }

    @Override
    @Transactional
    public LocationDTO addLocation(LocationDTO dto){
        validate(dto);
        Location saved = locationRepository.save(new Location(dto.name(), dto.address()));
        return toDTO(saved);
    }

    @Override
    @Transactional
    public LocationDTO updateLocation(LocationDTO dto){
        validate(dto);
        Location location = findLocation(dto.id());
        location.setName(dto.name());
        location.setAddress(dto.address());
        return toDTO(locationRepository.save(location));
    }

    @Override
    @Transactional
    public void deleteLocation(Long id){
        Location location = findLocation(id);
        if (vehicleRepository.existsByLocationId(id)) {
            throw new IllegalStateException("Location still has vehicles and cannot be deleted");
        }
        if (reservationRepository.existsByLocationId(id)) {
            throw new IllegalStateException("Location has reservations and cannot be deleted");
        }
        locationRepository.delete(location);
    }

    private Location findLocation(Long id){
        return locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Location " + id + " not found"));
    }

    private void validate(LocationDTO dto){
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Location name is required");
        }
        if (dto.address() == null || dto.address().isBlank()) {
            throw new IllegalArgumentException("Location address is required");
        }
    }

    private LocationDTO toDTO(Location location){
        return new LocationDTO(location.getId(), location.getName(), location.getAddress());
    }
}
