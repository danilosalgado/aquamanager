package com.aquamanager.modules.fornecedor.application;

import com.aquamanager.modules.fornecedor.application.dto.FornecedorRequest;
import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import com.aquamanager.modules.fornecedor.infrastructure.persistence.FornecedorRepository;
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
public class FornecedorServiceImpl implements FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Fornecedor> listar(UUID empresaId, String busca, Pageable pageable) {
        if (StringUtils.hasText(busca)) {
            return fornecedorRepository.findByEmpresaIdAndNomeContainingIgnoreCase(empresaId, busca, pageable);
        }
        return fornecedorRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Fornecedor buscar(UUID empresaId, UUID fornecedorId) {
        return buscarDaEmpresa(empresaId, fornecedorId);
    }

    @Override
    @Transactional
    public Fornecedor criar(UUID empresaId, FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setEmpresaId(empresaId);
        aplicarCampos(fornecedor, request);
        return fornecedorRepository.save(fornecedor);
    }

    @Override
    @Transactional
    public Fornecedor atualizar(UUID empresaId, UUID fornecedorId, FornecedorRequest request) {
        Fornecedor fornecedor = buscarDaEmpresa(empresaId, fornecedorId);
        aplicarCampos(fornecedor, request);
        return fornecedor;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID fornecedorId) {
        Fornecedor fornecedor = buscarDaEmpresa(empresaId, fornecedorId);
        fornecedorRepository.delete(fornecedor);
    }

    private Fornecedor buscarDaEmpresa(UUID empresaId, UUID fornecedorId) {
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", fornecedorId));
        if (!fornecedor.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fornecedor", fornecedorId);
        }
        return fornecedor;
    }

    private void aplicarCampos(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.setNome(request.nome());
        fornecedor.setDocumento(request.documento());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setEmail(request.email());
        fornecedor.setProdutosFornecidos(request.produtosFornecidos());
        fornecedor.setObservacoes(request.observacoes());
    }
}
