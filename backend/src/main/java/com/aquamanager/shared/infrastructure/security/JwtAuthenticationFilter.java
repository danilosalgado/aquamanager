package com.aquamanager.shared.infrastructure.security;

import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extrai e valida o Bearer token em cada requisição, populando o
 * {@link SecurityContextHolder} (autorização) e o {@link TenantContext} (isolamento
 * multi-tenant, consumido por {@code TenantRlsAspect}).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Endpoints que estabelecem uma identidade nova (login) ou não pressupõem
     * nenhuma — nunca devem herdar tenant/autenticação de um Bearer token
     * incidentalmente presente (ex.: sessão antiga ainda em memória no
     * front-end). Sem isso, um token velho porém válido faz o RLS escopar a
     * busca do usuário pelo tenant errado, e o login falha com credenciais
     * corretas.
     */
    private static final Set<String> ENDPOINTS_SEM_CONTEXTO_PREVIO = Set.of(
            "/api/v1/auth/login", "/api/v1/auth/register");

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!ENDPOINTS_SEM_CONTEXTO_PREVIO.contains(request.getRequestURI())) {
                extractToken(request).flatMap(jwtService::parse).ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    TenantContext.setTenantId(user.getEmpresaId());
                });
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return Optional.empty();
    }
}
