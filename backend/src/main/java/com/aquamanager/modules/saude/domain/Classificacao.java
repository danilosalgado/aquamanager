package com.aquamanager.modules.saude.domain;

public enum Classificacao {
    EXCELENTE,
    ATENCAO,
    CRITICO;

    public static Classificacao daNota(int score) {
        if (score >= 80) return EXCELENTE;
        if (score >= 50) return ATENCAO;
        return CRITICO;
    }
}
