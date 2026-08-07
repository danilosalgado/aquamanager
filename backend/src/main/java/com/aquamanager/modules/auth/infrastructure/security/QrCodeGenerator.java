package com.aquamanager.modules.auth.infrastructure.security;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.stereotype.Component;

/** Gera QR Codes (data URI PNG) para provisionamento de 2FA em apps autenticadores. */
@Component
public class QrCodeGenerator {

    private static final int SIZE = 260;

    public String gerarDataUri(String conteudo) {
        try {
            var matrix = new QRCodeWriter().encode(conteudo, BarcodeFormat.QR_CODE, SIZE, SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Falha ao gerar QR Code", ex);
        }
    }
}
