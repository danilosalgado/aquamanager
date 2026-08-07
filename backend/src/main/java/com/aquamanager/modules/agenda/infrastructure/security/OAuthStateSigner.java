package com.aquamanager.modules.agenda.infrastructure.security;

import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.infrastructure.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Assina/valida o parâmetro "state" do fluxo OAuth do Google Calendar.
 *
 * O callback do Google (`GET /integracoes/google/callback`) é necessariamente público —
 * o redirecionamento do navegador não carrega o Bearer token da API. Sem um "state"
 * assinado, um atacante poderia iniciar seu próprio fluxo OAuth, capturar um "code"
 * válido e reenviá-lo com `state=<uuid de outro usuário>` para vincular a própria conta
 * Google à conta alheia (ataque clássico de CSRF/confused-deputy em OAuth). Assinar o
 * state com HMAC (e expirá-lo em poucos minutos) impede isso: o callback só aceita um
 * state que o próprio backend emitiu para aquele usuário autenticado, pouco antes.
 */
@Component
@RequiredArgsConstructor
public class OAuthStateSigner {

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtProperties jwtProperties;

    public String sign(UUID usuarioId) {
        long expiresAt = System.currentTimeMillis() + TTL.toMillis();
        String payload = usuarioId + ":" + expiresAt;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + hmac(payload)).getBytes(StandardCharsets.UTF_8));
    }

    public UUID validar(String state) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] partes = decoded.split(":");
            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato inválido");
            }
            String usuarioIdBruto = partes[0];
            long expiresAt = Long.parseLong(partes[1]);
            String assinaturaRecebida = partes[2];

            String assinaturaEsperada = hmac(usuarioIdBruto + ":" + expiresAt);
            if (!MessageDigest.isEqual(
                    assinaturaEsperada.getBytes(StandardCharsets.UTF_8),
                    assinaturaRecebida.getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("Assinatura inválida");
            }
            if (System.currentTimeMillis() > expiresAt) {
                throw new SecurityException("State expirado");
            }
            return UUID.fromString(usuarioIdBruto);
        } catch (Exception ex) {
            throw new BusinessException("INVALID_OAUTH_STATE", "Link de autorização do Google inválido ou expirado.");
        }
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(jwtProperties.accessSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
