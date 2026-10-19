package co.judisa.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String username,
        String nombreCompleto,
        boolean debeCambiarPassword) {
}
