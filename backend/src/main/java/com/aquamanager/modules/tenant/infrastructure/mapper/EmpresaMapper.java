package com.aquamanager.modules.tenant.infrastructure.mapper;

import com.aquamanager.modules.tenant.application.dto.EmpresaResponse;
import com.aquamanager.modules.tenant.domain.Empresa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PlanoMapper.class)
public interface EmpresaMapper {
    EmpresaResponse toResponse(Empresa empresa);
}
