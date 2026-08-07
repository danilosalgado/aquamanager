package com.aquamanager.modules.cliente.infrastructure.mapper;

import com.aquamanager.modules.cliente.application.dto.ClienteResponse;
import com.aquamanager.modules.cliente.domain.Cliente;
import org.springframework.stereotype.Component;

/** Mapper manual (não MapStruct), seguindo o padrão dos demais módulos. */
@Component
public class ClienteMapper {

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDocumento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getObservacoes()
        );
    }
}
