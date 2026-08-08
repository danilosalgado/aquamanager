package com.aquamanager.modules.tenant.infrastructure.gateway;

import com.aquamanager.config.StripeProperties;
import com.aquamanager.modules.tenant.application.port.PaymentGateway;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.Plano;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.billingportal.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.billingportal.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Integração real com a API do Stripe (https://stripe.com/docs/api), habilitada apenas
 * quando app.stripe.enabled=true e STRIPE_SECRET_KEY está configurado. Usa o Checkout
 * hospedado pelo próprio Stripe (mode=subscription) — o cartão nunca trafega pelo backend
 * do AquaManager — e o Portal do Cliente do Stripe cobre troca de forma de pagamento e
 * cancelamento sem exigir UI própria para isso.
 *
 * criarAssinatura não é suportado diretamente: no Stripe, a assinatura só é criada quando
 * o cliente conclui o pagamento na página hospedada do Checkout (webhook
 * checkout.session.completed) — não existe criação síncrona sem antes capturar um método
 * de pagamento. Use criarCheckoutSession.
 */
@Component
@ConditionalOnProperty(prefix = "app.stripe", name = "enabled", havingValue = "true")
public class StripePaymentGateway implements PaymentGateway {

    private final StripeProperties properties;

    public StripePaymentGateway(StripeProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void configurarApiKey() {
        Stripe.apiKey = properties.secretKey();
    }

    @Override
    public PaymentCustomerResult criarCliente(Empresa empresa) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(empresa.getNome())
                    .setEmail(empresa.getEmail())
                    .putMetadata("empresaId", empresa.getId().toString())
                    .build();
            Customer customer = Customer.create(params);
            return new PaymentCustomerResult(customer.getId());
        } catch (StripeException e) {
            throw new BusinessException("STRIPE_ERROR", "Falha ao criar cliente no Stripe: " + e.getMessage());
        }
    }

    @Override
    public PaymentSubscriptionResult criarAssinatura(Empresa empresa, String customerId, Plano plano) {
        throw new UnsupportedOperationException(
                "StripePaymentGateway não cria assinaturas diretamente — use criarCheckoutSession.");
    }

    @Override
    public void cancelarAssinatura(String subscriptionId) {
        try {
            Subscription.retrieve(subscriptionId).cancel();
        } catch (StripeException e) {
            throw new BusinessException("STRIPE_ERROR", "Falha ao cancelar assinatura no Stripe: " + e.getMessage());
        }
    }

    @Override
    public CheckoutSessionResult criarCheckoutSession(Empresa empresa, String customerId, Plano plano,
                                                        String successUrl, String cancelUrl) {
        try {
            com.stripe.param.checkout.SessionCreateParams.Builder builder = com.stripe.param.checkout.SessionCreateParams.builder()
                    .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(empresa.getId().toString())
                    .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                            .setPrice(properties.priceId())
                            .setQuantity(1L)
                            .build());

            if (customerId != null) {
                builder.setCustomer(customerId);
            } else {
                builder.setCustomerEmail(empresa.getEmail());
            }

            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(builder.build());
            return new CheckoutSessionResult(session.getId(), session.getUrl(), null);
        } catch (StripeException e) {
            throw new BusinessException("STRIPE_ERROR", "Falha ao criar sessão de checkout no Stripe: " + e.getMessage());
        }
    }

    @Override
    public String criarPortalSession(String customerId, String returnUrl) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setCustomer(customerId)
                    .setReturnUrl(returnUrl)
                    .build();
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new BusinessException("STRIPE_ERROR", "Falha ao criar sessão do portal no Stripe: " + e.getMessage());
        }
    }
}
