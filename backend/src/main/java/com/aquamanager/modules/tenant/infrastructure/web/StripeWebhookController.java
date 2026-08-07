package com.aquamanager.modules.tenant.infrastructure.web;

import com.aquamanager.config.StripeProperties;
import com.aquamanager.modules.tenant.application.AssinaturaService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe eventos de webhook do Stripe. Diferente do webhook do Asaas (JSON simples +
 * token compartilhado num header), o Stripe exige verificação criptográfica HMAC-SHA256
 * do corpo bruto da requisição via header "Stripe-Signature" — por isso o body é lido
 * diretamente do HttpServletRequest em vez de desserializado pelo Spring (@RequestBody
 * já teria alterado/consumido os bytes antes da verificação, invalidando a assinatura).
 * Documentação: https://stripe.com/docs/webhooks
 *
 * Nas versões atuais da API do Stripe (2024-xx+), o ID da assinatura não vem mais direto
 * em Invoice.getSubscription() — está em invoice.getParent().getSubscriptionDetails()
 * .getSubscription(). Em checkout.session.completed o ID continua direto em
 * Session.getSubscription().
 *
 * EventDataObjectDeserializer.getObject() retorna Optional.empty() em silêncio (sem
 * exceção, sem log) quando a versão da API associada ao evento diverge da que o SDK
 * conhece — o que faz o evento ser "processado" com 200 OK sem que nada aconteça de
 * fato. Por isso todo acesso ao payload passa por resolverObjeto(), que cai para
 * deserializeUnsafe() (ignora a checagem de compatibilidade de versão) e loga os dois
 * casos de falha, em vez de silenciar.
 */
@RestController
@RequestMapping("/api/v1/billing/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final AssinaturaService assinaturaService;
    private final StripeProperties stripeProperties;

    @PostMapping
    public ResponseEntity<Void> receberEvento(HttpServletRequest request,
                                               @RequestHeader("Stripe-Signature") String assinaturaHeader) throws IOException {
        String payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        Event event;
        try {
            event = Webhook.constructEvent(payload, assinaturaHeader, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Assinatura de webhook do Stripe inválida.");
            return ResponseEntity.status(400).build();
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = (Session) resolverObjeto(event);
                if (session == null) {
                    break;
                }
                if (session.getClientReferenceId() != null && session.getSubscription() != null) {
                    UUID empresaId = UUID.fromString(session.getClientReferenceId());
                    assinaturaService.ativarAssinaturaViaCheckout(empresaId, session.getCustomer(), session.getSubscription());
                } else {
                    log.warn("checkout.session.completed sem clientReferenceId/subscription: {}", session.getId());
                }
            }
            case "invoice.paid" -> {
                String subscriptionId = subscriptionIdDaFatura((Invoice) resolverObjeto(event));
                if (subscriptionId != null) {
                    assinaturaService.processarEventoPagamento(subscriptionId, "PAYMENT_CONFIRMED");
                }
            }
            case "invoice.payment_failed" -> {
                String subscriptionId = subscriptionIdDaFatura((Invoice) resolverObjeto(event));
                if (subscriptionId != null) {
                    assinaturaService.processarEventoPagamento(subscriptionId, "PAYMENT_OVERDUE");
                }
            }
            case "customer.subscription.deleted" -> {
                Subscription subscription = (Subscription) resolverObjeto(event);
                if (subscription != null) {
                    assinaturaService.processarEventoPagamento(subscription.getId(), "SUBSCRIPTION_DELETED");
                }
            }
            default -> log.debug("Evento de webhook do Stripe ignorado: {}", event.getType());
        }

        return ResponseEntity.ok().build();
    }

    private static Object resolverObjeto(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        return deserializer.getObject().orElseGet(() -> {
            log.warn("Deserialização direta falhou para o evento {} ({}) — provável divergência de versão "
                            + "da API do Stripe; tentando deserializeUnsafe.",
                    event.getId(), event.getType());
            try {
                return deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Falha ao desserializar payload do evento {} ({}) do Stripe.", event.getId(), event.getType(), e);
                return null;
            }
        });
    }

    private static String subscriptionIdDaFatura(Invoice invoice) {
        if (invoice == null || invoice.getParent() == null || invoice.getParent().getSubscriptionDetails() == null) {
            return null;
        }
        return invoice.getParent().getSubscriptionDetails().getSubscription();
    }
}
