package com.aquamanager.shared.infrastructure.email;

import com.aquamanager.shared.application.port.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Envio padrão para desenvolvimento: apenas loga o conteúdo do e-mail, sem SMTP. */
@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void enviar(String destinatario, String assunto, String corpoHtml) {
        log.info("""
                ==================== E-MAIL (modo console) ====================
                Para: {}
                Assunto: {}
                ----------------------------------------------------------------
                {}
                =================================================================""",
                destinatario, assunto, corpoHtml);
    }
}
