package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer>{

  List<Vehicle> findByStatusNot( VehicleStatus status );

  boolean existsByRegistrationNo( String registrationNo );

  boolean existsByRegistrationNoAndIdNot( String registrationNo, int id );

  boolean existsByLocation_Id( int locationId );

  // Vehicles at a location that have no reservation overlapping the chosen dates
  @Query("""
          SELECT v FROM Vehicle v
          WHERE v.location.id = :locationId
            AND v.status IN :vehicleStatuses
            AND NOT EXISTS (
                SELECT r FROM Reservation r
                WHERE r.vehicle = v
                  AND r.status IN :reservationStatuses
                  AND r.pickupDate < :returnDate
                  AND r.returnDate > :pickupDate)
          """)
  List<Vehicle> findAvailable( int locationId, LocalDate pickupDate,
                              LocalDate returnDate, Collection<VehicleStatus> vehicleStatuses,
                              Collection<ReservationStatus> reservationStatuses );

  // Locks the vehicle row until the transaction ends, so two reservations
  // for the same vehicle are handled one after the other
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
  Optional<Vehicle> findByIdForUpdate(int id);
}