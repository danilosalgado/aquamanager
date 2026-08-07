package com.aquamanager.modules.estoque.application;

import com.aquamanager.modules.estoque.application.dto.EstoqueItemRequest;
import com.aquamanager.modules.estoque.application.dto.MovimentacaoRequest;
import com.aquamanager.modules.estoque.domain.CategoriaEstoque;
import com.aquamanager.modules.estoque.domain.EstoqueItem;
import com.aquamanager.modules.estoque.domain.EstoqueMovimentacao;
import com.aquamanager.modules.estoque.domain.TipoMovimentacao;
import com.aquamanager.modules.estoque.infrastructure.persistence.EstoqueItemRepository;
import com.aquamanager.modules.estoque.infrastructure.persistence.EstoqueMovimentacaoRepository;
import com.aquamanager.modules.fornecedor.domain.Fornecedor;
import com.aquamanager.modules.fornecedor.infrastructure.persistence.FornecedorRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueServiceImpl implements EstoqueService {

    private final EstoqueItemRepository estoqueItemRepository;
    private final EstoqueMovimentacaoRepository estoqueMovimentacaoRepository;
    private final FornecedorRepository fornecedorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EstoqueItem> listarItens(UUID empresaId, Pageable pageable) {
        return estoqueItemRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public EstoqueItem buscarItem(UUID empresaId, UUID itemId) {
        return buscarItemDaEmpresa(empresaId, itemId);
    }

    @Override
    @Transactional
    public EstoqueItem criarItem(UUID empresaId, EstoqueItemRequest request) {
        EstoqueItem item = new EstoqueItem();
        item.setEmpresaId(empresaId);
        item.setQuantidadeAtual(BigDecimal.ZERO);
        aplicarCampos(empresaId, item, request);
        return estoqueItemRepository.save(item);
    }

    @Override
    @Transactional
    public EstoqueItem atualizarItem(UUID empresaId, UUID itemId, EstoqueItemRequest request) {
        EstoqueItem item = buscarItemDaEmpresa(empresaId, itemId);
        aplicarCampos(empresaId, item, request);
        return item;
    }

    @Override
    @Transactional
    public void removerItem(UUID empresaId, UUID itemId) {
        EstoqueItem item = buscarItemDaEmpresa(empresaId, itemId);
        estoqueItemRepository.delete(item);
    }

    @Override
    @Transactional
    public EstoqueMovimentacao registrarMovimentacao(UUID empresaId, MovimentacaoRequest request) {
        EstoqueItem item = buscarItemDaEmpresa(empresaId, request.itemId());
        TipoMovimentacao tipo = parseEnum(TipoMovimentacao.class, request.tipo(), "tipo de movimentação");

        BigDecimal novaQuantidade = tipo == TipoMovimentacao.ENTRADA
                ? item.getQuantidadeAtual().add(request.quantidade())
                : item.getQuantidadeAtual().subtract(request.quantidade());

        if (novaQuantidade.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("ESTOQUE_INSUFICIENTE", "Quantidade em estoque insuficiente para esta saída.");
        }

        item.setQuantidadeAtual(novaQuantidade);
        estoqueItemRepository.save(item);

        EstoqueMovimentacao movimentacao = new EstoqueMovimentacao();
        movimentacao.setEmpresaId(empresaId);
        movimentacao.setItem(item);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setMotivo(request.motivo());
        movimentacao.setUsuarioId(SecurityUtils.currentUserId());
        return estoqueMovimentacaoRepository.save(movimentacao);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstoqueMovimentacao> listarMovimentacoes(UUID empresaId, UUID itemId, Pageable pageable) {
        if (itemId != null) {
            return estoqueMovimentacaoRepository.findByEmpresaIdAndItemId(empresaId, itemId, pageable);
        }
        return estoqueMovimentacaoRepository.findByEmpresaId(empresaId, pageable);
    }

    private EstoqueItem buscarItemDaEmpresa(UUID empresaId, UUID itemId) {
        EstoqueItem item = estoqueItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque", itemId));
        if (!item.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Item de estoque", itemId);
        }
        return item;
    }

    private void aplicarCampos(UUID empresaId, EstoqueItem item, EstoqueItemRequest request) {
        item.setCategoria(parseEnum(CategoriaEstoque.class, request.categoria(), "categoria de estoque"));
        item.setNome(request.nome());
        item.setUnidade(request.unidade());
        item.setQuantidadeMinima(request.quantidadeMinima() != null ? request.quantidadeMinima() : BigDecimal.ZERO);
        item.setFornecedor(buscarFornecedor(empresaId, request.fornecedorId()));
        item.setValidade(request.validade());
        item.setPrecoUnitario(request.precoUnitario());
    }

    private Fornecedor buscarFornecedor(UUID empresaId, UUID fornecedorId) {
        if (fornecedorId == null) {
            return null;
        }
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Fornecedor", fornecedorId));
        if (!fornecedor.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Fornecedor", fornecedorId);
        }
        return fornecedor;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String descricao) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ENUM_VALUE", "Valor inválido para " + descricao + ": " + value);
        }
    }
}
