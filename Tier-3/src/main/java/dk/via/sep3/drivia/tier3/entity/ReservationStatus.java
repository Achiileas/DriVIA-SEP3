package dk.via.sep3.drivia.tier3.entity;

import java.util.List;

// PENDING -> APPROVED -> ACTIVE -> COMPLETED
// PENDING or APPROVED -> CANCELLED
public enum ReservationStatus {
    PENDING,
    APPROVED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    // Reservations with these statuses block the vehicle for their dates
    public static final List<ReservationStatus> BLOCKING = List.of(PENDING, APPROVED, ACTIVE);
}