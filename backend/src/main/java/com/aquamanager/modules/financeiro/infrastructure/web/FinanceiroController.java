package com.aquamanager.modules.financeiro.infrastructure.web;

import com.aquamanager.modules.financeiro.application.FinanceiroService;
import com.aquamanager.modules.financeiro.application.dto.LancamentoRequest;
import com.aquamanager.modules.financeiro.application.dto.LancamentoResponse;
import com.aquamanager.modules.financeiro.application.dto.ResumoFinanceiroResponse;
import com.aquamanager.modules.financeiro.infrastructure.mapper.LancamentoMapper;
import com.aquamanager.shared.infrastructure.excel.ImportResultado;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import com.aquamanager.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final FinanceiroService financeiroService;
    private final LancamentoMapper lancamentoMapper;

    @GetMapping("/lancamentos")
    public ApiResponse<PageResponse<LancamentoResponse>> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        var page = financeiroService.listar(SecurityUtils.currentEmpresaId(), tipo, status, pageable)
                .map(lancamentoMapper::toResponse);
        return ApiResponse.of(PageResponse.from(page));
    }

    @GetMapping("/lancamentos/{id}")
    public ApiResponse<LancamentoResponse> buscar(@PathVariable UUID id) {
        var lancamento = financeiroService.buscar(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(lancamentoMapper.toResponse(lancamento));
    }

    @PostMapping("/lancamentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<LancamentoResponse> criar(@Valid @RequestBody LancamentoRequest request) {
        var lancamento = financeiroService.criar(SecurityUtils.currentEmpresaId(), request);
        return ApiResponse.of(lancamentoMapper.toResponse(lancamento));
    }

    @PutMapping("/lancamentos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<LancamentoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody LancamentoRequest request) {
        var lancamento = financeiroService.atualizar(SecurityUtils.currentEmpresaId(), id, request);
        return ApiResponse.of(lancamentoMapper.toResponse(lancamento));
    }

    @PostMapping("/lancamentos/{id}/pagar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<LancamentoResponse> marcarComoPago(@PathVariable UUID id) {
        var lancamento = financeiroService.marcarComoPago(SecurityUtils.currentEmpresaId(), id);
        return ApiResponse.of(lancamentoMapper.toResponse(lancamento));
    }

    @DeleteMapping("/lancamentos/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void remover(@PathVariable UUID id) {
        financeiroService.remover(SecurityUtils.currentEmpresaId(), id);
    }

    @GetMapping("/resumo")
    public ApiResponse<ResumoFinanceiroResponse> resumo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ApiResponse.of(financeiroService.resumo(SecurityUtils.currentEmpresaId(), inicio, fim));
    }

    @PostMapping("/lancamentos/importar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    public ApiResponse<ImportResultado> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        var resultado = financeiroService.importar(SecurityUtils.currentEmpresaId(), arquivo);
        return ApiResponse.of(resultado);
    }

    @GetMapping("/lancamentos/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] arquivo = financeiroService.exportar(SecurityUtils.currentEmpresaId());
        return arquivoExcel(arquivo, "financeiro.xlsx");
    }

    @GetMapping("/lancamentos/importar/modelo")
    public ResponseEntity<byte[]> baixarModelo() {
        byte[] arquivo = financeiroService.gerarModeloImportacao();
        return arquivoExcel(arquivo, "modelo-financeiro.xlsx");
    }

    private ResponseEntity<byte[]> arquivoExcel(byte[] conteudo, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(conteudo);
    }
}
