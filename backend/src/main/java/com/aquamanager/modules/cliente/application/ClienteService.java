package com.aquamanager.modules.cliente.application;

import com.aquamanager.modules.cliente.application.dto.ClienteRequest;
import com.aquamanager.modules.cliente.domain.Cliente;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    Page<Cliente> listar(UUID empresaId, String busca, Pageable pageable);

    Cliente buscar(UUID empresaId, UUID clienteId);

    Cliente criar(UUID empresaId, ClienteRequest request);

    Cliente atualizar(UUID empresaId, UUID clienteId, ClienteRequest request);

    void remover(UUID empresaId, UUID clienteId);
}
