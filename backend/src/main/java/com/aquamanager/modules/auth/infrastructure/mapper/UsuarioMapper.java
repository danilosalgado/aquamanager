package com.aquamanager.modules.auth.infrastructure.mapper;

import com.aquamanager.modules.auth.application.dto.UsuarioResponse;
import com.aquamanager.modules.auth.domain.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponse toResponse(Usuario usuario);
}
