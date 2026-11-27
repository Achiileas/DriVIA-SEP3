
package dk.via.sep3.drivia.tier2.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

  private final JwtEncoder jwtEncoder;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-ms}") long expirationMs) {

    SecretKey key = new SecretKeySpec(
        secret.getBytes(StandardCharsets.UTF_8),
        "HmacSHA256"
    );

    this.jwtEncoder = new NimbusJwtEncoder(
        new ImmutableSecret<SecurityContext>(key)
    );

    this.expirationMs = expirationMs;
  }

  public String generateToken(int userId, String name, String email, String role) {

    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("drivia-tier2")
        .issuedAt(now)
        .expiresAt(now.plus(expirationMs, ChronoUnit.MILLIS))
        .subject(email)
        .claim("userId", userId)
        .claim("name", name)
        .claim("role", role)
        .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

    return jwtEncoder.encode(
        JwtEncoderParameters.from(header, claims)
    ).getTokenValue();
  }
}
