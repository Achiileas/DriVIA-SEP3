package dk.via.sep3.drivia.tier3.repository;

import dk.via.sep3.drivia.tier3.entity.Customer;
import dk.via.sep3.drivia.tier3.entity.Employee;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.entity.Reservation;
import dk.via.sep3.drivia.tier3.entity.ReservationStatus;
import dk.via.sep3.drivia.tier3.entity.User;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.entity.VehicleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Database operations: runs the repositories' own queries against an in-memory H2 database.
// @Transactional rolls every test back, so each test starts from the same data.
@SpringBootTest(properties = {
        "spring.grpc.server.port=0",
        "spring.datasource.url=jdbc:h2:mem:repositories;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create"
})
@Transactional
class RepositoryTest {

    private static final LocalDateTime DAY = LocalDate.now().plusDays(20).atTime(10, 0);

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Location horsens;
    private Location aarhus;
    private Vehicle yaris;
    private Vehicle golf;
    private Customer anna;

    @BeforeEach
    void setUp() {
        horsens = locationRepository.save(new Location("Horsens", "Banegaardsgade 2"));
        aarhus = locationRepository.save(new Location("Aarhus", "Ny Banegaardsgade 1"));
        yaris = vehicleRepository.save(new Vehicle("AB12345", "Toyota Yaris", LocalDate.of(2021, 3, 15), 92,
                "Small", "Manual", 350, horsens));
        golf = vehicleRepository.save(new Vehicle("AB23456", "VW Golf", LocalDate.of(2022, 6, 1), 130,
                "Compact", "Manual", 450, horsens));
        anna = customerRepository.save(new Customer("Anna", "anna@mail.dk", "secret1", "12345678"));
    }

    private Reservation book(Vehicle vehicle, LocalDateTime pickup, LocalDateTime returnDate, ReservationStatus status) {
        Reservation reservation = new Reservation(anna, vehicle, pickup, returnDate, 700);
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }

    private boolean overlaps(Vehicle vehicle, int excludeId, LocalDateTime pickup, LocalDateTime returnDate) {
        return reservationRepository.existsOverlapping(vehicle.getId(), excludeId, pickup, returnDate,
                ReservationStatus.BLOCKING);
    }

    // ---------- ReservationRepository.existsOverlapping ----------

    @Test
    void overlappingPeriodIsFound() {
        book(yaris, DAY, DAY.plusDays(3), ReservationStatus.APPROVED);

        assertTrue(overlaps(yaris, 0, DAY.plusDays(1), DAY.plusDays(5)));
    }

    @Test
    void pickupOnTheDayTheCarIsReturnedDoesNotOverlap() {
        book(yaris, DAY, DAY.plusDays(3), ReservationStatus.APPROVED);

        assertFalse(overlaps(yaris, 0, DAY.plusDays(3), DAY.plusDays(5)));
    }

    @Test
    void reservationDoesNotOverlapWithItself() {
        Reservation own = book(yaris, DAY, DAY.plusDays(3), ReservationStatus.PENDING);

        assertFalse(overlaps(yaris, own.getId(), DAY, DAY.plusDays(4)));
    }

    @Test
    void cancelledAndCompletedReservationsDoNotBlock() {
        book(yaris, DAY, DAY.plusDays(3), ReservationStatus.CANCELLED);
        book(yaris, DAY, DAY.plusDays(3), ReservationStatus.COMPLETED);

        assertFalse(overlaps(yaris, 0, DAY, DAY.plusDays(3)));
    }

    @Test
    void otherVehiclesBookingsDoNotBlock() {
        book(golf, DAY, DAY.plusDays(3), ReservationStatus.APPROVED);

        assertFalse(overlaps(yaris, 0, DAY, DAY.plusDays(3)));
    }

    // ---------- VehicleRepository.findAvailable ----------

    @Test
    void bookedAndUnavailableVehiclesAreNotAvailable() {
        Vehicle clio = vehicleRepository.save(new Vehicle("AB34567", "Renault Clio", LocalDate.of(2020, 4, 22), 90,
                "Small", "Manual", 300, horsens));
        clio.setStatus(VehicleStatus.UNAVAILABLE);
        book(yaris, DAY, DAY.plusDays(3), ReservationStatus.PENDING);

        List<Vehicle> available = vehicleRepository.findAvailable(horsens.getId(), DAY.plusDays(1), DAY.plusDays(2),
                List.of(VehicleStatus.AVAILABLE, VehicleStatus.RENTED), ReservationStatus.BLOCKING);

        assertEquals(List.of(golf.getId()), available.stream().map(Vehicle::getId).toList());
    }

