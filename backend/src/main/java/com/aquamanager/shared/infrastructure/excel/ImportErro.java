package com.aquamanager.shared.infrastructure.excel;

/** linha é o número da linha exibido no Excel (considerando o cabeçalho como linha 1). */
public record ImportErro(int linha, String motivo) {
}
