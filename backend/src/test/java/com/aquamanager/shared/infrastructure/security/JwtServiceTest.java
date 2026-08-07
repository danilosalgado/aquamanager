package com.aquamanager.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aquamanager.shared.domain.Role;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties(
            "unit-test-secret-key-with-at-least-32-characters", 15, 30, "aquamanager-test");
    private final JwtService jwtService = new JwtService(properties);

    @Test
    void deveGerarEValidarTokenComAsMesmasClaims() {
        UUID userId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();

        String token = jwtService.generateAccessToken(userId, empresaId, "joao@fazenda.com.br", "João", Role.GERENTE);

        var usuario = jwtService.parse(token).orElseThrow();

        assertThat(usuario.getUserId()).isEqualTo(userId);
        assertThat(usuario.getEmpresaId()).isEqualTo(empresaId);
        assertThat(usuario.getUsername()).isEqualTo("joao@fazenda.com.br");
        assertThat(usuario.getRole()).isEqualTo(Role.GERENTE);
        assertThat(usuario.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_GERENTE");
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "a@b.com", "A", Role.CONSULTOR);
        String adulterado = token.substring(0, token.length() - 4) + "abcd";

        assertThat(jwtService.parse(adulterado)).isEmpty();
    }

    @Test
    void deveRejeitarTokenComEmissorDiferente() {
        JwtService outroEmissor = new JwtService(
                new JwtProperties(properties.accessSecret(), 15, 30, "outro-emissor"));
        String token = outroEmissor.generateAccessToken(UUID.randomUUID(), UUID.randomUUID(), "a@b.com", "A", Role.CONSULTOR);

        assertThat(jwtService.parse(token)).isEmpty();
    }
}
