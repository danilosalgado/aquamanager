package com.aquamanager.modules.tenant.infrastructure.gateway;

import com.aquamanager.modules.tenant.application.port.PaymentGateway;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.Plano;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Gateway padrão para desenvolvimento: simula sucesso imediato, sem credenciais. */
@Component
@ConditionalOnProperty(name = {"app.asaas.enabled", "app.stripe.enabled"}, havingValue = "false", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    @Override
    public PaymentCustomerResult criarCliente(Empresa empresa) {
        String customerId = "mock_cus_" + UUID.randomUUID();
        log.info("[MockPaymentGateway] Cliente simulado criado para empresa {}: {}", empresa.getId(), customerId);
        return new PaymentCustomerResult(customerId);
    }

    @Override
    public PaymentSubscriptionResult criarAssinatura(Empresa empresa, String customerId, Plano plano) {
        String subscriptionId = "mock_sub_" + UUID.randomUUID();
        log.info("[MockPaymentGateway] Assinatura simulada criada para empresa {} no plano {}: {}",
                empresa.getId(), plano.getCodigo(), subscriptionId);
        return new PaymentSubscriptionResult(subscriptionId, LocalDate.now().plusMonths(1));
    }

    @Override
    public void cancelarAssinatura(String subscriptionId) {
        log.info("[MockPaymentGateway] Assinatura simulada cancelada: {}", subscriptionId);
    }
}
