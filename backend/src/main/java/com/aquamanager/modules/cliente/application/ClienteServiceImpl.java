package com.aquamanager.modules.cliente.application;

import com.aquamanager.modules.cliente.application.dto.ClienteRequest;
import com.aquamanager.modules.cliente.domain.Cliente;
import com.aquamanager.modules.cliente.infrastructure.persistence.ClienteRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Cliente> listar(UUID empresaId, String busca, Pageable pageable) {
        if (StringUtils.hasText(busca)) {
            return clienteRepository.findByEmpresaIdAndNomeContainingIgnoreCase(empresaId, busca, pageable);
        }
        return clienteRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscar(UUID empresaId, UUID clienteId) {
        return buscarDaEmpresa(empresaId, clienteId);
    }

    @Override
    @Transactional
    public Cliente criar(UUID empresaId, ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setEmpresaId(empresaId);
        aplicarCampos(cliente, request);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente atualizar(UUID empresaId, UUID clienteId, ClienteRequest request) {
        Cliente cliente = buscarDaEmpresa(empresaId, clienteId);
        aplicarCampos(cliente, request);
        return cliente;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID clienteId) {
        Cliente cliente = buscarDaEmpresa(empresaId, clienteId);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarDaEmpresa(UUID empresaId, UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!cliente.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        return cliente;
    }

    private void aplicarCampos(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome());
        cliente.setDocumento(request.documento());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());
        cliente.setEndereco(request.endereco());
        cliente.setObservacoes(request.observacoes());
    }
}
