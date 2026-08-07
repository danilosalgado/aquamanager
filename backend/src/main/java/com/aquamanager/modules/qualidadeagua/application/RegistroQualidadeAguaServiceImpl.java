package com.aquamanager.modules.qualidadeagua.application;

import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaRequest;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
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
public class RegistroQualidadeAguaServiceImpl implements RegistroQualidadeAguaService {

    private static final List<String> COLUNAS_IMPORTACAO = List.of(
            "Tanque (código)", "Temperatura (°C)", "pH", "Oxigênio Dissolvido (mg/L)", "Amônia (mg/L)",
            "Nitrito (mg/L)", "Alcalinidade (mg/L)", "Salinidade (ppt)", "Transparência (cm)", "Data/Hora da Medição");
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of("Tanque (código)", "Data/Hora da Medição");

    private final RegistroQualidadeAguaRepository registroQualidadeAguaRepository;
    private final TanqueRepository tanqueRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroQualidadeAgua> listar(UUID empresaId, UUID tanqueId, Pageable pageable) {
        if (tanqueId != null) {
            return registroQualidadeAguaRepository.findByEmpresaIdAndTanqueId(empresaId, tanqueId, pageable);
        }
        return registroQualidadeAguaRepository.findByEmpresaId(empresaId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroQualidadeAgua buscar(UUID empresaId, UUID registroId) {
        return buscarDaEmpresa(empresaId, registroId);
    }

    @Override
    @Transactional
    public RegistroQualidadeAgua criar(UUID empresaId, UUID usuarioId, RegistroQualidadeAguaRequest request) {
        Tanque tanque = buscarTanqueDaEmpresa(empresaId, request.tanqueId());

        RegistroQualidadeAgua registro = new RegistroQualidadeAgua();
        registro.setEmpresaId(empresaId);
        registro.setTanque(tanque);
        registro.setUsuarioId(usuarioId);
        aplicarCampos(registro, request);
        return registroQualidadeAguaRepository.save(registro);
    }

    @Override
    @Transactional
    public RegistroQualidadeAgua atualizar(UUID empresaId, UUID registroId, RegistroQualidadeAguaRequest request) {
        RegistroQualidadeAgua registro = buscarDaEmpresa(empresaId, registroId);
        if (!registro.getTanque().getId().equals(request.tanqueId())) {
            registro.setTanque(buscarTanqueDaEmpresa(empresaId, request.tanqueId()));
        }
        aplicarCampos(registro, request);
        return registro;
    }

    @Override
    @Transactional
    public void remover(UUID empresaId, UUID registroId) {
        RegistroQualidadeAgua registro = buscarDaEmpresa(empresaId, registroId);
        registroQualidadeAguaRepository.delete(registro);
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
                    Tanque tanque = tanqueRepository.findByEmpresaIdAndCodigoIgnoreCase(empresaId, codigoTanque)
                            .orElseThrow(() -> new IllegalArgumentException("Tanque \"" + codigoTanque + "\" não encontrado."));

                    LocalDateTime dataHora = reader.dataHora(row, "Data/Hora da Medição");
                    if (dataHora == null) {
                        throw new IllegalArgumentException("Data/Hora da Medição é obrigatória.");
                    }

                    RegistroQualidadeAguaRequest request = new RegistroQualidadeAguaRequest(
                            tanque.getId(),
                            reader.decimal(row, "Temperatura (°C)"),
                            reader.decimal(row, "pH"),
                            reader.decimal(row, "Oxigênio Dissolvido (mg/L)"),
                            reader.decimal(row, "Amônia (mg/L)"),
                            reader.decimal(row, "Nitrito (mg/L)"),
                            reader.decimal(row, "Alcalinidade (mg/L)"),
                            reader.decimal(row, "Salinidade (ppt)"),
                            reader.decimal(row, "Transparência (cm)"),
                            dataHora.atZone(ZoneId.systemDefault()).toInstant());
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
        List<RegistroQualidadeAgua> registros = registroQualidadeAguaRepository
                .findByEmpresaId(empresaId, Pageable.unpaged()).getContent();
        List<List<Object>> linhas = registros.stream().map(r -> List.<Object>of(
                r.getTanque().getCodigo(),
                valorOuVazio(r.getTemperatura()),
                valorOuVazio(r.getPh()),
                valorOuVazio(r.getOxigenioDissolvido()),
                valorOuVazio(r.getAmonia()),
                valorOuVazio(r.getNitrito()),
                valorOuVazio(r.getAlcalinidade()),
                valorOuVazio(r.getSalinidade()),
                valorOuVazio(r.getTransparenciaCm()),
                LocalDateTime.ofInstant(r.getMedidoEm(), ZoneId.systemDefault())
        )).toList();
        return ExcelExportUtil.gerar("Qualidade da Água", COLUNAS_IMPORTACAO, linhas);
    }

    @Override
    public byte[] gerarModeloImportacao() {
        List<List<Object>> exemplo = List.of(List.<Object>of(
                "TQ-01", BigDecimal.valueOf(26.5), BigDecimal.valueOf(7.2), BigDecimal.valueOf(5.8),
                BigDecimal.valueOf(0.02), BigDecimal.valueOf(0.01), BigDecimal.valueOf(80), BigDecimal.ZERO,
                BigDecimal.valueOf(40), LocalDateTime.now().withHour(7).withMinute(0).withSecond(0).withNano(0)));
        return ExcelExportUtil.gerar("Modelo Qualidade da Água", COLUNAS_IMPORTACAO, exemplo);
    }

    private static Object valorOuVazio(BigDecimal valor) {
        return valor != null ? valor : "";
    }

    private static String mensagemErro(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Erro ao processar a linha.";
    }

    private RegistroQualidadeAgua buscarDaEmpresa(UUID empresaId, UUID registroId) {
        RegistroQualidadeAgua registro = registroQualidadeAguaRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de qualidade de água", registroId));
        if (!registro.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Registro de qualidade de água", registroId);
        }
        return registro;
    }

    private Tanque buscarTanqueDaEmpresa(UUID empresaId, UUID tanqueId) {
        Tanque tanque = tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Tanque", tanqueId));
        if (!tanque.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Tanque", tanqueId);
        }
        return tanque;
    }

    private void aplicarCampos(RegistroQualidadeAgua registro, RegistroQualidadeAguaRequest request) {
        registro.setTemperatura(request.temperatura());
        registro.setPh(request.ph());
        registro.setOxigenioDissolvido(request.oxigenioDissolvido());
        registro.setAmonia(request.amonia());
        registro.setNitrito(request.nitrito());
        registro.setAlcalinidade(request.alcalinidade());
        registro.setSalinidade(request.salinidade());
        registro.setTransparenciaCm(request.transparenciaCm());
        registro.setMedidoEm(request.medidoEm());
    }
}
