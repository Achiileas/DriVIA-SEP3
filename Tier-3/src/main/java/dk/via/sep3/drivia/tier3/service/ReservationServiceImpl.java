package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.ReservationDTO;
import dk.via.sep3.drivia.tier3.entity.Reservation;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.repository.ReservationRepository;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservationServiceImpl implements ReservationService {

    // Used as excludeId when there is no existing reservation to skip . Make it int
    private static final int NO_RESERVATION = 0;

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  VehicleRepository vehicleRepository) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // Everything in this method is one transaction: it is saved completely or not at all
    @Override
    @Transactional
    public ReservationDTO createReservation(int customerId, int vehicleId, LocalDate pickupDate, LocalDate returnDate) {
        validateDates(pickupDate, returnDate);
        Vehicle vehicle = lockVehicle(vehicleId);
        checkVehicleIsFree(vehicle, NO_RESERVATION, pickupDate, returnDate);

        Reservation reservation = new Reservation(customerId, vehicle, pickupDate, returnDate,
                calculatePrice(vehicle, pickupDate, returnDate));
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO getReservation(int id) {
        return toDTO(findReservation(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAllByOrderByPickupDateDesc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getCustomerReservations(int customerId) {
        return reservationRepository.findByCustomerIdOrderByPickupDateDesc(customerId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ReservationDTO modifyReservation(int id, int vehicleId, LocalDate pickupDate, LocalDate returnDate) {
        Reservation reservation = findReservation(id);
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new IllegalStateException("Only pending or approved reservations can be modified");
        }
        validateDates(pickupDate, returnDate);
        Vehicle vehicle = lockVehicle(vehicleId);
        checkVehicleIsFree(vehicle, id, pickupDate, returnDate);

        reservation.setVehicle(vehicle);
        reservation.setPickupDate(pickupDate);
        reservation.setReturnDate(returnDate);
        reservation.setTotalPrice(calculatePrice(vehicle, pickupDate, returnDate));
        reservation.setStatus(ReservationStatus.PENDING); // an employee approves the new details again
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDTO cancelReservation(int id) {
        Reservation reservation = findReservation(id);
        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new IllegalStateException("Only pending or approved reservations can be cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDTO approveReservation(int id) {
        Reservation reservation = findReservation(id);
        checkStatus(reservation, ReservationStatus.PENDING);
        reservation.setStatus(ReservationStatus.APPROVED);
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDTO pickupReservation(int id) {
        Reservation reservation = findReservation(id);
        checkStatus(reservation, ReservationStatus.APPROVED);
        if (LocalDate.now().isBefore(reservation.getPickupDate())) {
            throw new IllegalStateException("The car cannot be picked up before " + reservation.getPickupDate());
        }
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.getVehicle().setStatus(VehicleStatus.RENTED);
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDTO returnReservation(int id) {
        Reservation reservation = findReservation(id);
        checkStatus(reservation, ReservationStatus.ACTIVE);
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        return toDTO(reservationRepository.save(reservation));
    }

    private Vehicle lockVehicle(int vehicleId) {
        return vehicleRepository.findByIdForUpdate(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle " + vehicleId + " not found"));
    }

    private void checkVehicleIsFree(Vehicle vehicle, int excludeId, LocalDate pickupDate, LocalDate returnDate) {
        if (!vehicle.canBeReserved()) {
            throw new IllegalStateException("Vehicle " + vehicle.getId() + " cannot be reserved");
        }
        if (reservationRepository.existsOverlapping(vehicle.getId(), excludeId, pickupDate, returnDate,
                ReservationStatus.BLOCKING)) {
            throw new IllegalStateException("Vehicle is not available for the selected period");
        }
    }

    private void validateDates(LocalDate pickupDate, LocalDate returnDate) {
        if (pickupDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Pickup date cannot be in the past");
        }
        if (!returnDate.isAfter(pickupDate)) {
            throw new IllegalArgumentException("Return date must be after pickup date");
        }
    }

    private void checkStatus(Reservation reservation, ReservationStatus expected) {
        if (reservation.getStatus() != expected) {
            throw new IllegalStateException("Reservation is " + reservation.getStatus() + ", expected " + expected);
        }
    }

    private int calculatePrice(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate) {
        int days = (int) ChronoUnit.DAYS.between(pickupDate, returnDate);
        return days * vehicle.getPricePerDay();
    }

    private Reservation findReservation(int id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation " + id + " not found"));
    }

    private ReservationDTO toDTO(Reservation r) {
        return new ReservationDTO(r.getId(), r.getCustomerId(), r.getVehicle().getId(), r.getVehicle().getModel(),
                r.getLocation().getId(), r.getLocation().getName(), r.getPickupDate(), r.getReturnDate(),
                r.getStatus(), r.getTotalPrice());
    }
}
