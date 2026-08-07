package com.aquamanager.modules.fornecedor.application;

import com.aquamanager.modules.fornecedor.application.dto.FornecedorRequest;
import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FornecedorService {

    Page<Fornecedor> listar(UUID empresaId, String busca, Pageable pageable);

    Fornecedor buscar(UUID empresaId, UUID fornecedorId);

    Fornecedor criar(UUID empresaId, FornecedorRequest request);

    Fornecedor atualizar(UUID empresaId, UUID fornecedorId, FornecedorRequest request);

    void remover(UUID empresaId, UUID fornecedorId);
}
