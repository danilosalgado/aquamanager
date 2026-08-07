package com.aquamanager.shared.infrastructure.security;

import com.aquamanager.shared.domain.Role;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Principal autenticado, reconstruído a partir das claims do JWT — sem round-trip
 * ao banco a cada requisição (autenticação stateless de verdade).
 */
@Getter
public class AuthenticatedUser extends User {

    private final UUID userId;
    private final UUID empresaId;
    private final Role role;
    private final String nome;

    public AuthenticatedUser(UUID userId, UUID empresaId, String email, String nome, Role role) {
        super(email, "", authorities(role));
        this.userId = userId;
        this.empresaId = empresaId;
        this.role = role;
        this.nome = nome;
    }

    private static Collection<? extends GrantedAuthority> authorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
