package com.aquamanager.shared.infrastructure.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Limitador de taxa em memória, por chave (ex.: IP ou e-mail), janela fixa.
 * Suficiente para uma instância única; em produção com múltiplas instâncias o
 * ponto de extensão natural é trocar por um backend Redis (mesma interface).
 */
@Component
public class RateLimiter {

    private record Window(Instant startedAt, AtomicInteger count) {
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * @return true se a chamada está dentro do limite permitido (e deve prosseguir).
     */
    public boolean tryAcquire(String key, int maxAttempts, java.time.Duration windowDuration) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.startedAt().plus(windowDuration).isBefore(now)) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return window.count().get() <= maxAttempts;
    }

    public void reset(String key) {
        windows.remove(key);
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    void cleanup() {
        Instant threshold = Instant.now().minus(java.time.Duration.ofHours(1));
        windows.entrySet().removeIf(e -> e.getValue().startedAt().isBefore(threshold));
    }
}
