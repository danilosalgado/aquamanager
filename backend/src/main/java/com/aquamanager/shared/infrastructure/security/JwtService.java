package com.aquamanager.shared.infrastructure.security;

import com.aquamanager.shared.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Emissão e validação dos access tokens JWT (stateless — sem consulta ao banco). */
@Service
public class JwtService {

    private static final String CLAIM_EMPRESA_ID = "empresaId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NOME = "nome";

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.accessSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, UUID empresaId, String email, String nome, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(userId.toString())
                .claim(CLAIM_EMPRESA_ID, empresaId.toString())
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_NOME, nome)
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessExpirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            UUID empresaId = UUID.fromString(claims.get(CLAIM_EMPRESA_ID, String.class));
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            String nome = claims.get(CLAIM_NOME, String.class);
            String email = claims.get("email", String.class);
            return Optional.of(new AuthenticatedUser(userId, empresaId, email, nome, role));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long accessExpirationSeconds() {
        return properties.accessExpirationMinutes() * 60;
    }
}
