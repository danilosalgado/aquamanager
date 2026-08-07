package com.aquamanager.modules.qualidadeagua.infrastructure.web;

import com.aquamanager.modules.qualidadeagua.application.RegistroQualidadeAguaService;
import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaRequest;
import com.aquamanager.modules.qualidadeagua.application.dto.RegistroQualidadeAguaResponse;
import com.aquamanager.modules.qualidadeagua.infrastructure.mapper.RegistroQualidadeAguaMapper;
import com.aquamanager.shared.infrastructure.excel.ImportResultado;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/qualidade-agua")
@RequiredArgsConstructor
public class RegistroQualidadeAguaController {

    private final RegistroQualidadeAguaService registroQualidadeAguaService;
    private final RegistroQualidadeAguaMapper registroQualidadeAguaMapper;

    @GetMapping
    public ApiResponse<PageResponse<RegistroQualidadeAguaResponse>> listar(
            @RequestParam(required = false) UUID tanqueId, Pageable pageable) {
        var page = registroQualidadeAguaService.listar(SecurityUtils.currentEmpresaId(), tanqueId, pageable)
                .map(registroQualidadeAguaMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistroQualidadeAguaResponse> buscar(@PathVariable UUID id) {
        var registro = registroQualidadeAguaService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(registroQualidadeAguaMapper.toResponse(registro));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroQualidadeAguaResponse> criar(@Valid @RequestBody RegistroQualidadeAguaRequest request) {
        var registro = registroQualidadeAguaService.criar(
                SecurityUtils.currentEmpresaId(), SecurityUtils.currentUserId(), request);
        return ApiResponse.of(registroQualidadeAguaMapper.toResponse(registro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroQualidadeAguaResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody RegistroQualidadeAguaRequest request) {
        var registro = registroQualidadeAguaService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(registroQualidadeAguaMapper.toResponse(registro));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public void remover(@PathVariable UUID id) {
        registroQualidadeAguaService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<ImportResultado> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        var resultado = registroQualidadeAguaService.importar(
                SecurityUtils.currentEmpresaId(), SecurityUtils.currentUserId(), arquivo);
        return ApiResponse.of(resultado);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] arquivo = registroQualidadeAguaService.exportar(SecurityUtils.currentEmpresaId());
        return arquivoExcel(arquivo, "qualidade-agua.xlsx");
    }

    @GetMapping("/importar/modelo")
    public ResponseEntity<byte[]> baixarModelo() {
        byte[] arquivo = registroQualidadeAguaService.gerarModeloImportacao();
        return arquivoExcel(arquivo, "modelo-qualidade-agua.xlsx");
    }

    private ResponseEntity<byte[]> arquivoExcel(byte[] conteudo, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}
