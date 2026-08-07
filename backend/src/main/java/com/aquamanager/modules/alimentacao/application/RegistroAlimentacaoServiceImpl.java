package com.aquamanager.modules.alimentacao.application;

import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoRequest;
import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.excel.ExcelExportUtil;
import com.aquamanager.shared.infrastructure.excel.ExcelSheetReader;
import com.aquamanager.shared.infrastructure.excel.ImportErro;
import com.aquamanager.shared.infrastructure.excel.ImportResultado;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
public class RegistroAlimentacaoServiceImpl implements RegistroAlimentacaoService {

    private static final List<String> COLUNAS_IMPORTACAO = List.of(
            "Tanque (código)", "Tipo de Ração", "Fornecedor", "Quantidade (kg)", "Data/Hora", "Custo (R$)");
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "Tanque (código)", "Tipo de Ração", "Quantidade (kg)", "Data/Hora");

    private final RegistroAlimentacaoRepository registroAlimentacaoRepository;
    private final LoteRepository loteRepository;
    private final TanqueRepository tanqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAlimentacao> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroAlimentacaoRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroAlimentacaoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroAlimentacao buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroAlimentacao criar(UUID empresaId, UUID usuarioId, RegistroAlimentacaoRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroAlimentacao registro = new RegistroAlimentacao();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        registro.setUsuarioId(usuarioId);
        aplicarCampos(registro, request);
        return registroAlimentacaoRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroAlimentacao atualizar(UUID empresaId, UUID registroId, RegistroAlimentacaoRequest request) {
        RegistroAlimentacao registro = buscarDaEmpresa(empresaId, registroId);
        if (!registro.getLote().getId().equals(request.loteId())) {
            registro.setLote(buscarLoteDaEmpresa(empresaId, request.loteId()));
        }
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroAlimentacao registro = buscarDaEmpresa(empresaId, registroId);
        registroAlimentacaoRepository.delete(registro);
    }

    @Override
    @Transactional
    public ImportResultado importar(UUID empresaId, UUID usuarioId, MultipartFile arquivo) {
        List<ImportErro> erros = new ArrayList<>();
        int importados = 0;
        int totalLinhas = 0;
        try (ExcelSheetReader reader = new ExcelSheetReader(arquivo.getInputStream(), COLUNAS_OBRIGATORIAS)) {
            for (int idx : reader.indicesLinhasDados()) {
                totalLinhas++;
                Row row = reader.linha(idx);
                try {
                    String codigoTanque = reader.texto(row, "Tanque (código)");
                    if (codigoTanque == null) {
                        throw new IllegalArgumentException("Tanque (código) é obrigatório.");
                    }
                    Lote lote = resolverLoteAtivoPorTanque(empresaId, codigoTanque);

                    String tipoRacao = reader.texto(row, "Tipo de Ração");
                    if (tipoRacao == null) {
                        throw new IllegalArgumentException("Tipo de Ração é obrigatório.");
                    }
                    BigDecimal quantidade = reader.decimal(row, "Quantidade (kg)");
                    if (quantidade == null) {
                        throw new IllegalArgumentException("Quantidade (kg) é obrigatória.");
                    }
                    LocalDateTime dataHora = reader.dataHora(row, "Data/Hora");
                    if (dataHora == null) {
                        throw new IllegalArgumentException("Data/Hora é obrigatória.");
                    }

                    RegistroAlimentacaoRequest request = new RegistroAlimentacaoRequest(
                            lote.getId(),
                            tipoRacao,
                            reader.texto(row, "Fornecedor"),
                            quantidade,
                            dataHora.atZone(ZoneId.systemDefault()).toInstant(),
                            reader.decimal(row, "Custo (R$)"));
                    criar(empresaId, usuarioId, request);
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
        List<RegistroAlimentacao> registros = registroAlimentacaoRepository
                .findByEmpresaId(empresaId, Pageable.unpaged()).getContent();
        List<List<Object>> linhas = registros.stream().map(r -> List.<Object>of(
                r.getLote().getTanque().getCodigo(),
                r.getTipoRacao(),
                r.getFornecedor() != null ? r.getFornecedor() : "",
                r.getQuantidadeKg(),
                LocalDateTime.ofInstant(r.getHorario(), ZoneId.systemDefault()),
                r.getCusto() != null ? r.getCusto() : ""
        )).toList();
        return ExcelExportUtil.gerar("Alimentação", COLUNAS_IMPORTACAO, linhas);
    }

    @Override
    public byte[] gerarModeloImportacao() {
        List<List<Object>> exemplo = List.of(List.<Object>of(
                "TQ-01", "Extrusada 32% PB", "Fornecedor Exemplo Ltda",
                BigDecimal.valueOf(12.5), LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0),
                BigDecimal.valueOf(150.00)));
        return ExcelExportUtil.gerar("Modelo Alimentação", COLUNAS_IMPORTACAO, exemplo);
    }

    private Lote resolverLoteAtivoPorTanque(UUID empresaId, String codigoTanque) {
        Tanque tanque = tanqueRepository.findByEmpresaIdAndCodigoIgnoreCase(empresaId, codigoTanque)
                .orElseThrow(() -> new IllegalArgumentException("Tanque \"" + codigoTanque + "\" não encontrado."));
        List<Lote> ativos = loteRepository.findByTanqueIdAndStatus(tanque.getId(), StatusLote.ATIVO);
        if (ativos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum lote ativo encontrado no tanque \"" + codigoTanque + "\".");
        }
        if (ativos.size() > 1) {
            throw new IllegalArgumentException(
                    "Mais de um lote ativo no tanque \"" + codigoTanque + "\" — cadastre este registro manualmente informando o lote.");
        }
        return ativos.get(0);
    }

    private static String mensagemErro(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Erro ao processar a linha.";
    }

    private RegistroAlimentacao buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroAlimentacao registro = registroAlimentacaoRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de alimentação", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de alimentação", registroId);
        }
        return registro;
    }

    private Lote buscarLoteDaEmpresa(UUID empresaId, UUID loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote", loteId));
        if (!lote.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Lote", loteId);
        }
        return lote;
    }

    private void aplicarCampos(RegistroAlimentacao registro, RegistroAlimentacaoRequest request) {
        registro.setTipoRacao(request.tipoRacao());
        registro.setFornecedor(request.fornecedor());
        registro.setQuantidadeKg(request.quantidadeKg());
        registro.setHorario(request.horario());
        registro.setCusto(request.custo());
    }
}
