package com.aquamanager.modules.tenant.application;

import com.aquamanager.modules.tenant.application.port.PaymentGateway.CheckoutSessionResult;
import com.aquamanager.modules.tenant.domain.Assinatura;
import java.util.UUID;

public interface AssinaturaService {

    Assinatura obterAtual(UUID empresaId);

    /** Troca o plano da empresa, criando a assinatura correspondente no gateway de pagamento. */
    Assinatura alterarPlano(UUID empresaId, String codigoPlano);

    void cancelar(UUID empresaId);

    /** Processa um evento de webhook do gateway de pagamento (confirmação/atraso/cancelamento). */
    void processarEventoPagamento(String gatewaySubscriptionId, String evento);

    /**
     * Cria uma sessão de checkout hospedada para o plano único da plataforma. Em gateways
     * sem checkout hospedado (Mock/Asaas), cai automaticamente no fluxo síncrono de
     * {@link #alterarPlano} e retorna a própria URL de sucesso.
     */
    CheckoutSessionResult criarCheckoutSession(UUID empresaId);

    /** Cria uma sessão do portal de gerenciamento de assinatura (troca de cartão, cancelamento). */
    String criarPortalSession(UUID empresaId);

    /** Ativa a assinatura da empresa a partir de um checkout concluído no gateway (webhook). */
    void ativarAssinaturaViaCheckout(UUID empresaId, String customerId, String subscriptionId);
}
