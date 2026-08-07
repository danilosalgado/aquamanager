package com.aquamanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record TenantProperties(int trialDays) {
}
