package com.aquamanager.shared.infrastructure.excel;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Leitor genérico de planilhas .xlsx para os fluxos de importação em massa. A primeira
 * linha da primeira aba é tratada como cabeçalho (nome da coluna → índice, casamento
 * exato após trim/lowercase — por isso os templates gerados por {@link ExcelExportUtil}
 * devem ser reutilizados como base pelo usuário). Linhas totalmente vazias são ignoradas.
 */
public class ExcelSheetReader implements AutoCloseable {

    private static final DateTimeFormatter DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Workbook workbook;
    private final Sheet sheet;
    private final Map<String, Integer> colunas = new LinkedHashMap<>();

    public ExcelSheetReader(InputStream in, List<String> colunasObrigatorias) throws IOException {
        this.workbook = WorkbookFactory.create(in);
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("A planilha não contém nenhuma aba.");
        }
        this.sheet = workbook.getSheetAt(0);
        Row header = sheet.getRow(0);
        if (header == null) {
            throw new IllegalArgumentException("A planilha está vazia — a primeira linha deve conter os nomes das colunas.");
        }
        for (Cell cell : header) {
            String nome = valorTexto(cell);
            if (nome != null && !nome.isBlank()) {
                colunas.put(normalizar(nome), cell.getColumnIndex());
            }
        }

        List<String> faltando = colunasObrigatorias.stream()
                .filter(c -> !colunas.containsKey(normalizar(c)))
                .toList();
        if (!faltando.isEmpty()) {
            throw new IllegalArgumentException(
                    "Coluna(s) obrigatória(s) não encontrada(s) na planilha: " + String.join(", ", faltando)
                            + ". Baixe o modelo novamente para conferir os nomes exatos das colunas.");
        }
    }

    /** Índices (0-based, na aba) de todas as linhas de dados não vazias, na ordem em que aparecem. */
    public List<Integer> indicesLinhasDados() {
        List<Integer> out = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && !linhaVazia(row)) {
                out.add(i);
            }
        }
        return out;
    }

    /** Número da linha como aparece no Excel (cabeçalho = linha 1). */
    public static int numeroLinhaExcel(int indice) {
        return indice + 1;
    }

    public Row linha(int indice) {
        return sheet.getRow(indice);
    }

    public String texto(Row row, String coluna) {
        String v = valorTexto(obterCelula(row, coluna));
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    public BigDecimal decimal(Row row, String coluna) {
        Cell cell = obterCelula(row, coluna);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String texto = valorTexto(cell);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(texto.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido na coluna \"" + coluna + "\": \"" + texto + "\".");
        }
    }

    public Integer inteiro(Row row, String coluna) {
        BigDecimal valor = decimal(row, coluna);
        if (valor == null) {
            return null;
        }
        return valor.setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    public LocalDate data(Row row, String coluna) {
        LocalDateTime dataHora = dataHora(row, coluna);
        return dataHora == null ? null : dataHora.toLocalDate();
    }

    public LocalDateTime dataHora(Row row, String coluna) {
        Cell cell = obterCelula(row, coluna);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        String texto = valorTexto(cell);
        if (texto == null || texto.isBlank()) {
            return null;
        }
        texto = texto.trim();
        try {
            return LocalDateTime.parse(texto, DATA_HORA_BR);
        } catch (DateTimeParseException ignored) {
            // tenta o próximo formato
        }
        try {
            return LocalDate.parse(texto, DATA_BR).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // tenta o próximo formato
        }
        try {
            return LocalDateTime.parse(texto);
        } catch (DateTimeParseException ignored) {
            // tenta o próximo formato
        }
        try {
            return LocalDate.parse(texto).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data inválida na coluna \"" + coluna + "\": \"" + texto + "\". Use o formato dd/mm/aaaa ou dd/mm/aaaa hh:mm.");
        }
    }

    private boolean linhaVazia(Row row) {
        for (Cell cell : row) {
            String v = valorTexto(cell);
            if (v != null && !v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private Cell obterCelula(Row row, String coluna) {
        if (row == null) {
            return null;
        }
        Integer idx = colunas.get(normalizar(coluna));
        return idx == null ? null : row.getCell(idx);
    }

    private static String valorTexto(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double valor = cell.getNumericCellValue();
                yield (valor == Math.floor(valor) && !Double.isInfinite(valor))
                        ? String.valueOf((long) valor)
                        : String.valueOf(valor);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> null;
        };
    }

    private static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
