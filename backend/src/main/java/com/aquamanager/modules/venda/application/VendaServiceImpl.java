package com.aquamanager.modules.venda.application;

import com.aquamanager.modules.cliente.domain.Cliente;
import com.aquamanager.modules.cliente.infrastructure.persistence.ClienteRepository;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.domain.StatusLancamento;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.venda.application.dto.ResumoLucroBrutoResponse;
import com.aquamanager.modules.venda.application.dto.VendaRequest;
import com.aquamanager.modules.venda.domain.Venda;
import com.aquamanager.modules.venda.infrastructure.persistence.VendaRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
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
    private final ClienteRepository clienteRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Venda> listar(UUID empresaId, String categoriaProduto, Pageable pageable) {
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
        Cliente cliente = request.clienteId() != null ? buscarCliente(empresaId, request.clienteId()) : null;

        LancamentoFinanceiro lancamento = new LancamentoFinanceiro();
        lancamento.setEmpresaId(empresaId);
        preencherLancamento(lancamento, cliente, request);
        lancamento = lancamentoFinanceiroRepository.save(lancamento);

        Venda venda = new Venda();
        venda.setEmpresaId(empresaId);
        venda.setLancamentoFinanceiroId(lancamento.getId());
        aplicarCampos(venda, cliente, request);
        return vendaRepository.save(venda);
    }

    @Override
    @Transactional
    public Venda atualizar(UUID empresaId, UUID id, VendaRequest request) {
        Venda venda = buscarDaEmpresa(empresaId, id);
        Cliente cliente = request.clienteId() != null ? buscarCliente(empresaId, request.clienteId()) : null;

        if (venda.getLancamentoFinanceiroId() != null) {
            lancamentoFinanceiroRepository.findById(venda.getLancamentoFinanceiroId())
                    .ifPresent(lancamento -> preencherLancamento(lancamento, cliente, request));
        }

        aplicarCampos(venda, cliente, request);
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
    public ResumoLucroBrutoResponse resumoLucroBruto(UUID empresaId) {
        BigDecimal receita = vendaRepository.findByEmpresaId(empresaId).stream()
                .map(Venda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal custoRacao = BigDecimal.ZERO;
        BigDecimal custoOperacional = BigDecimal.ZERO;
        for (LancamentoFinanceiro lancamento : lancamentoFinanceiroRepository
                .findByEmpresaIdAndTipoAndLoteIsNotNull(empresaId, TipoLancamento.DESPESA)) {
            if (ehCategoriaRacao(lancamento.getCategoria())) {
                custoRacao = custoRacao.add(lancamento.getValor());
            } else {
                custoOperacional = custoOperacional.add(lancamento.getValor());
            }
        }

        BigDecimal lucroBruto = receita.subtract(custoRacao).subtract(custoOperacional);
        return new ResumoLucroBrutoResponse(receita, custoRacao, custoOperacional, lucroBruto);
    }

    private void preencherLancamento(LancamentoFinanceiro lancamento, Cliente cliente, VendaRequest request) {
        lancamento.setTipo(TipoLancamento.RECEITA);
        lancamento.setCategoria(request.categoriaProduto());
        lancamento.setDescricao("Venda de " + request.categoriaProduto());
        lancamento.setValor(request.valorTotal());
        lancamento.setDataVencimento(request.dataVenda());
        lancamento.setDataPagamento(request.dataVenda());
        lancamento.setStatus(StatusLancamento.PAGO);
        lancamento.setCliente(cliente);
    }

    private void aplicarCampos(Venda venda, Cliente cliente, VendaRequest request) {
        venda.setCliente(cliente);
        venda.setCategoriaProduto(request.categoriaProduto());
        venda.setQuantidadeKg(request.quantidadeKg());
        venda.setValorTotal(request.valorTotal());
        venda.setDataVenda(request.dataVenda());
        venda.setObservacoes(request.observacoes());
    }

    /** Normaliza acentos ("ração"/"racao") pra casar a categoria com o balde de custo de ração do resumo. */
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

    private Cliente buscarCliente(UUID empresaId, UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        if (!cliente.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Cliente", clienteId);
        }
        return cliente;
    }
}
