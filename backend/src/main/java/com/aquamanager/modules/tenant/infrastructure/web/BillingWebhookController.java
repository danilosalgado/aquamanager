package com.aquamanager.modules.tenant.infrastructure.web;

import com.aquamanager.config.AsaasProperties;
import com.aquamanager.modules.tenant.application.AssinaturaService;
import com.aquamanager.shared.domain.exception.ForbiddenException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe eventos de webhook do Asaas (confirmação/atraso de pagamento).
 * Documentação: https://docs.asaas.com/docs/webhook — o token configurado no
 * painel do Asaas (header "asaas-access-token") deve bater com ASAAS_WEBHOOK_TOKEN.
 */
@RestController
@RequestMapping("/api/v1/billing/webhooks")
@RequiredArgsConstructor
public class BillingWebhookController {

    private static final Logger log = LoggerFactory.getLogger(BillingWebhookController.class);

    private final AssinaturaService assinaturaService;
    private final AsaasProperties asaasProperties;

    @PostMapping("/asaas")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Void> receberEvento(@RequestHeader(value = "asaas-access-token", required = false) String token,
                                               @RequestBody Map<String, Object> payload) {
        if (asaasProperties.webhookToken() != null && !asaasProperties.webhookToken().isBlank()
                && !asaasProperties.webhookToken().equals(token)) {
            throw new ForbiddenException("Token de webhook inválido.");
        }

        String evento = (String) payload.get("event");
        Map<String, Object> payment = (Map<String, Object>) payload.get("payment");
        if (evento == null || payment == null) {
            log.warn("Payload de webhook do Asaas em formato inesperado: {}", payload);
            return ResponseEntity.ok().build();
        }

        String subscriptionId = (String) payment.get("subscription");
        if (subscriptionId != null) {
            assinaturaService.processarEventoPagamento(subscriptionId, evento);
        }
        return ResponseEntity.ok().build();
    }
}
