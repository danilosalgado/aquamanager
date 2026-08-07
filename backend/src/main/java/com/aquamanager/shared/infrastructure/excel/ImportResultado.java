package com.aquamanager.shared.infrastructure.excel;

import java.util.List;

public record ImportResultado(int totalLinhas, int importados, List<ImportErro> erros) {
}
