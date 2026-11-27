
package dk.via.sep3.drivia.tier2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${jwt.secret}")
  private String secret;

  @Bean
  public JwtDecoder jwtDecoder() {

    SecretKey key = new SecretKeySpec(
        secret.getBytes(StandardCharsets.UTF_8),
        "HmacSHA256"
    );

    return NimbusJwtDecoder.withSecretKey(key)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {

    JwtGrantedAuthoritiesConverter authoritiesConverter =
        new JwtGrantedAuthoritiesConverter();

    authoritiesConverter.setAuthoritiesClaimName("role");
    authoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter authenticationConverter =
        new JwtAuthenticationConverter();

    authenticationConverter.setJwtGrantedAuthoritiesConverter(
        authoritiesConverter
    );

    return authenticationConverter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    return http
        .csrf(csrf -> csrf.disable())

        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )

        .authorizeHttpRequests(auth -> auth

            // Public authentication endpoints
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/error").permitAll()

            // Public vehicle and location information
            .requestMatchers(HttpMethod.GET, "/api/vehicles/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()

            // Only employees can manage vehicles
            .requestMatchers("/api/vehicles/**").hasRole("EMPLOYEE")

            // Only employees can manage locations
            .requestMatchers("/api/locations/**").hasRole("EMPLOYEE")

            // Customer endpoints
            .requestMatchers("/api/customer/**").hasRole("CUSTOMER")

            // Employee endpoints
            .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")

            // Everything else requires authentication
            .anyRequest().authenticated()
        )

        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(
                    jwtAuthenticationConverter()
                )
            )
        )

        .build();
  }
}
