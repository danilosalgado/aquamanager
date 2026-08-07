package com.aquamanager.modules.tenant.application;

import com.aquamanager.modules.tenant.application.dto.AtualizarEmpresaRequest;
import com.aquamanager.modules.tenant.domain.Empresa;
import java.util.UUID;

public interface EmpresaService {

    /** Cria uma nova empresa em trial + assinatura correspondente. Fluxo de bootstrap (cadastro público). */
    Empresa criarComTrial(String nome, String documento, String endereco, String cidade, String estado,
                           String telefone, String email);

    Empresa buscarPorId(UUID empresaId);

    Empresa atualizar(UUID empresaId, AtualizarEmpresaRequest request);

    void garantirLimiteTanques(UUID empresaId, long quantidadeAtual);

    void garantirLimiteUsuarios(UUID empresaId, long quantidadeAtual);

    void garantirAcessoLiberado(UUID empresaId);
}
