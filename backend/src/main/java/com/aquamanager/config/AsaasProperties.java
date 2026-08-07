package com.aquamanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.asaas")
public record AsaasProperties(boolean enabled, String apiKey, String baseUrl, String webhookToken) {
}
