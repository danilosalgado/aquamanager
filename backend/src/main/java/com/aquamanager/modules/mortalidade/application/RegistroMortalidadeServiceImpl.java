package com.aquamanager.modules.mortalidade.application;

import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeRequest;
import com.aquamanager.modules.mortalidade.domain.CausaExclusao;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.excel.ExcelExportUtil;
import com.aquamanager.shared.infrastructure.excel.ExcelSheetReader;
import com.aquamanager.shared.infrastructure.excel.ImportErro;
import com.aquamanager.shared.infrastructure.excel.ImportResultado;
import java.io.IOException;
import java.time.LocalDate;
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
public class RegistroMortalidadeServiceImpl implements RegistroMortalidadeService {

    private static final List<String> COLUNAS_IMPORTACAO = List.of(
            "Tanque (código)", "Quantidade", "Data", "Causa", "Observações");
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "Tanque (código)", "Quantidade", "Data", "Causa");

    private final RegistroMortalidadeRepository registroMortalidadeRepository;
    private final LoteRepository loteRepository;
    private final TanqueRepository tanqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroMortalidade> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroMortalidadeRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroMortalidadeRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroMortalidade buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroMortalidade criar(UUID empresaId, RegistroMortalidadeRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroMortalidade registro = new RegistroMortalidade();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        aplicarCampos(registro, request);

        decrementarEstoque(lote, request.quantidade());
        loteRepository.save(lote);

        return registroMortalidadeRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroMortalidade atualizar(UUID empresaId, UUID registroId, RegistroMortalidadeRequest request) {
        RegistroMortalidade registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote().getId().equals(request.loteId())
                ? registro.getLote()
                : buscarLoteDaEmpresa(empresaId, request.loteId());

        // Estorna a quantidade anteriormente descontada do lote original antes de aplicar o novo valor.
        Lote loteOriginal = registro.getLote();
        loteOriginal.setQuantidadeAtual(loteOriginal.getQuantidadeAtual() + registro.getQuantidade());
        if (!loteOriginal.getId().equals(lote.getId())) {
            loteRepository.save(loteOriginal);
        }

        decrementarEstoque(lote, request.quantidade());
        loteRepository.save(lote);

        registro.setLote(lote);
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroMortalidade registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote();
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() + registro.getQuantidade());
        loteRepository.save(lote);
        registroMortalidadeRepository.delete(registro);
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
                    String codigoTanque = reader.texto(row, "Tanque (código)");
                    if (codigoTanque == null) {
                        throw new IllegalArgumentException("Tanque (código) é obrigatório.");
                    }
                    Lote lote = resolverLoteAtivoPorTanque(empresaId, codigoTanque);

                    Integer quantidade = reader.inteiro(row, "Quantidade");
                    if (quantidade == null) {
                        throw new IllegalArgumentException("Quantidade é obrigatória.");
                    }
                    LocalDate data = reader.data(row, "Data");
                    if (data == null) {
                        throw new IllegalArgumentException("Data é obrigatória.");
                    }
                    String textoCausa = reader.texto(row, "Causa");
                    if (textoCausa == null) {
                        throw new IllegalArgumentException("Causa é obrigatória.");
                    }
                    CausaExclusao causa = parseCausa(textoCausa);

                    RegistroMortalidadeRequest request = new RegistroMortalidadeRequest(
                            lote.getId(), quantidade, data, causa, reader.texto(row, "Observações"));
                    criar(empresaId, request);
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
        List<RegistroMortalidade> registros = registroMortalidadeRepository
                .findByEmpresaId(empresaId, Pageable.unpaged()).getContent();
        List<List<Object>> linhas = registros.stream().map(r -> List.<Object>of(
                r.getLote().getTanque().getCodigo(),
                r.getQuantidade(),
                r.getData(),
                rotuloCausa(r.getCausa()),
                r.getObservacoes() != null ? r.getObservacoes() : ""
        )).toList();
        return ExcelExportUtil.gerar("Exclusao", COLUNAS_IMPORTACAO, linhas);
    }

    @Override
    public byte[] gerarModeloImportacao() {
        List<List<Object>> exemplo = List.of(List.<Object>of(
                "TQ-01", 5, LocalDate.now(), "Morte", "Observado durante alimentação"));
        return ExcelExportUtil.gerar("Modelo Exclusao", COLUNAS_IMPORTACAO, exemplo);
    }

    private static CausaExclusao parseCausa(String texto) {
        String normalizado = java.text.Normalizer.normalize(texto.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
        return switch (normalizado) {
            case "RETIRADA PARA ABATE", "RETIRADA_ABATE" -> CausaExclusao.RETIRADA_ABATE;
            case "TRANSFERENCIA" -> CausaExclusao.TRANSFERENCIA;
            case "MORTE" -> CausaExclusao.MORTE;
            default -> throw new IllegalArgumentException(
                    "Causa \"" + texto + "\" inválida. Use: Retirada para abate, Transferência ou Morte.");
        };
    }

    private static String rotuloCausa(CausaExclusao causa) {
        return switch (causa) {
            case RETIRADA_ABATE -> "Retirada para abate";
            case TRANSFERENCIA -> "Transferência";
            case MORTE -> "Morte";
        };
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

    private void decrementarEstoque(Lote lote, Integer quantidade) {
        if (quantidade > lote.getQuantidadeAtual()) {
            throw new BusinessException("MORTALIDADE_INVALIDA",
                    "Quantidade de mortalidade maior que o estoque atual do lote.");
        }
        lote.setQuantidadeAtual(lote.getQuantidadeAtual() - quantidade);
    }

    private RegistroMortalidade buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroMortalidade registro = registroMortalidadeRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de mortalidade", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de mortalidade", registroId);
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

    private void aplicarCampos(RegistroMortalidade registro, RegistroMortalidadeRequest request) {
        registro.setQuantidade(request.quantidade());
        registro.setData(request.data());
        registro.setCausa(request.causa());
        registro.setObservacoes(request.observacoes());
    }
}
