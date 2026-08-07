package com.aquamanager.shared.infrastructure.security;

import com.aquamanager.shared.domain.exception.ForbiddenException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        return user;
    }

    public static UUID currentEmpresaId() {
        return currentUser().getEmpresaId();
    }

    public static UUID currentUserId() {
        return currentUser().getUserId();
    }
}
