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
import com.aquamanager.shared.infrastructure.excel.ExcelExportUtil;
import com.aquamanager.shared.infrastructure.excel.ExcelSheetReader;
import com.aquamanager.shared.infrastructure.excel.ImportErro;
import com.aquamanager.shared.infrastructure.excel.ImportResultado;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FinanceiroServiceImpl implements FinanceiroService {

    private static final List<String> COLUNAS_IMPORTACAO = List.of(
            "Tipo", "Categoria", "Descrição", "Valor", "Data de vencimento", "Data de pagamento", "Forma de pagamento");
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "Tipo", "Categoria", "Descrição", "Valor", "Data de vencimento");

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

    @Override
    @Transactional
    public ImportResultado importar(UUID empresaId, MultipartFile arquivo) {
        List<ImportErro> erros = new ArrayList<>();
        int importados = 0;
        int totalLinhas = 0;
        try (ExcelSheetReader reader = new ExcelSheetReader(arquivo.getInputStream(), COLUNAS_OBRIGATORIAS)) {
            for (int idx : reader.indicesLinhasDados()) {
                totalLinhas++;
                Row row = reader.linha(idx);
                try {
                    String tipoTexto = reader.texto(row, "Tipo");
                    if (tipoTexto == null) {
                        throw new IllegalArgumentException("Tipo é obrigatório.");
                    }
                    String tipo = parseTipoAmigavel(tipoTexto).name();

                    String categoria = reader.texto(row, "Categoria");
                    if (categoria == null) {
                        throw new IllegalArgumentException("Categoria é obrigatória.");
                    }
                    String descricao = reader.texto(row, "Descrição");
                    if (descricao == null) {
                        throw new IllegalArgumentException("Descrição é obrigatória.");
                    }
                    BigDecimal valor = reader.decimal(row, "Valor");
                    if (valor == null) {
                        throw new IllegalArgumentException("Valor é obrigatório.");
                    }
                    LocalDate dataVencimento = reader.data(row, "Data de vencimento");
                    if (dataVencimento == null) {
                        throw new IllegalArgumentException("Data de vencimento é obrigatória.");
                    }
                    LocalDate dataPagamento = reader.data(row, "Data de pagamento");
                    String formaPagamento = reader.texto(row, "Forma de pagamento");

                    LancamentoRequest request = new LancamentoRequest(
                            tipo, categoria, descricao, valor, dataVencimento, formaPagamento, null, null, null);
                    LancamentoFinanceiro criado = criar(empresaId, request);
                    if (dataPagamento != null) {
                        criado.setStatus(StatusLancamento.PAGO);
                        criado.setDataPagamento(dataPagamento);
                    }
                    importados++;
                } catch (Exception e) {
                    erros.add(new ImportErro(ExcelSheetReader.numeroLinhaExcel(idx), mensagemErro(e)));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("EXCEL_INVALIDO", "Não foi possível ler o arquivo. Verifique se é uma planilha .xlsx válida.");
        } catch (IllegalArgumentException e) {
            throw new BusinessException("EXCEL_INVALIDO", e.getMessage());
        }
        return new ImportResultado(totalLinhas, importados, erros);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportar(UUID empresaId) {
        List<LancamentoFinanceiro> lancamentos = lancamentoRepository.findByEmpresaId(empresaId, Pageable.unpaged()).getContent();
        List<List<Object>> linhas = lancamentos.stream().map(l -> List.<Object>of(
                rotuloTipo(l.getTipo()),
                l.getCategoria(),
                l.getDescricao(),
                l.getValor(),
                l.getDataVencimento(),
                l.getDataPagamento() != null ? l.getDataPagamento() : "",
                l.getFormaPagamento() != null ? l.getFormaPagamento() : ""
        )).toList();
        return ExcelExportUtil.gerar("Financeiro", COLUNAS_IMPORTACAO, linhas);
    }

    @Override
    public byte[] gerarModeloImportacao() {
        List<List<Object>> exemplo = List.of(List.<Object>of(
                "Despesa", "Ração", "Compra de ração 25kg", 356.46, LocalDate.now(), "", "Pix"));
        return ExcelExportUtil.gerar("Modelo Financeiro", COLUNAS_IMPORTACAO, exemplo);
    }

    private static String mensagemErro(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Erro ao processar a linha.";
    }

    private static TipoLancamento parseTipoAmigavel(String texto) {
        String normalizado = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return switch (normalizado) {
            case "RECEITA" -> TipoLancamento.RECEITA;
            case "DESPESA" -> TipoLancamento.DESPESA;
            default -> throw new IllegalArgumentException(
                    "Tipo \"" + texto + "\" inválido. Use: Receita ou Despesa.");
        };
    }

    private static String rotuloTipo(TipoLancamento tipo) {
        return tipo == TipoLancamento.RECEITA ? "Receita" : "Despesa";
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
