
package dk.via.sep3.drivia.tier2.controller;

import dk.via.sep3.drivia.tier2.dto.response.ReservationResponse;
import dk.via.sep3.drivia.tier2.service.ReservationService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    Jwt jwt = Jwt.withTokenValue("test-token")
        .header("alg", "none")
        .claim("userId", 1)
        .claim("role", "CUSTOMER")
        .build();

    SecurityContextHolder.getContext().setAuthentication(
        new JwtAuthenticationToken(jwt)
    );

    mockMvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setCustomArgumentResolvers(
            new AuthenticationPrincipalArgumentResolver()
        )
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
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
  void getCustomerReservationsReturnsEmptyList() throws Exception {
    when(reservationService.getCustomerReservations(1)).thenReturn(List.of());

    mockMvc.perform(get("/api/customer/reservations"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(reservationService).getCustomerReservations(1);
  }

  // O - One
  @Test
  void getReservationReturnsOneReservation() throws Exception {
    when(reservationService.getCustomerReservation(1, 1))
        .thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.vehicleModel").value("Toyota Corolla"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).getCustomerReservation(1, 1);
  }

  // M - Many
  @Test
  void getCustomerReservationsReturnsMultipleReservations() throws Exception {
    when(reservationService.getCustomerReservations(1)).thenReturn(List.of(
        createReservation(1, "PENDING"),
        createReservation(2, "APPROVED"),
        createReservation(3, "COMPLETED")
    ));

    mockMvc.perform(get("/api/customer/reservations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[1].status").value("APPROVED"))
        .andExpect(jsonPath("$[2].status").value("COMPLETED"));
  }

  // B - Boundary
  @Test
  void getReservationPassesCorrectIdToService() throws Exception {
    when(reservationService.getCustomerReservation(999, 1))
        .thenReturn(createReservation(999, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(999));

    verify(reservationService).getCustomerReservation(999, 1);
  }

  // I - Interface
  @Test
  void getReservationReturnsCorrectDatesAndPrice() throws Exception {
    when(reservationService.getCustomerReservation(1, 1))
        .thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pickupDate").value("2026-12-01T10:00:00"))
        .andExpect(jsonPath("$.returnDate").value("2026-12-05T10:00:00"))
        .andExpect(jsonPath("$.totalPrice").value(2000));
  }

  // S - Simple scenarios
  @Test
  void createReservationReturnsCreatedReservation() throws Exception {
    when(reservationService.createReservation(
        1,
        2,
        LocalDateTime.of(2026, 12, 1, 10, 0),
        LocalDateTime.of(2026, 12, 5, 10, 0)
    )).thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(post("/api/customer/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 2,
                                    "pickupDate": "2026-12-01T10:00",
                                    "returnDate": "2026-12-05T10:00"
                                }
                                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).createReservation(
        1,
        2,
        LocalDateTime.of(2026, 12, 1, 10, 0),
        LocalDateTime.of(2026, 12, 5, 10, 0)
    );
  }

  @Test
  void modifyReservationReturnsUpdatedReservation() throws Exception {
    when(reservationService.modifyReservation(
        1,
        1,
        3,
        LocalDateTime.of(2026, 12, 2, 10, 0),
        LocalDateTime.of(2026, 12, 6, 10, 0)
    )).thenReturn(createReservation(1, "PENDING"));

    mockMvc.perform(put("/api/customer/reservations/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 3,
                                    "pickupDate": "2026-12-02T10:00",
                                    "returnDate": "2026-12-06T10:00"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(reservationService).modifyReservation(
        1,
        1,
        3,
        LocalDateTime.of(2026, 12, 2, 10, 0),
        LocalDateTime.of(2026, 12, 6, 10, 0)
    );
  }

  @Test
  void cancelReservationReturnsCancelledReservation() throws Exception {
    when(reservationService.cancelCustomerReservation(1, 1))
        .thenReturn(createReservation(1, "CANCELLED"));

    mockMvc.perform(put("/api/customer/reservations/1/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("CANCELLED"));

    verify(reservationService).cancelCustomerReservation(1, 1);
  }

  // E - Exceptions

  @Test
  void getReservationReturnsNotFoundWhenReservationDoesNotExist()
      throws Exception {

    when(reservationService.getCustomerReservation(999, 1))
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
        LocalDateTime.of(2026, 12, 1, 10, 0),
        LocalDateTime.of(2026, 12, 5, 10, 0)
    )).thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT));

    mockMvc.perform(post("/api/customer/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 2,
                                    "pickupDate": "2026-12-01T10:00",
                                    "returnDate": "2026-12-05T10:00"
                                }
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void modifyReservationReturnsConflictWhenReservationCannotBeModified()
      throws Exception {

    when(reservationService.modifyReservation(
        1,
        1,
        3,
        LocalDateTime.of(2026, 12, 2, 10, 0),
        LocalDateTime.of(2026, 12, 6, 10, 0)
    )).thenThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION));

    mockMvc.perform(put("/api/customer/reservations/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                                {
                                    "vehicleId": 3,
                                    "pickupDate": "2026-12-02T10:00",
                                    "returnDate": "2026-12-06T10:00"
                                }
                                """))
        .andExpect(status().isConflict());
  }

  @Test
  void cancelReservationReturnsConflictWhenReservationCannotBeCancelled()
      throws Exception {

    when(reservationService.cancelCustomerReservation(1, 1))
        .thenThrow(new StatusRuntimeException(Status.FAILED_PRECONDITION));

    mockMvc.perform(put("/api/customer/reservations/1/cancel"))
        .andExpect(status().isConflict());
  }

  @Test
  void getCustomerReservationsReturnsServiceUnavailableWhenTier3IsUnavailable()
      throws Exception {

    when(reservationService.getCustomerReservations(1))
        .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

    mockMvc.perform(get("/api/customer/reservations"))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void getReservationReturnsInternalServerErrorForUnexpectedException()
      throws Exception {

    when(reservationService.getCustomerReservation(1, 1))
        .thenThrow(new StatusRuntimeException(Status.INTERNAL));

    mockMvc.perform(get("/api/customer/reservations/1"))
        .andExpect(status().isInternalServerError());
  }
}
