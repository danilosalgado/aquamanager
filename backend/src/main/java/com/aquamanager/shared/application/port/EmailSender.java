package com.aquamanager.shared.application.port;

/**
 * Porta para envio de e-mails transacionais (confirmação de cadastro, reset de senha, etc).
 * Implementações: {@code ConsoleEmailSender} (padrão, loga o conteúdo — zero configuração)
 * e {@code SmtpEmailSender} (produção, ativada via app.email.enabled=true).
 */
public interface EmailSender {
    void enviar(String destinatario, String assunto, String corpoHtml);
}
