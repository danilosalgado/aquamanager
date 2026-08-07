package com.aquamanager.modules.saude.application;

import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.saude.application.dto.IndiceSaudeHistoricoItem;
import com.aquamanager.modules.saude.application.dto.IndiceSaudeResponse;
import com.aquamanager.modules.saude.domain.Classificacao;
import com.aquamanager.modules.saude.infrastructure.persistence.IndiceSaudeTanqueRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndiceSaudeServiceImpl implements IndiceSaudeService {

    private final TanqueRepository tanqueRepository;
    private final LoteRepository loteRepository;
    private final RegistroQualidadeAguaRepository qualidadeAguaRepository;
    private final RegistroCrescimentoRepository crescimentoRepository;
    private final RegistroMortalidadeRepository mortalidadeRepository;
    private final RegistroAlimentacaoRepository alimentacaoRepository;
    private final IndiceSaudeTanqueRepository indiceSaudeRepository;

    @Override
    @Transactional(readOnly = true)
    public IndiceSaudeResponse calcularAtual(UUID empresaId, UUID tanqueId) {
        Tanque tanque = tanqueRepository.findById(tanqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Tanque", tanqueId));
        if (!tanque.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Tanque", tanqueId);
        }
        return calcular(tanqueId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndiceSaudeHistoricoItem> historico(UUID empresaId, UUID tanqueId, LocalDate inicio, LocalDate fim) {
        return indiceSaudeRepository.findByEmpresaIdAndTanqueIdAndDataBetweenOrderByDataAsc(empresaId, tanqueId, inicio, fim)
                .stream()
                .map(i -> new IndiceSaudeHistoricoItem(i.getData(), i.getScore(), i.getClassificacao().name()))
                .toList();
    }

    /**
     * Algoritmo do índice de saúde (0-100): pondera qualidade da água, conversão
     * alimentar, mortalidade recente e crescimento. Componentes sem dados suficientes
     * são excluídos do denominador (não penalizam nem favorecem o tanque).
     */
    public IndiceSaudeResponse calcular(UUID tanqueId) {
        List<String> detalhes = new ArrayList<>();
        double pesoTotal = 0;
        double pesoObtido = 0;

        List<Lote> lotesAtivos = loteRepository.findByTanqueIdAndStatus(tanqueId, StatusLote.ATIVO);
        if (lotesAtivos.isEmpty()) {
            return new IndiceSaudeResponse(tanqueId, null, null, true,
                    List.of("Nenhum lote ativo neste tanque."));
        }
        Lote lote = lotesAtivos.get(0);
        Especie especie = lote.getEspecie();

        // --- Qualidade da água (55 pts: temperatura 20, oxigênio 20, pH 15) ---
        RegistroQualidadeAgua ultimaAgua = qualidadeAguaRepository
                .findFirstByTanqueIdOrderByMedidoEmDesc(tanqueId).orElse(null);
        if (ultimaAgua != null) {
            pesoTotal += 20;
            if (dentroDaFaixa(ultimaAgua.getTemperatura(), especie.getTempMin(), especie.getTempMax())) {
                pesoObtido += 20;
            } else {
                detalhes.add("Temperatura fora da faixa ideal.");
            }

            if (ultimaAgua.getOxigenioDissolvido() != null && especie.getOxigenioMin() != null) {
                pesoTotal += 20;
                if (ultimaAgua.getOxigenioDissolvido().compareTo(especie.getOxigenioMin()) >= 0) {
                    pesoObtido += 20;
                } else {
                    detalhes.add("Oxigênio dissolvido abaixo do mínimo ideal.");
                }
            }

            pesoTotal += 15;
            if (dentroDaFaixa(ultimaAgua.getPh(), especie.getPhMin(), especie.getPhMax())) {
                pesoObtido += 15;
            } else {
                detalhes.add("pH fora da faixa ideal.");
            }
        } else {
            detalhes.add("Sem registros recentes de qualidade da água.");
        }

        // --- Conversão alimentar / FCR (15 pts) ---
        Instant seteDiasAtras = Instant.now().minus(7, ChronoUnit.DAYS);
        BigDecimal racaoConsumida = alimentacaoRepository.findByLoteIdAndHorarioBetween(lote.getId(), seteDiasAtras, Instant.now())
                .stream().map(r -> r.getQuantidadeKg()).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RegistroCrescimento> ultimasPesagens = crescimentoRepository.findTop2ByLoteIdOrderByDataPesagemDesc(lote.getId());
        if (racaoConsumida.compareTo(BigDecimal.ZERO) > 0 && ultimasPesagens.size() == 2) {
            BigDecimal ganhoBiomassaKg = ultimasPesagens.get(0).getBiomassaKg().subtract(ultimasPesagens.get(1).getBiomassaKg());
            pesoTotal += 15;
            if (ganhoBiomassaKg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal fcr = racaoConsumida.divide(ganhoBiomassaKg, 2, RoundingMode.HALF_UP);
                if (fcr.compareTo(BigDecimal.valueOf(1.8)) <= 0) {
                    pesoObtido += 15;
                } else if (fcr.compareTo(BigDecimal.valueOf(2.5)) <= 0) {
                    pesoObtido += 8;
                    detalhes.add("Conversão alimentar mediana (FCR " + fcr + ").");
                } else {
                    detalhes.add("Conversão alimentar ruim (FCR " + fcr + ").");
                }
            } else {
                detalhes.add("Biomassa não cresceu no período apesar do consumo de ração.");
            }
        }

        // --- Mortalidade recente (15 pts) ---
        List<RegistroMortalidade> mortalidadeRecente = mortalidadeRepository
                .findByLoteIdAndDataGreaterThanEqual(lote.getId(), LocalDate.now().minusDays(7));
        int totalMorto = mortalidadeRecente.stream().mapToInt(RegistroMortalidade::getQuantidade).sum();
        if (lote.getQuantidadeAtual() != null && lote.getQuantidadeAtual() > 0) {
            pesoTotal += 15;
            double percentualMorte = totalMorto / (double) (lote.getQuantidadeAtual() + totalMorto);
            if (percentualMorte <= 0.02) {
                pesoObtido += 15;
            } else if (percentualMorte <= 0.10) {
                pesoObtido += 15 * (1 - (percentualMorte - 0.02) / 0.08);
                detalhes.add("Mortalidade recente moderada.");
            } else {
                detalhes.add("Mortalidade recente elevada.");
            }
        }

        // --- Crescimento (15 pts) ---
        if (ultimasPesagens.size() == 2) {
            pesoTotal += 15;
            int comparacao = ultimasPesagens.get(0).getPesoMedioG().compareTo(ultimasPesagens.get(1).getPesoMedioG());
            if (comparacao > 0) {
                pesoObtido += 15;
            } else if (comparacao == 0) {
                pesoObtido += 8;
                detalhes.add("Peso médio estagnado desde a última pesagem.");
            } else {
                detalhes.add("Peso médio caiu desde a última pesagem.");
            }
        }

        if (pesoTotal == 0) {
            return new IndiceSaudeResponse(tanqueId, null, null, true,
                    List.of("Dados insuficientes para calcular o índice de saúde."));
        }

        int score = (int) Math.round((pesoObtido / pesoTotal) * 100);
        Classificacao classificacao = Classificacao.daNota(score);
        return new IndiceSaudeResponse(tanqueId, score, classificacao.name(), false, detalhes);
    }

    private boolean dentroDaFaixa(BigDecimal valor, BigDecimal min, BigDecimal max) {
        if (valor == null || min == null || max == null) {
            return true; // sem faixa definida para a espécie, não penaliza
        }
        return valor.compareTo(min) >= 0 && valor.compareTo(max) <= 0;
    }
}
