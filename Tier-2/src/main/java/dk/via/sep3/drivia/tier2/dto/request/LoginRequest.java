package dk.via.sep3.drivia.tier2.dto.request;

public record LoginRequest(
    String email,
    String password
) {
}
