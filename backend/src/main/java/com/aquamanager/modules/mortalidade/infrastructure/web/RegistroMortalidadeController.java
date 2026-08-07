package com.aquamanager.modules.mortalidade.infrastructure.web;

import com.aquamanager.modules.mortalidade.application.RegistroMortalidadeService;
import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeRequest;
import com.aquamanager.modules.mortalidade.application.dto.RegistroMortalidadeResponse;
import com.aquamanager.modules.mortalidade.infrastructure.mapper.RegistroMortalidadeMapper;
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
@RequestMapping("/api/v1/mortalidade")
@RequiredArgsConstructor
public class RegistroMortalidadeController {

    private final RegistroMortalidadeService registroMortalidadeService;
    private final RegistroMortalidadeMapper registroMortalidadeMapper;

    @GetMapping
    public ApiResponse<PageResponse<RegistroMortalidadeResponse>> listar(
            @RequestParam(required = false) UUID loteId, Pageable pageable) {
        var page = registroMortalidadeService.listar(SecurityUtils.currentEmpresaId(), loteId, pageable)
                .map(registroMortalidadeMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<RegistroMortalidadeResponse> buscar(@PathVariable UUID id) {
        var registro = registroMortalidadeService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(registroMortalidadeMapper.toResponse(registro));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroMortalidadeResponse> criar(@Valid @RequestBody RegistroMortalidadeRequest request) {
        var registro = registroMortalidadeService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(registroMortalidadeMapper.toResponse(registro));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<RegistroMortalidadeResponse> atualizar(
            @PathVariable UUID id, @Valid @RequestBody RegistroMortalidadeRequest request) {
        var registro = registroMortalidadeService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(registroMortalidadeMapper.toResponse(registro));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public void remover(@PathVariable UUID id) {
        registroMortalidadeService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'FUNCIONARIO')")
    public ApiResponse<ImportResultado> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        var resultado = registroMortalidadeService.importar(SecurityUtils.currentEmpresaId(), arquivo);
        return ApiResponse.of(resultado);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] arquivo = registroMortalidadeService.exportar(SecurityUtils.currentEmpresaId());
        return arquivoExcel(arquivo, "mortalidade.xlsx");
    }

    @GetMapping("/importar/modelo")
    public ResponseEntity<byte[]> baixarModelo() {
        byte[] arquivo = registroMortalidadeService.gerarModeloImportacao();
        return arquivoExcel(arquivo, "modelo-mortalidade.xlsx");
    }

    private ResponseEntity<byte[]> arquivoExcel(byte[] conteudo, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}
