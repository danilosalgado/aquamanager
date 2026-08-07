package com.aquamanager.modules.auth.infrastructure.persistence;

import com.aquamanager.modules.auth.domain.EmailConfirmationToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> findByTokenHash(String tokenHash);
}
