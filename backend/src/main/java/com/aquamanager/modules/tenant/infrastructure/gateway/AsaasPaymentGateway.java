package com.aquamanager.modules.tenant.infrastructure.gateway;

import com.aquamanager.config.AsaasProperties;
import com.aquamanager.modules.tenant.application.port.PaymentGateway;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.Plano;
import com.aquamanager.shared.domain.exception.BusinessException;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Integração real com a API do Asaas (https://docs.asaas.com/), habilitada apenas
 * quando app.asaas.enabled=true e ASAAS_API_KEY está configurado. Cobranças via
 * PIX, boleto e cartão são criadas com billingType=UNDEFINED (o cliente escolhe na
 * fatura hospedada pelo Asaas). Validar contra o ambiente sandbox antes de ativar
 * em produção.
 */
@Component
@ConditionalOnProperty(prefix = "app.asaas", name = "enabled", havingValue = "true")
public class AsaasPaymentGateway implements PaymentGateway {

    private final RestClient restClient;

    public AsaasPaymentGateway(AsaasProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader("User-Agent", "AquaManager")
                .build();
    }

    @Override
    public PaymentCustomerResult criarCliente(Empresa empresa) {
        Map<String, Object> response = restClient.post()
                .uri("/customers")
                .body(Map.of(
                        "name", empresa.getNome(),
                        "cpfCnpj", empresa.getDocumento(),
                        "email", empresa.getEmail(),
                        "phone", empresa.getTelefone() == null ? "" : empresa.getTelefone()
                ))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new BusinessException("ASAAS_ERROR", "Falha ao criar cliente no Asaas.");
        }
        return new PaymentCustomerResult((String) response.get("id"));
    }

    @Override
    public PaymentSubscriptionResult criarAssinatura(Empresa empresa, String customerId, Plano plano) {
        Map<String, Object> response = restClient.post()
                .uri("/subscriptions")
                .body(Map.of(
                        "customer", customerId,
                        "billingType", "UNDEFINED",
                        "cycle", "MONTHLY",
                        "value", plano.getPrecoMensal(),
                        "nextDueDate", LocalDate.now().plusDays(1).toString(),
                        "description", "AquaManager - Plano " + plano.getNome()
                ))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new BusinessException("ASAAS_ERROR", "Falha ao criar assinatura no Asaas.");
        }
        return new PaymentSubscriptionResult((String) response.get("id"), LocalDate.now().plusMonths(1));
    }

    @Override
    public void cancelarAssinatura(String subscriptionId) {
        restClient.delete().uri("/subscriptions/{id}", subscriptionId).retrieve().toBodilessEntity();
    }
}
