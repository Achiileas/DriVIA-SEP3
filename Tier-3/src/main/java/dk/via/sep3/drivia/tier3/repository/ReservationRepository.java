package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.Reservation;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer>{

    List<Reservation> findAllByOrderByPickupDateDesc();

    List<Reservation> findByCustomerIdOrderByPickupDateDesc(int customerId);

    boolean existsByVehicleIdAndStatusIn(int vehicleId, Collection<ReservationStatus> statuses);

    boolean existsByLocationId(int locationId);

    // Is the vehicle already booked in this period? excludeId skips the reservation being modified
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.vehicle.id = :vehicleId
              AND r.id <> :excludeId
              AND r.status IN :statuses
              AND r.pickupDate < :returnDate
              AND r.returnDate > :pickupDate
            """)
    boolean existsOverlapping(int vehicleId,
                              int excludeId,
                              LocalDate pickupDate,
                              LocalDate returnDate,
                              Collection<ReservationStatus> statuses);
}
