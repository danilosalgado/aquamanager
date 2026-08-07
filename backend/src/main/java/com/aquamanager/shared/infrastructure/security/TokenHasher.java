package com.aquamanager.shared.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Gera tokens opacos (refresh, reset de senha, confirmação de e-mail) e seus hashes
 * SHA-256 para armazenamento — o valor bruto do token nunca é persistido, apenas
 * comparado por hash na hora do uso (mesmo princípio de senhas, adaptado a tokens
 * de uso único/curta duração).
 */
@Component
public class TokenHasher {

    private final SecureRandom secureRandom = new SecureRandom();

    public String gerarTokenOpaco() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String tokenBruto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenBruto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
