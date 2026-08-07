package com.aquamanager.shared.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aplica o isolamento multi-tenant no início de cada transação de negócio,
 * delegando para {@link TenantSessionManager}.
 *
 * Ordem: {@code @EnableTransactionManagement(order = 0)} garante que o advisor
 * transacional é o mais externo; este aspecto (order = 10) roda por dentro dele,
 * ou seja, DEPOIS que a transação (e a conexão JDBC) já foi aberta — condição
 * necessária para "SET LOCAL" ter efeito.
 *
 * Requisições sem tenant no contexto (ex.: cadastro público, login) simplesmente
 * não aplicam a restrição — o próprio caso de uso não deveria precisar dela.
 */
@Aspect
@Component
@Order(10)
@RequiredArgsConstructor
public class TenantRlsAspect {

    private final TenantSessionManager tenantSessionManager;

    @Around("@within(org.springframework.transaction.annotation.Transactional) "
            + "|| @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object applyTenantScope(ProceedingJoinPoint joinPoint) throws Throwable {
        tenantSessionManager.activate(TenantContext.getTenantId());
        return joinPoint.proceed();
    }
}
