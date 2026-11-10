package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

  boolean existsByLocation_Id(int locationId);

  // Locks the vehicle row until the transaction ends, so two reservations
  // for the same vehicle are handled one after the other
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
  Optional<Vehicle> findByIdForUpdate(int id);
}