package com.aquamanager.modules.auth.infrastructure.persistence;

import com.aquamanager.modules.auth.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.usuario.id = :usuarioId AND r.revoked = false")
    void revogarTodosDoUsuario(@Param("usuarioId") UUID usuarioId);
}
