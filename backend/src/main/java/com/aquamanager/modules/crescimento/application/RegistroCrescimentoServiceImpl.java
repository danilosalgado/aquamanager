package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.RegistroCrescimentoRequest;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
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
import java.math.RoundingMode;
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
public class RegistroCrescimentoServiceImpl implements RegistroCrescimentoService {

    private static final int ESCALA_BIOMASSA = 3;
    private static final List<String> COLUNAS_IMPORTACAO = List.of(
            "Tanque (código)", "Peso Médio (g)", "Quantidade da Amostra", "Data da Pesagem");
    private static final List<String> COLUNAS_OBRIGATORIAS = COLUNAS_IMPORTACAO;

    private final RegistroCrescimentoRepository registroCrescimentoRepository;
    private final LoteRepository loteRepository;
    private final TanqueRepository tanqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroCrescimento> listar(UUID empresaId, UUID loteId, Pageable pageable) {
        if (loteId != null) {
            return registroCrescimentoRepository.findByEmpresaIdAndLoteId(empresaId, loteId, pageable);
        }
        return registroCrescimentoRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroCrescimento buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroCrescimento criar(UUID empresaId, UUID usuarioId, RegistroCrescimentoRequest request) {
        Lote lote = buscarLoteDaEmpresa(empresaId, request.loteId());

        RegistroCrescimento registro = new RegistroCrescimento();
        registro.setEmpresaId(empresaId);
        registro.setLote(lote);
        registro.setUsuarioId(usuarioId);
        registro.setPesoMedioG(request.pesoMedioG());
        registro.setQuantidadeAmostra(request.quantidadeAmostra());
        registro.setDataPesagem(request.dataPesagem());
        registro.setBiomassaKg(calcularBiomassaKg(lote, request.pesoMedioG()));

        RegistroCrescimento salvo = registroCrescimentoRepository.save(registro);

        lote.setPesoAtualG(request.pesoMedioG());
        loteRepository.save(lote);

        return salvo;
    }

    @Override
    @Transactional
    public RegistroCrescimento atualizar(UUID empresaId, UUID registroId, RegistroCrescimentoRequest request) {
        RegistroCrescimento registro = buscarDaEmpresa(empresaId, registroId);
        Lote lote = registro.getLote().getId().equals(request.loteId())
                ? registro.getLote()
                : buscarLoteDaEmpresa(empresaId, request.loteId());

        registro.setLote(lote);
        registro.setPesoMedioG(request.pesoMedioG());
        registro.setQuantidadeAmostra(request.quantidadeAmostra());
        registro.setDataPesagem(request.dataPesagem());
        registro.setBiomassaKg(calcularBiomassaKg(lote, request.pesoMedioG()));

        lote.setPesoAtualG(request.pesoMedioG());
        loteRepository.save(lote);

        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroCrescimento registro = buscarDaEmpresa(empresaId, registroId);
        registroCrescimentoRepository.delete(registro);
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

                    BigDecimal pesoMedioG = reader.decimal(row, "Peso Médio (g)");
                    if (pesoMedioG == null) {
                        throw new IllegalArgumentException("Peso Médio (g) é obrigatório.");
                    }
                    Integer quantidadeAmostra = reader.inteiro(row, "Quantidade da Amostra");
                    if (quantidadeAmostra == null) {
                        throw new IllegalArgumentException("Quantidade da Amostra é obrigatória.");
                    }
                    LocalDate dataPesagem = reader.data(row, "Data da Pesagem");
                    if (dataPesagem == null) {
                        throw new IllegalArgumentException("Data da Pesagem é obrigatória.");
                    }

                    RegistroCrescimentoRequest request = new RegistroCrescimentoRequest(
                            lote.getId(), pesoMedioG, quantidadeAmostra, dataPesagem);
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
        List<RegistroCrescimento> registros = registroCrescimentoRepository
                .findByEmpresaId(empresaId, Pageable.unpaged()).getContent();
        List<List<Object>> linhas = registros.stream().map(r -> List.<Object>of(
                r.getLote().getTanque().getCodigo(),
                r.getPesoMedioG(),
                r.getQuantidadeAmostra(),
                r.getDataPesagem()
        )).toList();
        return ExcelExportUtil.gerar("Crescimento", COLUNAS_IMPORTACAO, linhas);
    }

    @Override
    public byte[] gerarModeloImportacao() {
        List<List<Object>> exemplo = List.of(List.<Object>of(
                "TQ-01", BigDecimal.valueOf(250.5), 30, LocalDate.now()));
        return ExcelExportUtil.gerar("Modelo Crescimento", COLUNAS_IMPORTACAO, exemplo);
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

    private BigDecimal calcularBiomassaKg(Lote lote, BigDecimal pesoMedioG) {
        BigDecimal quantidadeAtual = BigDecimal.valueOf(lote.getQuantidadeAtual());
        return quantidadeAtual.multiply(pesoMedioG)
                .divide(BigDecimal.valueOf(1000), ESCALA_BIOMASSA, RoundingMode.HALF_UP);
    }

    private RegistroCrescimento buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroCrescimento registro = registroCrescimentoRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de crescimento", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de crescimento", registroId);
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
}
