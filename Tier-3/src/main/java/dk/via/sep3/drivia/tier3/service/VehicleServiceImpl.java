package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.VehicleDTO;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import dk.via.sep3.drivia.tier3.repository.LocationRepository;
import dk.via.sep3.drivia.tier3.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;
    private final ReservationRepository reservationRepository;

    public VehicleServiceImpl( VehicleRepository vehicleRepository, LocationRepository locationRepository,
                              ReservationRepository reservationRepository ){
        this.vehicleRepository = vehicleRepository;
        this.locationRepository = locationRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional( readOnly = true )
    public List<VehicleDTO> getAllVehicles(){
        return vehicleRepository.findByStatusNot(VehicleStatus.REMOVED).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional( readOnly = true )
    public VehicleDTO getVehicle( int id ){
        return toDTO(findVehicle(id));
    }

    @Override
    @Transactional( readOnly = true )
    public List<VehicleDTO> searchVehicles( String model, String category, String gearbox, int locationId ){
        return vehicleRepository.findByStatusNot(VehicleStatus.REMOVED).stream()
                .filter(v -> model == null || v.getModel().toLowerCase().contains(model.toLowerCase()))
                .filter(v -> category == null || v.getCategory().equalsIgnoreCase(category))
                .filter(v -> gearbox == null || v.getGearbox().equalsIgnoreCase(gearbox))
                .filter(v -> locationId == 0 || v.getLocation().getId() == locationId)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional( readOnly = true )
    public List<VehicleDTO> getAvailableVehicles( int locationId, LocalDate pickupDate, LocalDate returnDate ){
        if ( pickupDate.isBefore(LocalDate.now()) ){
            throw new IllegalArgumentException("Pickup date cannot be in the past");
        }
        if ( !returnDate.isAfter(pickupDate) ){
            throw new IllegalArgumentException("Return date must be after pickup date");
        }
        findLocation(locationId);
        return vehicleRepository.findAvailable(locationId, pickupDate, returnDate,
                        List.of(VehicleStatus.AVAILABLE, VehicleStatus.RENTED),
                        ReservationStatus.BLOCKING).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public VehicleDTO addVehicle( VehicleDTO dto ){
        validate(dto);
        if ( vehicleRepository.existsByRegistrationNo(dto.registrationNo()) ){
            throw new IllegalStateException("Registration number " + dto.registrationNo() + " already exists");
        }
        Vehicle vehicle = new Vehicle(dto.registrationNo(), dto.model(), dto.productionDate(), dto.horsepower(),
                dto.category(), dto.gearbox(), dto.pricePerDay(), findLocation(dto.locationId()));
        return toDTO(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleDTO updateVehicle( VehicleDTO dto ){
        validate(dto);
        Vehicle vehicle = findVehicle(dto.id());
        if ( vehicleRepository.existsByRegistrationNoAndIdNot(dto.registrationNo(), dto.id()) ){
            throw new IllegalStateException("Registration number " + dto.registrationNo() + " already exists");
        }
        vehicle.setRegistrationNo(dto.registrationNo());
        vehicle.setModel(dto.model());
        vehicle.setProductionDate(dto.productionDate());
        vehicle.setHorsepower(dto.horsepower());
        vehicle.setCategory(dto.category());
        vehicle.setGearbox(dto.gearbox());
        vehicle.setPricePerDay(dto.pricePerDay());
        vehicle.setLocation(findLocation(dto.locationId()));
        return toDTO(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleDTO updateVehicleStatus( int id, VehicleStatus status ){
        if ( status == VehicleStatus.REMOVED ){
            throw new IllegalArgumentException("Use remove vehicle to remove a vehicle");
        }
        Vehicle vehicle = findVehicle(id);
        vehicle.setStatus(status);
        return toDTO(vehicleRepository.save(vehicle));
    }

    // The vehicle is only marked as REMOVED, so old reservations still point to it
    @Override
    @Transactional
    public void removeVehicle( int id ){
        Vehicle vehicle = findVehicle(id);
        if ( reservationRepository.existsByVehicleIdAndStatusIn(id, ReservationStatus.BLOCKING) ){
            throw new IllegalStateException("Vehicle has active or future reservations and cannot be removed");
        }
        vehicle.setStatus(VehicleStatus.REMOVED);
        vehicleRepository.save(vehicle);
    }

    private Vehicle findVehicle( int id ){
        return vehicleRepository.findById(id)
                .filter(v -> v.getStatus() != VehicleStatus.REMOVED)
                .orElseThrow(() -> new NoSuchElementException("Vehicle " + id + " not found"));
    }

    private Location findLocation( int id ){
        return locationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Location " + id + " not found"));
    }

    private void validate( VehicleDTO dto ){
        if ( dto.registrationNo() == null || dto.registrationNo().isBlank() ){
            throw new IllegalArgumentException("Registration number is required");
        }
        if ( dto.model() == null || dto.model().isBlank() ){
            throw new IllegalArgumentException("Model is required");
        }
        if ( dto.productionDate() == null || dto.productionDate().isAfter(LocalDate.now()) ){
            throw new IllegalArgumentException("Production date cannot be in the future");
        }
        if ( dto.horsepower() <= 0 ){
            throw new IllegalArgumentException("Horsepower must be positive");
        }
        if ( dto.category() == null || dto.category().isBlank() ){
            throw new IllegalArgumentException("Category is required");
        }
        if ( dto.gearbox() == null || dto.gearbox().isBlank() ){
            throw new IllegalArgumentException("Gearbox is required");
        }
        if ( dto.pricePerDay() <= 0 ){
            throw new IllegalArgumentException("Price per day must be positive");
        }
    }

    private VehicleDTO toDTO( Vehicle v ){
        return new VehicleDTO(v.getId(), v.getRegistrationNo(), v.getModel(), v.getProductionDate(),
                v.getHorsepower(), v.getCategory(), v.getGearbox(), v.getPricePerDay(), v.getStatus(),
                v.getLocation().getId(), v.getLocation().getName());
    }
}