package com.aquamanager.modules.relatorio.application;

import com.aquamanager.modules.estoque.domain.EstoqueItem;
import com.aquamanager.modules.estoque.infrastructure.persistence.EstoqueItemRepository;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelatorioServiceImpl implements RelatorioService {

    private final LancamentoFinanceiroRepository financeiroRepository;
    private final RegistroMortalidadeRepository mortalidadeRepository;
    private final EstoqueItemRepository estoqueRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @Override
    @Transactional(readOnly = true)
    public byte[] gerarRelatorioFinanceiro(UUID empresaId, Instant inicio, Instant fim, String formato) {
        LocalDate start = LocalDate.ofInstant(inicio, ZoneId.systemDefault());
        LocalDate end = LocalDate.ofInstant(fim, ZoneId.systemDefault());
        List<LancamentoFinanceiro> lancamentos = financeiroRepository.findByEmpresaIdAndDataVencimentoBetween(empresaId, start, end);
        
        String[] headers = {"Data", "Tipo", "Categoria", "Descrição", "Valor", "Status"};
        String[][] data = lancamentos.stream().map(l -> new String[]{
                l.getDataVencimento() != null ? l.getDataVencimento().toString() : "",
                l.getTipo().name(),
                l.getCategoria(),
                l.getDescricao(),
                String.format("R$ %.2f", l.getValor()),
                l.getStatus().name()
        }).toArray(String[][]::new);

        return gerarArquivo("Relatório Financeiro", headers, data, formato);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] gerarRelatorioProducao(UUID empresaId, Instant inicio, Instant fim, String formato) {
        // Implementação simplificada, usar lotes ou safras como produção
        String[] headers = {"Lote", "Espécie", "Quantidade", "Peso Médio (g)", "Data Início"};
        String[][] data = new String[0][0]; // Placeholder

        return gerarArquivo("Relatório de Produção", headers, data, formato);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] gerarRelatorioMortalidade(UUID empresaId, Instant inicio, Instant fim, String formato) {
        LocalDate start = LocalDate.ofInstant(inicio, ZoneId.systemDefault());
        LocalDate end = LocalDate.ofInstant(fim, ZoneId.systemDefault());
        List<RegistroMortalidade> registros = mortalidadeRepository.findByEmpresaIdAndDataBetween(empresaId, start, end);
        
        String[] headers = {"Data", "Lote", "Quantidade", "Motivo", "Observações"};
        String[][] data = registros.stream().map(r -> new String[]{
                r.getData() != null ? r.getData().toString() : "",
                r.getLote().getId().toString(),
                String.valueOf(r.getQuantidade()),
                r.getMotivo(),
                r.getObservacoes() != null ? r.getObservacoes() : ""
        }).toArray(String[][]::new);

        return gerarArquivo("Relatório de Mortalidade", headers, data, formato);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] gerarRelatorioEstoque(UUID empresaId, String formato) {
        List<EstoqueItem> itens = estoqueRepository.findByEmpresaId(empresaId);
        
        String[] headers = {"Item", "Categoria", "Quantidade", "Unidade", "Alerta", "Atualizado em"};
        String[][] data = itens.stream().map(i -> new String[]{
                i.getNome(),
                i.getCategoria().name(),
                String.format("%.2f", i.getQuantidadeAtual()),
                i.getUnidade(),
                i.getQuantidadeMinima() != null && i.getQuantidadeAtual().compareTo(i.getQuantidadeMinima()) <= 0 ? "BAIXO" : "OK",
                DATE_FORMATTER.format(i.getUpdatedAt())
        }).toArray(String[][]::new);

        return gerarArquivo("Relatório de Estoque", headers, data, formato);
    }

    private byte[] gerarArquivo(String titulo, String[] headers, String[][] data, String formato) {
        if ("excel".equalsIgnoreCase(formato)) {
            return gerarExcel(titulo, headers, data);
        }
        return gerarPdf(titulo, headers, data);
    }

    private byte[] gerarExcel(String titulo, String[] headers, String[][] data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(titulo);
            
            // Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // Data
            int rowIdx = 1;
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório Excel", e);
        }
    }

    private byte[] gerarPdf(String titulo, String[] headers, String[][] data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();
            
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph p = new Paragraph(titulo, titleFont);
            p.setSpacingAfter(20);
            document.add(p);
            
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            Font dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            for (String[] rowData : data) {
                for (String text : rowData) {
                    PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", dataFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }
}
