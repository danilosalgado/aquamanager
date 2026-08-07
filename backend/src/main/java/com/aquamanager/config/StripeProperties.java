package com.aquamanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * priceId identifica o Price recorrente mensal (R$120) criado no Dashboard do Stripe
 * para o plano único da plataforma — configurado ali, não via API, para permitir ajuste
 * de preço/promoções sem deploy.
 */
@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(boolean enabled, String secretKey, String webhookSecret, String priceId) {
}
