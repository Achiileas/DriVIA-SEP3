package dk.via.sep3.drivia.tier2.dto.response;

public record AuthResponse(
    int userId,
    String name,
    String email,
    String role,
    String token
) {
}
