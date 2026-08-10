package com.aquamanager.modules.venda.application;

import com.aquamanager.modules.cliente.domain.Cliente;
import com.aquamanager.modules.cliente.infrastructure.persistence.ClienteRepository;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.domain.StatusLancamento;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.venda.application.dto.LucroBrutoPorTanqueResponse;
import com.aquamanager.modules.venda.application.dto.VendaRequest;
import com.aquamanager.modules.venda.domain.Venda;
import com.aquamanager.modules.venda.infrastructure.persistence.VendaRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VendaServiceImpl implements VendaService {

    private final VendaRepository vendaRepository;
    private final LoteRepository loteRepository;
    private final ClienteRepository clienteRepository;
    private final TanqueRepository tanqueRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Venda> listar(UUID empresaId, UUID loteId, String categoriaProduto, Pageable pageable) {
        if (loteId != null) {
            return vendaRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        if (categoriaProduto != null) {
            return vendaRepository.findByEmpresaIdAndCategoriaProdutoIgnoreCase(empresaId, categoriaProduto, pageable);
        }
        return vendaRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Venda buscar(UUID empresaId, UUID id) {
        return buscarDaEmpresa(empresaId, id);
    }

    @Override
    @Transactional
    public Venda criar(UUID empresaId, VendaRequest request) {
        Lote lote = buscarLote(empresaId, request.loteId());
        Cliente cliente = request.clienteId() != null ? buscarCliente(empresaId, request.clienteId()) : null;

        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setEmpresaId(empresaId);
        preencherLancamento(lancamento, lote, cliente, request);
        lancamento = lancamentoFinanceiroRepository.save(lancamento);

        Venda venda = new Venda();
        venda.setEmpresaId(empresaId);
        venda.setLancamentoFinanceiroId(lancamento.getId());
        aplicarCampos(venda, lote, cliente, request);
        return vendaRepository.save(venda);
    }

    @Override
    @Transactional
    public Venda atualizar(UUID empresaId, UUID id, VendaRequest request) {
        Venda venda = buscarDaEmpresa(empresaId, id);
        Lote lote = buscarLote(empresaId, request.loteId());
        Cliente cliente = request.clienteId() != null ? buscarCliente(empresaId, request.clienteId()) : null;

        if (venda.getLancamentoFinanceiroId() != null) {
            lancamentoFinanceiroRepository.findById(venda.getLancamentoFinanceiroId())
                    .ifPresent(lancamento -> preencherLancamento(lancamento, lote, cliente, request));
        }

        aplicarCampos(venda, lote, cliente, request);
        return venda;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID id) {
        Venda venda = buscarDaEmpresa(empresaId, id);
        UUID lancamentoId = venda.getLancamentoFinanceiroId();
        // A FK é vendas.lancamento_financeiro_id -> lancamentos_financeiros.id: a venda
        // precisa ser apagada ANTES do lançamento, senão o banco rejeita por violação de
        // integridade referencial (a venda ainda apontaria pro lançamento já removido).
        vendaRepository.delete(venda);
        if (lancamentoId != null) {
            lancamentoFinanceiroRepository.findById(lancamentoId).ifPresent(lancamentoFinanceiroRepository::delete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarCategorias(UUID empresaId) {
        return vendaRepository.listarCategoriasDistintas(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LucroBrutoPorTanqueResponse> relatorioLucroBrutoPorTanque(UUID empresaId) {
        List<Tanque> tanques = tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO);

        Map<UUID, BigDecimal> receitaPorTanque = new HashMap<>();
        for (Venda venda : vendaRepository.findByEmpresaId(empresaId)) {
            UUID tanqueId = venda.getLote().getTanque().getId();
            receitaPorTanque.merge(tanqueId, venda.getValorTotal(), BigDecimal::add);
        }

        Map<UUID, BigDecimal> racaoPorTanque = new HashMap<>();
        Map<UUID, BigDecimal> operacionalPorTanque = new HashMap<>();
        for (LancamentoFinanceiro lancamento : lancamentoFinanceiroRepository
                .findByEmpresaIdAndTipoAndLoteIsNotNull(empresaId, TipoLancamento.DESPESA)) {
            UUID tanqueId = lancamento.getLote().getTanque().getId();
            Map<UUID, BigDecimal> destino = ehCategoriaRacao(lancamento.getCategoria()) ? racaoPorTanque : operacionalPorTanque;
            destino.merge(tanqueId, lancamento.getValor(), BigDecimal::add);
        }

        List<LucroBrutoPorTanqueResponse> resultado = new ArrayList<>();
        for (Tanque tanque : tanques) {
            BigDecimal receita = receitaPorTanque.getOrDefault(tanque.getId(), BigDecimal.ZERO);
            BigDecimal custoRacao = racaoPorTanque.getOrDefault(tanque.getId(), BigDecimal.ZERO);
            BigDecimal custoOperacional = operacionalPorTanque.getOrDefault(tanque.getId(), BigDecimal.ZERO);
            BigDecimal lucroBruto = receita.subtract(custoRacao).subtract(custoOperacional);
            resultado.add(new LucroBrutoPorTanqueResponse(
                    tanque.getId(), tanque.getNome(), receita, custoRacao, custoOperacional, lucroBruto));
        }
        return resultado;
    }

    private void preencherLancamento(LancamentoFinanceiro lancamento, Lote lote, Cliente cliente, VendaRequest request) {
        lancamento.setTipo(TipoLancamento.RECEITA);
        lancamento.setCategoria(request.categoriaProduto());
        lancamento.setDescricao("Venda de " + request.categoriaProduto() + " — " + lote.getTanque().getNome());
        lancamento.setValor(request.valorTotal());
        lancamento.setDataVencimento(request.dataVenda());
        lancamento.setDataPagamento(request.dataVenda());
        lancamento.setStatus(StatusLancamento.PAGO);
        lancamento.setCliente(cliente);
        lancamento.setLote(lote);
    }

    private void aplicarCampos(Venda venda, Lote lote, Cliente cliente, VendaRequest request) {
        venda.setLote(lote);
        venda.setCliente(cliente);
        venda.setCategoriaProduto(request.categoriaProduto());
        venda.setQuantidadeKg(request.quantidadeKg());
        venda.setValorTotal(request.valorTotal());
        venda.setDataVenda(request.dataVenda());
        venda.setObservacoes(request.observacoes());
    }

    /** Normaliza acentos ("ração"/"racao") pra casar a categoria com o balde de custo de ração do relatório. */
    private static boolean ehCategoriaRacao(String categoria) {
        if (categoria == null) return false;
        String normalizado = Normalizer.normalize(categoria, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return normalizado.contains("racao");
    }

    private Venda buscarDaEmpresa(UUID empresaId, UUID id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
        if (!venda.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Venda", id);
        }
        return venda;
    }

    private Lote buscarLote(UUID empresaId, UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));
        if (!lote.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lote", loteId);
        }
        return lote;
    }

    private Cliente buscarCliente(UUID empresaId, UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!cliente.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        return cliente;
    }
}
