package com.aquamanager.modules.auth.infrastructure.persistence;

import com.aquamanager.modules.auth.domain.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByEmpresaId(UUID empresaId);

    Page<Usuario> findByEmpresaId(UUID empresaId, Pageable pageable);
}
