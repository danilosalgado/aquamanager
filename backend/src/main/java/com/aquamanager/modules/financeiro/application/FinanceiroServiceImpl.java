package com.aquamanager.modules.financeiro.application;

import com.aquamanager.modules.cliente.domain.Cliente;
import com.aquamanager.modules.cliente.infrastructure.persistence.ClienteRepository;
import com.aquamanager.modules.financeiro.application.dto.LancamentoRequest;
import com.aquamanager.modules.financeiro.application.dto.ResumoFinanceiroResponse;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.domain.StatusLancamento;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import com.aquamanager.modules.fornecedor.infrastructure.persistence.FornecedorRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinanceiroServiceImpl implements FinanceiroService {

    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LancamentoFinanceiro> listar(UUID empresaId, String tipo, String status, Pageable pageable) {
        if (tipo != null) {
            return lancamentoRepository.findByEmpresaIdAndTipo(empresaId, parseTipo(tipo), pageable);
        }
        if (status != null) {
            return lancamentoRepository.findByEmpresaIdAndStatus(empresaId, parseStatus(status), pageable);
        }
        return lancamentoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LancamentoFinanceiro buscar(UUID empresaId, UUID id) {
        return buscarDaEmpresa(empresaId, id);
    }

    @Override
    @Transactional
    public LancamentoFinanceiro criar(UUID empresaId, LancamentoRequest request) {
        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setEmpresaId(empresaId);
        aplicarCampos(lancamento, empresaId, request);
        return lancamentoRepository.save(lancamento);
    }

    @Override
    @Transactional
    public LancamentoFinanceiro atualizar(UUID empresaId, UUID id, LancamentoRequest request) {
        LancamentoFinanceiro lancamento = buscarDaEmpresa(empresaId, id);
        aplicarCampos(lancamento, empresaId, request);
        return lancamento;
    }

    @Override
    @Transactional
    public LancamentoFinanceiro marcarComoPago(UUID empresaId, UUID id) {
        LancamentoFinanceiro lancamento = buscarDaEmpresa(empresaId, id);
        lancamento.setStatus(StatusLancamento.PAGO);
        lancamento.setDataPagamento(LocalDate.now());
        return lancamento;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID id) {
        lancamentoRepository.delete(buscarDaEmpresa(empresaId, id));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoFinanceiroResponse resumo(UUID empresaId, LocalDate inicio, LocalDate fim) {
        BigDecimal receita = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.RECEITA, inicio, fim);
        BigDecimal despesa = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.DESPESA, inicio, fim);
        BigDecimal lucro = receita.subtract(despesa);
        BigDecimal margem = receita.compareTo(BigDecimal.ZERO) > 0
                ? lucro.divide(receita, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal receitaPendente = lancamentoRepository.somarPendentePorTipo(empresaId, TipoLancamento.RECEITA);
        BigDecimal despesaPendente = lancamentoRepository.somarPendentePorTipo(empresaId, TipoLancamento.DESPESA);

        return new ResumoFinanceiroResponse(receita, despesa, lucro, margem.setScale(2, RoundingMode.HALF_UP),
                receitaPendente, despesaPendente);
    }

    private void aplicarCampos(LancamentoFinanceiro lancamento, UUID empresaId, LancamentoRequest request) {
        lancamento.setTipo(parseTipo(request.tipo()));
        lancamento.setCategoria(request.categoria());
        lancamento.setDescricao(request.descricao());
        lancamento.setValor(request.valor());
        lancamento.setDataVencimento(request.dataVencimento());
        lancamento.setFormaPagamento(request.formaPagamento());

        lancamento.setCliente(request.clienteId() != null ? buscarCliente(empresaId, request.clienteId()) : null);
        lancamento.setFornecedor(request.fornecedorId() != null ? buscarFornecedor(empresaId, request.fornecedorId()) : null);
        lancamento.setLote(request.loteId() != null ? buscarLote(empresaId, request.loteId()) : null);
    }

    private LancamentoFinanceiro buscarDaEmpresa(UUID empresaId, UUID id) {
        LancamentoFinanceiro lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento financeiro", id));
        if (!lancamento.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lançamento financeiro", id);
        }
        return lancamento;
    }

    private Cliente buscarCliente(UUID empresaId, UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!cliente.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        return cliente;
    }

    private Fornecedor buscarFornecedor(UUID empresaId, UUID fornecedorId) {
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", fornecedorId));
        if (!fornecedor.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fornecedor", fornecedorId);
        }
        return fornecedor;
    }

    private Lote buscarLote(UUID empresaId, UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));
        if (!lote.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lote", loteId);
        }
        return lote;
    }

    private TipoLancamento parseTipo(String valor) {
        try {
            return TipoLancamento.valueOf(valor);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ENUM_VALUE", "Tipo de lançamento inválido: " + valor);
        }
    }

    private StatusLancamento parseStatus(String valor) {
        try {
            return StatusLancamento.valueOf(valor);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ENUM_VALUE", "Status de lançamento inválido: " + valor);
        }
    }
}
