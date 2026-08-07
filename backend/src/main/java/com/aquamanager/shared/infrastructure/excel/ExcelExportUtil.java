package com.aquamanager.shared.infrastructure.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Escritor genérico de planilhas .xlsx, usado tanto para exportar dados existentes quanto
 * para gerar os modelos (templates) de importação — uma linha em branco com apenas o
 * cabeçalho (e opcionalmente uma linha de exemplo) já serve como template.
 */
public final class ExcelExportUtil {

    private ExcelExportUtil() {
    }

    public static byte[] gerar(String nomeAba, List<String> cabecalhos, List<List<Object>> linhas) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(nomeAba.length() > 31 ? nomeAba.substring(0, 31) : nomeAba);

            CellStyle estiloCabecalho = workbook.createCellStyle();
            Font fonteCabecalho = workbook.createFont();
            fonteCabecalho.setBold(true);
            estiloCabecalho.setFont(fonteCabecalho);

            CellStyle estiloData = workbook.createCellStyle();
            estiloData.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
            CellStyle estiloDataHora = workbook.createCellStyle();
            estiloDataHora.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < cabecalhos.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabecalhos.get(i));
                cell.setCellStyle(estiloCabecalho);
            }
            sheet.createFreezePane(0, 1);

            int r = 1;
            for (List<Object> linha : linhas) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < linha.size(); c++) {
                    escreverCelula(row.createCell(c), linha.get(c), estiloData, estiloDataHora);
                }
            }

            for (int i = 0; i < cabecalhos.size(); i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar planilha Excel.", e);
        }
    }

    private static void escreverCelula(Cell cell, Object valor, CellStyle estiloData, CellStyle estiloDataHora) {
        if (valor == null) {
            return;
        }
        if (valor instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (valor instanceof LocalDate ld) {
            cell.setCellValue(ld);
            cell.setCellStyle(estiloData);
        } else if (valor instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt);
            cell.setCellStyle(estiloDataHora);
        } else if (valor instanceof Instant instant) {
            cell.setCellValue(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
            cell.setCellStyle(estiloDataHora);
        } else {
            cell.setCellValue(valor.toString());
        }
    }
}
