package dk.via.sep3.drivia.tier3.reservation.repository;

import dk.via.sep3.drivia.tier3.reservation.entity.Reservation;
import dk.via.sep3.drivia.tier3.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByPickupDateDesc();

    List<Reservation> findByCustomerIdOrderByPickupDateDesc(Long customerId);

    boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<ReservationStatus> statuses);

    boolean existsByLocationId(Long locationId);

    // Is the vehicle already booked in this period? excludeId skips the reservation being modified
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.vehicle.id = :vehicleId
              AND r.id <> :excludeId
              AND r.status IN :statuses
              AND r.pickupDate < :returnDate
              AND r.returnDate > :pickupDate
            """)
    boolean existsOverlapping(Long vehicleId,
                              Long excludeId,
                              LocalDate pickupDate,
                              LocalDate returnDate,
                              Collection<ReservationStatus> statuses);
}
