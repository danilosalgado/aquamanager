package com.aquamanager.modules.tenant.infrastructure.mapper;

import com.aquamanager.modules.tenant.application.dto.AssinaturaResponse;
import com.aquamanager.modules.tenant.domain.Assinatura;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PlanoMapper.class)
public interface AssinaturaMapper {
    AssinaturaResponse toResponse(Assinatura assinatura);
}
