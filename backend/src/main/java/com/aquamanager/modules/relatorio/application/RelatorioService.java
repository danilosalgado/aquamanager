package com.aquamanager.modules.relatorio.application;

import java.time.Instant;
import java.util.UUID;

public interface RelatorioService {
    
    byte[] gerarRelatorioFinanceiro(UUID empresaId, Instant inicio, Instant fim, String formato);
    
    byte[] gerarRelatorioProducao(UUID empresaId, Instant inicio, Instant fim, String formato);
    
    byte[] gerarRelatorioMortalidade(UUID empresaId, Instant inicio, Instant fim, String formato);
    
    byte[] gerarRelatorioEstoque(UUID empresaId, String formato);
}
