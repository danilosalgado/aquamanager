package com.aquamanager.modules.tenant.infrastructure.gateway;

import com.aquamanager.config.AsaasProperties;
import com.aquamanager.modules.tenant.application.port.PaymentGateway;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.Plano;
import com.aquamanager.shared.domain.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
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

    @Override
    public CheckoutSessionResult criarCheckoutSession(Empresa empresa, String customerId, Plano plano,
                                                        String successUrl, String cancelUrl) {
        String custId = customerId != null ? customerId : criarCliente(empresa).customerId();
        PaymentSubscriptionResult assinatura = criarAssinatura(empresa, custId, plano);
        String invoiceUrl = buscarUrlPrimeiraFatura(assinatura.subscriptionId());
        return new CheckoutSessionResult(assinatura.subscriptionId(), invoiceUrl, custId);
    }

    /**
     * A criação da assinatura (billingType=UNDEFINED) já gera a primeira fatura no Asaas;
     * essa fatura hospedada (invoiceUrl) é onde o cliente escolhe PIX/boleto/cartão e paga
     * de fato — é ela que deve ser usada como link de checkout, nunca a própria assinatura.
     */
    @SuppressWarnings("unchecked")
    private String buscarUrlPrimeiraFatura(String subscriptionId) {
        Map<String, Object> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments")
                        .queryParam("subscription", subscriptionId)
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> faturas = response == null ? null : (List<Map<String, Object>>) response.get("data");
        if (faturas == null || faturas.isEmpty() || faturas.get(0).get("invoiceUrl") == null) {
            throw new BusinessException("ASAAS_ERROR", "Nenhuma fatura foi gerada para a assinatura no Asaas.");
        }
        return (String) faturas.get(0).get("invoiceUrl");
    }
}
