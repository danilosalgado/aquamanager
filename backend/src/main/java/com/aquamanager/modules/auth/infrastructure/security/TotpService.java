package com.aquamanager.modules.auth.infrastructure.security;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * TOTP (RFC 6238) compatível com Google Authenticator / Microsoft Authenticator,
 * implementado apenas com {@code javax.crypto} — sem dependências externas.
 */
@Service
public class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /** Gera um novo segredo aleatório de 160 bits, codificado em Base32. */
    public String gerarSegredo() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return base32Encode(bytes);
    }

    public String gerarUriProvisionamento(String segredo, String emailUsuario) {
        return "otpauth://totp/AquaManager:%s?secret=%s&issuer=AquaManager&digits=%d&period=%d"
                .formatted(emailUsuario, segredo, CODE_DIGITS, TIME_STEP_SECONDS);
    }

    /** Valida o código informado, tolerando ±1 passo (30s) de deriva de relógio. */
    public boolean validar(String segredoBase32, String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }
        long currentStep = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        for (long step = currentStep - 1; step <= currentStep + 1; step++) {
            if (gerarCodigo(segredoBase32, step).equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    private String gerarCodigo(String segredoBase32, long timeStep) {
        try {
            byte[] key = base32Decode(segredoBase32);
            byte[] message = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(message);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar código TOTP", ex);
        }
    }

    private String base32Encode(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                result.append(BASE32_ALPHABET.charAt((buffer >> (bitsLeft - 5)) & 0x1F));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            result.append(BASE32_ALPHABET.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return result.toString();
    }

    private byte[] base32Decode(String encoded) {
        String clean = encoded.trim().toUpperCase().replace("=", "");
        int buffer = 0;
        int bitsLeft = 0;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (char c : clean.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(c);
            if (value < 0) {
                continue;
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out.write((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }
}
