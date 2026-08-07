package com.aquamanager.modules.auth.infrastructure.persistence;

import com.aquamanager.modules.auth.domain.LoginLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {
}
