package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.entity.Customer;
import dk.via.sep3.drivia.tier3.entity.Location;
import dk.via.sep3.drivia.tier3.entity.Vehicle;
import dk.via.sep3.drivia.tier3.repository.CustomerRepository;
import dk.via.sep3.drivia.tier3.repository.LocationRepository;
import dk.via.sep3.drivia.tier3.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Many: several customers book the same car for the same dates at the same time.
// Runs against an in-memory H2 database, so the row lock in findByIdForUpdate is really used.
@SpringBootTest(properties = {
        "spring.grpc.server.port=0",
        "spring.datasource.url=jdbc:h2:mem:concurrency;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create"
})
class ReservationConcurrencyTest {

    private static final int CUSTOMERS = 8;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void onlyOneOfManySimultaneousBookingsSucceeds() throws InterruptedException {
        Location horsens = locationRepository.save(new Location("Horsens", "Banegaardsgade 2"));
        Vehicle golf = vehicleRepository.save(new Vehicle("CD12345", "VW Golf", LocalDate.of(2022, 6, 1), 130,
                "Compact", "Manual", 450, horsens));
        LocalDateTime pickup = LocalDate.now().plusDays(10).atTime(10, 0);

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger refused = new AtomicInteger();
        AtomicInteger otherErrors = new AtomicInteger();
        Thread[] threads = new Thread[CUSTOMERS];

        for (int i = 0; i < CUSTOMERS; i++) {
            int customerId = customerRepository.save(
                    new Customer("Customer " + i, "customer" + i + "@mail.dk", "secret1", "1234567" + i)).getId();
            threads[i] = new Thread(() -> {
                try {
                    start.await();
                    reservationService.createReservation(customerId, golf.getId(), pickup, pickup.plusDays(2));
                    succeeded.incrementAndGet();
                } catch (IllegalStateException e) {
                    refused.incrementAndGet();
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                }
            });
            threads[i].start();
        }

        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(1, succeeded.get());
        assertEquals(CUSTOMERS - 1, refused.get());
        assertEquals(0, otherErrors.get());
    }
}
