package com.aquamanager.modules.especie.infrastructure.persistence;

import com.aquamanager.modules.especie.domain.Especie;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecieRepository extends JpaRepository<Especie, UUID> {

    /** Catálogo global (empresa_id NULL) + espécies customizadas do tenant, ordenado por nome. */
    List<Especie> findByEmpresaIdIsNullOrEmpresaIdOrderByNomeAsc(UUID empresaId);
}
