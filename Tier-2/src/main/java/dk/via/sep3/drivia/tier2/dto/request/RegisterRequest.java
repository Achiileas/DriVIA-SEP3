package dk.via.sep3.drivia.tier2.dto.request;

public record RegisterRequest(
    String name,
    String email,
    String password,
    String phoneNumber
) {
}
