
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

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerReservationControllerTest {

  @Mock
  private ReservationService reservationService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    CustomerReservationController controller =
        new CustomerReservationController(reservationService);

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
        LocalDate.of(2026, 12, 1),
        LocalDate.of(2026, 12, 5),
        status,
        2000
    );
  }

  // Z - Zero
  @Test
  void getCustomerReservationsReturnsEmptyList() throws Exception {
    when(reservationService.getCustomerReservations(1)).thenReturn(List.of());

    mockMvc.perform(get("/api/customer/reservations")
            .param("customerId", "1"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(reservationService).getCustomerReservations(1);
  }

  // O - One
  @Test
  void getReservationReturnsOneReservation() throws Exception {
    when(reservationService.getReservation(1))
        .thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.vehicleModel").value("Toyota Corolla"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).getReservation(1);
  }

  // M - Many
  @Test
  void getCustomerReservationsReturnsMultipleReservations() throws Exception {
    when(reservationService.getCustomerReservations(1)).thenReturn(List.of(
        createReservation(1, "PENDING"),
        createReservation(2, "APPROVED"),
        createReservation(3, "COMPLETED")
    ));

    mockMvc.perform(get("/api/customer/reservations")
            .param("customerId", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].status").value("APPROVED"))
        .andExpect(jsonPath("$[2].status").value("COMPLETED"));
  }

  // B - Boundary
  @Test
  void getReservationPassesCorrectIdToService() throws Exception {
    when(reservationService.getReservation(999))
        .thenReturn(createReservation(999, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(999));

    verify(reservationService).getReservation(999);
  }

  // I - Interface
  @Test
  void getReservationReturnsCorrectDatesAndPrice() throws Exception {
    when(reservationService.getReservation(1))
        .thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pickupDate").value("2026-12-01"))
        .andExpect(jsonPath("$.returnDate").value("2026-12-05"))
        .andExpect(jsonPath("$.totalPrice").value(2000));
  }

  // S - Simple scenarios
  @Test
  void createReservationReturnsCreatedReservation() throws Exception {
    when(reservationService.createReservation(
        1,
        2,
        LocalDate.of(2026, 12, 1),
        LocalDate.of(2026, 12, 5)
    )).thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(post("/api/customer/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "customerId": 1,
                                    "vehicleId": 2,
                                    "pickupDate": "2026-12-01",
                                    "returnDate": "2026-12-05"
                                }
                                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).createReservation(
        1,
        2,
        LocalDate.of(2026, 12, 1),
        LocalDate.of(2026, 12, 5)
    );
  }

  @Test
  void modifyReservationReturnsUpdatedReservation() throws Exception {
    when(reservationService.modifyReservation(
        1,
        3,
        LocalDate.of(2026, 12, 2),
        LocalDate.of(2026, 12, 6)
    )).thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(put("/api/customer/reservations/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 3,
                                    "pickupDate": "2026-12-02",
                                    "returnDate": "2026-12-06"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).modifyReservation(
        1,
        3,
        LocalDate.of(2026, 12, 2),
        LocalDate.of(2026, 12, 6)
    );
  }

  @Test
  void cancelReservationReturnsCancelledReservation() throws Exception {
    when(reservationService.cancelReservation(1))
        .thenReturn(createReservation(1, "CANCELLED"));

    mockMvc.perform(put("/api/customer/reservations/1/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("CANCELLED"));

    verify(reservationService).cancelReservation(1);
  }

  // E - Exceptions

  @Test
  void getReservationReturnsNotFoundWhenReservationDoesNotExist()
      throws Exception {

    when(reservationService.getReservation(999))
        .thenThrow(new StatusRuntimeException(Status.NOT_FOUND));

    mockMvc.perform(get("/api/customer/reservations/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void createReservationReturnsBadRequestWhenRequestIsInvalid()
      throws Exception {

    when(reservationService.createReservation(
        1,
        2,
        LocalDate.of(2026, 12, 1),
        LocalDate.of(2026, 12, 5)
    )).thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

    mockMvc.perform(post("/api/customer/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "customerId": 1,
                                    "vehicleId": 2,
                                    "pickupDate": "2026-12-01",
                                    "returnDate": "2026-12-05"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void modifyReservationReturnsConflictWhenReservationCannotBeModified()
      throws Exception {

    when(reservationService.modifyReservation(
        1,
        3,
        LocalDate.of(2026, 12, 2),
        LocalDate.of(2026, 12, 6)
    )).thenThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION));

    mockMvc.perform(put("/api/customer/reservations/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 3,
                                    "pickupDate": "2026-12-02",
                                    "returnDate": "2026-12-06"
                                }
                                """))
        .andExpect(status().isConflict());
  }

  @Test
  void cancelReservationReturnsConflictWhenReservationCannotBeCancelled()
      throws Exception {

    when(reservationService.cancelReservation(1))
        .thenThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION));

    mockMvc.perform(put("/api/customer/reservations/1/cancel"))
        .andExpect(status().isConflict());
  }

  @Test
  void getCustomerReservationsReturnsServiceUnavailableWhenTier3IsUnavailable()
      throws Exception {

    when(reservationService.getCustomerReservations(1))
        .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

    mockMvc.perform(get("/api/customer/reservations")
            .param("customerId", "1"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void getReservationReturnsInternalServerErrorForUnexpectedException()
      throws Exception {

    when(reservationService.getReservation(1))
        .thenThrow(new StatusRuntimeException(Status.INTERNAL));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isInternalServerError());
  }
}
