package com.aquamanager.modules.relatorio.infrastructure.web;

import com.aquamanager.modules.relatorio.application.RelatorioService;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/financeiro")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'CONSULTOR')")
    public ResponseEntity<byte[]> relatorioFinanceiro(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(defaultValue = "pdf") String formato) {
        
        byte[] relatorio = relatorioService.gerarRelatorioFinanceiro(SecurityUtils.currentEmpresaId(), inicio, fim, formato);
        return buildResponse(relatorio, "relatorio-financeiro", formato);
    }

    @GetMapping("/producao")
    public ResponseEntity<byte[]> relatorioProducao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(defaultValue = "pdf") String formato) {
        
        byte[] relatorio = relatorioService.gerarRelatorioProducao(SecurityUtils.currentEmpresaId(), inicio, fim, formato);
        return buildResponse(relatorio, "relatorio-producao", formato);
    }

    @GetMapping("/mortalidade")
    public ResponseEntity<byte[]> relatorioMortalidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(defaultValue = "pdf") String formato) {
        
        byte[] relatorio = relatorioService.gerarRelatorioMortalidade(SecurityUtils.currentEmpresaId(), inicio, fim, formato);
        return buildResponse(relatorio, "relatorio-mortalidade", formato);
    }

    @GetMapping("/estoque")
    public ResponseEntity<byte[]> relatorioEstoque(@RequestParam(defaultValue = "pdf") String formato) {
        byte[] relatorio = relatorioService.gerarRelatorioEstoque(SecurityUtils.currentEmpresaId(), formato);
        return buildResponse(relatorio, "relatorio-estoque", formato);
    }

    private ResponseEntity<byte[]> buildResponse(byte[] content, String filename, String formato) {
        boolean isExcel = "excel".equalsIgnoreCase(formato);
        String ext = isExcel ? ".xlsx" : ".pdf";
        MediaType mediaType = isExcel 
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ext + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
