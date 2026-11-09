package dk.via.sep3.drivia.tier3.vehicle.repository;

import dk.via.sep3.drivia.tier3.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  boolean existsByLocation_Id(Long locationId);
}