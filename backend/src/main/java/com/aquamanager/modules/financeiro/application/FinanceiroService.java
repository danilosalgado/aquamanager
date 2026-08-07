package com.aquamanager.modules.financeiro.application;

import com.aquamanager.modules.financeiro.application.dto.LancamentoRequest;
import com.aquamanager.modules.financeiro.application.dto.ResumoFinanceiroResponse;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FinanceiroService {

    Page<LancamentoFinanceiro> listar(UUID empresaId, String tipo, String status, Pageable pageable);

    LancamentoFinanceiro buscar(UUID empresaId, UUID id);

    LancamentoFinanceiro criar(UUID empresaId, LancamentoRequest request);

    LancamentoFinanceiro atualizar(UUID empresaId, UUID id, LancamentoRequest request);

    LancamentoFinanceiro marcarComoPago(UUID empresaId, UUID id);

    void remover(UUID empresaId, UUID id);

    ResumoFinanceiroResponse resumo(UUID empresaId, LocalDate inicio, LocalDate fim);
}
