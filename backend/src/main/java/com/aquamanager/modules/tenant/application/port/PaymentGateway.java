package com.aquamanager.modules.tenant.application.port;

import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.Plano;

/**
 * Porta (Strategy/Gateway) para o provedor de pagamentos. Implementações:
 *  - {@code MockPaymentGateway}: ativa por padrão (asaas e stripe desabilitados), simula
 *    sucesso imediato — permite rodar toda a plataforma sem credenciais reais.
 *  - {@code AsaasPaymentGateway}: integração real com a API do Asaas (sandbox/produção),
 *    ativada via app.asaas.enabled=true + ASAAS_API_KEY.
 *  - {@code StripePaymentGateway}: integração real com a API do Stripe (checkout hospedado +
 *    portal do cliente), ativada via app.stripe.enabled=true + STRIPE_SECRET_KEY.
 *
 * Trocar de implementação é apenas configuração — nenhum código cliente muda.
 *
 * criarCheckoutSession/criarPortalSession têm implementação default que lança
 * UnsupportedOperationException porque só fazem sentido para gateways com fluxo de
 * checkout hospedado (Stripe); Mock e Asaas seguem o fluxo síncrono de criarAssinatura.
 */
public interface PaymentGateway {

    PaymentCustomerResult criarCliente(Empresa empresa);

    PaymentSubscriptionResult criarAssinatura(Empresa empresa, String customerId, Plano plano);

    void cancelarAssinatura(String subscriptionId);

    default CheckoutSessionResult criarCheckoutSession(Empresa empresa, String customerId, Plano plano,
                                                         String successUrl, String cancelUrl) {
        throw new UnsupportedOperationException(
                "Checkout hospedado não é suportado por este gateway de pagamento.");
    }

    default String criarPortalSession(String customerId, String returnUrl) {
        throw new UnsupportedOperationException(
                "Portal de gerenciamento de assinatura não é suportado por este gateway de pagamento.");
    }

    record PaymentCustomerResult(String customerId) {
    }

    record PaymentSubscriptionResult(String subscriptionId, java.time.LocalDate proximoVencimento) {
    }

    /**
     * customerId vem preenchido quando o gateway já cria a assinatura de forma síncrona ao
     * gerar o link de checkout (Asaas: a assinatura existe desde já, com uma fatura em
     * aberto) — nesse caso o service layer persiste uma {@code Assinatura} PENDENTE
     * imediatamente. Fica null quando o gateway só cria a assinatura após o pagamento
     * (Stripe: o webhook checkout.session.completed é quem informa cliente/assinatura).
     */
    record CheckoutSessionResult(String sessionId, String checkoutUrl, String customerId) {
    }
}
