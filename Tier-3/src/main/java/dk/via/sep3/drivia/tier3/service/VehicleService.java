package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.VehicleDTO;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import java.time.LocalDate;
import java.util.List;

public interface VehicleService {

    List<VehicleDTO> getAllVehicles();

    VehicleDTO getVehicle( int id );

    // null text or locationId 0 means "no filter"
    List<VehicleDTO> searchVehicles( String model, String category, String gearbox, int locationId );

    List<VehicleDTO> getAvailableVehicles( int locationId, LocalDate pickupDate, LocalDate returnDate );

    VehicleDTO addVehicle( VehicleDTO vehicle );

    VehicleDTO updateVehicle( VehicleDTO vehicle );

    VehicleDTO updateVehicleStatus( int id, VehicleStatus status );

    void removeVehicle( int id );
}
