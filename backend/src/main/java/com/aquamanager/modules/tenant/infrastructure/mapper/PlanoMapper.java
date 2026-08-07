package com.aquamanager.modules.tenant.infrastructure.mapper;

import com.aquamanager.modules.tenant.application.dto.PlanoResponse;
import com.aquamanager.modules.tenant.domain.Plano;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanoMapper {
    PlanoResponse toResponse(Plano plano);
}