    @Test
    void vehiclesAtAnotherLocationAreNotAvailable() {
        List<Vehicle> available = vehicleRepository.findAvailable(aarhus.getId(), DAY, DAY.plusDays(2),
                List.of(VehicleStatus.AVAILABLE, VehicleStatus.RENTED), ReservationStatus.BLOCKING);

        assertTrue(available.isEmpty());
    }

    // ---------- VehicleRepository, other queries ----------

    @Test
    void removedVehiclesAreLeftOut() {
        golf.setStatus(VehicleStatus.REMOVED);

        List<Vehicle> vehicles = vehicleRepository.findByStatusNot(VehicleStatus.REMOVED);

        assertEquals(List.of(yaris.getId()), vehicles.stream().map(Vehicle::getId).toList());
    }

    @Test
    void registrationNumberChecks() {
        assertTrue(vehicleRepository.existsByRegistrationNo("AB12345"));
        assertFalse(vehicleRepository.existsByRegistrationNo("ZZ99999"));
        assertFalse(vehicleRepository.existsByRegistrationNoAndIdNot("AB12345", yaris.getId()));
        assertTrue(vehicleRepository.existsByRegistrationNoAndIdNot("AB12345", golf.getId()));
    }

    @Test
    void locationChecksForVehiclesAndReservations() {
        assertTrue(vehicleRepository.existsByLocation_Id(horsens.getId()));
        assertFalse(vehicleRepository.existsByLocation_Id(aarhus.getId()));

        assertFalse(reservationRepository.existsByLocationId(horsens.getId()));
        book(yaris, DAY, DAY.plusDays(1), ReservationStatus.PENDING);
        assertTrue(reservationRepository.existsByLocationId(horsens.getId()));
    }

    @Test
    void lockedLookupFindsTheVehicle() {
        assertEquals("Toyota Yaris", vehicleRepository.findByIdForUpdate(yaris.getId()).orElseThrow().getModel());
        assertTrue(vehicleRepository.findByIdForUpdate(9999).isEmpty());
    }

    // ---------- ReservationRepository, other queries ----------

    @Test
    void customerReservationsAreNewestFirst() {
        book(yaris, DAY, DAY.plusDays(1), ReservationStatus.PENDING);
        book(golf, DAY.plusDays(10), DAY.plusDays(11), ReservationStatus.PENDING);

        List<Reservation> reservations = reservationRepository.findByCustomer_IdOrderByPickupDateDesc(anna.getId());

        assertEquals(List.of(DAY.plusDays(10), DAY),
                reservations.stream().map(Reservation::getPickupDate).toList());
    }

    @Test
    void activeReservationsAreFoundByStatus() {
        book(yaris, DAY, DAY.plusDays(1), ReservationStatus.COMPLETED);
        assertFalse(reservationRepository.existsByVehicleIdAndStatusIn(yaris.getId(), ReservationStatus.BLOCKING));

        book(yaris, DAY.plusDays(5), DAY.plusDays(6), ReservationStatus.APPROVED);
        assertTrue(reservationRepository.existsByVehicleIdAndStatusIn(yaris.getId(), ReservationStatus.BLOCKING));
    }

    // ---------- UserRepository and CustomerRepository ----------

    @Test
    void findByEmailReturnsTheRightSubtype() {
        userRepository.save(new Employee("Bob", "bob@drivia.dk", "staff123", "E-01"));

        User customer = userRepository.findByEmail("anna@mail.dk").orElseThrow();
        User employee = userRepository.findByEmail("bob@drivia.dk").orElseThrow();

        assertInstanceOf(Customer.class, customer);
        assertInstanceOf(Employee.class, employee);
        assertEquals("E-01", ((Employee) employee).getEmployeeNo());
    }

    @Test
    void customerRepositoryDoesNotFindEmployees() {
        Employee bob = userRepository.save(new Employee("Bob", "bob@drivia.dk", "staff123", "E-01"));

        assertTrue(customerRepository.findById(anna.getId()).isPresent());
        assertTrue(customerRepository.findById(bob.getId()).isEmpty());
    }

    @Test
    void emailMustBeUnique() {
        assertTrue(userRepository.existsByEmail("anna@mail.dk"));

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(new Customer("Other", "anna@mail.dk", "secret2", "87654321")));
    }
}
