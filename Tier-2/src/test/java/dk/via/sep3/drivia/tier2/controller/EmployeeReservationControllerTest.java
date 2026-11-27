package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import dk.via.sep3.drivia.tier2.service.ReservationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EmployeeReservationController controller =
                new EmployeeReservationController(reservationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // Helper method
    private ReservationResponse createReservation(int id, String status) {
        return new ReservationResponse(
                id,
                1,
                2,
                "Toyota Corolla",
                1,
                "Horsens",
                LocalDateTime.of(2026, 12, 1, 10, 0),
                LocalDateTime.of(2026, 12, 5, 10, 0),
                status,
                new BigDecimal("2000.00")
        );
    }

    // Z - Zero
    @Test
    void getAllReservationsReturnsEmptyList() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of());

        mockMvc.perform(get("/api/employee/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(reservationService).getAllReservations();
    }

    // O - One
    @Test
    void getReservationReturnsOneReservation() throws Exception {
        when(reservationService.getReservation(1))
                .thenReturn(createReservation(1, "PENDING"));

        mockMvc.perform(get("/api/employee/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.vehicleModel").value("Toyota Corolla"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(reservationService).getReservation(1);
    }

    // M - Many
    @Test
    void getAllReservationsReturnsMultipleReservations() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(
                createReservation(1, "PENDING"),
                createReservation(2, "APPROVED"),
                createReservation(3, "ACTIVE")
        ));

        mockMvc.perform(get("/api/employee/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[2].status").value("ACTIVE"));
    }

    // B - Boundary
    @Test
    void getReservationPassesCorrectIdToService() throws Exception {
        when(reservationService.getReservation(999))
                .thenReturn(createReservation(999, "PENDING"));

        mockMvc.perform(get("/api/employee/reservations/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(999));

        verify(reservationService).getReservation(999);
    }

    // I - Interface
    @Test
    void getReservationReturnsCorrectDatesAndPrice() throws Exception {
        when(reservationService.getReservation(1))
                .thenReturn(createReservation(1, "PENDING"));

        mockMvc.perform(get("/api/employee/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pickupDate").value("2026-12-01T10:00:00"))
                .andExpect(jsonPath("$.returnDate").value("2026-12-05T10:00:00"))
                .andExpect(jsonPath("$.totalPrice").value(2000.00));
    }

    // S - Simple scenarios
    @Test
    void approveReservationReturnsApprovedReservation() throws Exception {
        when(reservationService.approveReservation(1))
                .thenReturn(createReservation(1, "APPROVED"));

        mockMvc.perform(put("/api/employee/reservations/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(reservationService).approveReservation(1);
    }

    @Test
    void pickupReservationReturnsActiveReservation() throws Exception {
        when(reservationService.pickupReservation(1))
                .thenReturn(createReservation(1, "ACTIVE"));

        mockMvc.perform(put("/api/employee/reservations/1/pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(reservationService).pickupReservation(1);
    }

    @Test
    void returnReservationReturnsCompletedReservation() throws Exception {
        when(reservationService.returnReservation(1))
                .thenReturn(createReservation(1, "COMPLETED"));

        mockMvc.perform(put("/api/employee/reservations/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(reservationService).returnReservation(1);
    }

    // E - Exceptions

    @Test
    void getReservationReturnsNotFoundWhenReservationDoesNotExist()
            throws Exception {

        when(reservationService.getReservation(999))
                .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

        mockMvc.perform(get("/api/employee/reservations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveReservationReturnsConflictWhenReservationCannotBeApproved()
            throws Exception {

        when(reservationService.approveReservation(1))
                .thenThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION));

        mockMvc.perform(put("/api/employee/reservations/1/approve"))
                .andExpect(status().isConflict());
    }

    @Test
    void pickupReservationReturnsBadRequestWhenRequestIsInvalid()
            throws Exception {

        when(reservationService.pickupReservation(1))
                .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

        mockMvc.perform(put("/api/employee/reservations/1/pickup"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnReservationReturnsServiceUnavailableWhenTier3IsUnavailable()
            throws Exception {

        when(reservationService.returnReservation(1))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        mockMvc.perform(put("/api/employee/reservations/1/return"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getAllReservationsReturnsInternalServerErrorForUnexpectedException()
            throws Exception {

        when(reservationService.getAllReservations())
                .thenThrow(new StatusRuntimeException(Status.INTERNAL));

        mockMvc.perform(get("/api/employee/reservations"))
                .andExpect(status().isInternalServerError());
    }
}