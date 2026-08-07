package com.aquamanager.modules.alimentacao.infrastructure.web;

import com.aquamanager.modules.alimentacao.application.RegistroAlimentacaoService;
import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoRequest;
import com.aquamanager.modules.alimentacao.application.dto.RegistroAlimentacaoResponse;
import com.aquamanager.modules.alimentacao.infrastructure.mapper.RegistroAlimentacaoMapper;
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
@RequestMapping("/api/v1/alimentacao")
@RequiredArgsConstructor
public class RegistroAlimentacaoController {

    private final RegistroAlimentacaoService registroAlimentacaoService;
    private final RegistroAlimentacaoMapper registroAlimentacaoMapper;

    @GetMapping
    public ApiResponse<PageResponse<RegistroAlimentacaoResponse>> listar(
            @RequestParam(required = false) UUID loteId, Pageable pageable) {
        var page = registroAlimentacaoService.listar(SecurityUtils.currentEmpresaId(), loteId, pageable)
                .map(registroAlimentacaoMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistroAlimentacaoResponse> buscar(@PathVariable UUID id) {
        var registro = registroAlimentacaoService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(registroAlimentacaoMapper.toResponse(registro));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroAlimentacaoResponse> criar(@Valid @RequestBody RegistroAlimentacaoRequest request) {
        var registro = registroAlimentacaoService.criar(
                SecurityUtils.currentEmpresaId(), SecurityUtils.currentUserId(), request);
        return ApiResponse.of(registroAlimentacaoMapper.toResponse(registro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroAlimentacaoResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody RegistroAlimentacaoRequest request) {
        var registro = registroAlimentacaoService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(registroAlimentacaoMapper.toResponse(registro));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public void remover(@PathVariable UUID id) {
        registroAlimentacaoService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<ImportResultado> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        var resultado = registroAlimentacaoService.importar(
                SecurityUtils.currentEmpresaId(), SecurityUtils.currentUserId(), arquivo);
        return ApiResponse.of(resultado);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] arquivo = registroAlimentacaoService.exportar(SecurityUtils.currentEmpresaId());
        return arquivoExcel(arquivo, "alimentacao.xlsx");
    }

    @GetMapping("/importar/modelo")
    public ResponseEntity<byte[]> baixarModelo() {
        byte[] arquivo = registroAlimentacaoService.gerarModeloImportacao();
        return arquivoExcel(arquivo, "modelo-alimentacao.xlsx");
    }

    private ResponseEntity<byte[]> arquivoExcel(byte[] conteudo, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}
