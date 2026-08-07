package com.aquamanager.modules.dashboard.application;

import com.aquamanager.modules.alerta.infrastructure.persistence.AlertaRepository;
import com.aquamanager.modules.alimentacao.domain.RegistroAlimentacao;
import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.dashboard.application.dto.DashboardResumoResponse;
import com.aquamanager.modules.dashboard.application.dto.ProducaoPorTanqueResponse;
import com.aquamanager.modules.dashboard.application.dto.QualidadeAguaPoint;
import com.aquamanager.modules.dashboard.application.dto.SaudeMediaDiariaResponse;
import com.aquamanager.modules.dashboard.application.dto.SeriePontoResponse;
import com.aquamanager.modules.dashboard.application.dto.TimeSeriesPoint;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.saude.application.IndiceSaudeServiceImpl;
import com.aquamanager.modules.saude.domain.IndiceSaudeTanque;
import com.aquamanager.modules.saude.infrastructure.persistence.IndiceSaudeTanqueRepository;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TanqueRepository tanqueRepository;
    private final LoteRepository loteRepository;
    private final RegistroCrescimentoRepository crescimentoRepository;
    private final RegistroAlimentacaoRepository alimentacaoRepository;
    private final RegistroMortalidadeRepository mortalidadeRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AlertaRepository alertaRepository;
    private final IndiceSaudeServiceImpl indiceSaudeService;
    private final IndiceSaudeTanqueRepository indiceSaudeTanqueRepository;
    private final RegistroQualidadeAguaRepository qualidadeAguaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumoResponse resumo(UUID empresaId) {
        long quantidadeTanques = tanqueRepository.countByEmpresaId(empresaId);
        List<Lote> lotesAtivos = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                .getContent();

        long peixes = lotesAtivos.stream().mapToLong(l -> l.getQuantidadeAtual() == null ? 0 : l.getQuantidadeAtual()).sum();
        BigDecimal biomassaTotalKg = lotesAtivos.stream()
                .map(l -> BigDecimal.valueOf(l.getQuantidadeAtual())
                        .multiply(l.getPesoAtualG())
                        .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pesoMedioG = lotesAtivos.isEmpty() ? BigDecimal.ZERO
                : lotesAtivos.stream().map(Lote::getPesoAtualG).reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(lotesAtivos.size()), 2, RoundingMode.HALF_UP);

        BigDecimal conversaoAlimentar = calcularFcrMedio(lotesAtivos);

        LocalDate hoje = LocalDate.now();
        LocalDate ha30Dias = hoje.minusDays(30);
        BigDecimal receita30 = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.RECEITA, ha30Dias, hoje);
        BigDecimal despesa30 = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.DESPESA, ha30Dias, hoje);

        long mortalidade30 = lotesAtivos.stream()
                .flatMap(l -> mortalidadeRepository.findByLoteIdAndDataGreaterThanEqual(l.getId(), ha30Dias).stream())
                .mapToLong(m -> m.getQuantidade())
                .sum();

        long alertasNaoLidos = alertaRepository.countByEmpresaIdAndLido(empresaId, false);

        List<Tanque> tanquesAtivos = tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO);
        List<Integer> scores = tanquesAtivos.stream()
                .map(t -> indiceSaudeService.calcular(t.getId()))
                .filter(r -> !r.semDadosSuficientes() && r.score() != null)
                .map(r -> r.score())
                .toList();
        Double indiceSaudeMedio = scores.isEmpty() ? null
                : scores.stream().mapToInt(Integer::intValue).average().orElse(0);

        return new DashboardResumoResponse(quantidadeTanques, lotesAtivos.size(), peixes, biomassaTotalKg, pesoMedioG,
                conversaoAlimentar, receita30, despesa30, receita30.subtract(despesa30), mortalidade30, alertasNaoLidos,
                indiceSaudeMedio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeriePontoResponse> graficoFinanceiro(UUID empresaId, int meses) {
        List<SeriePontoResponse> pontos = new ArrayList<>();
        LocalDate referencia = LocalDate.now().withDayOfMonth(1);
        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = referencia.minusMonths(i);
            LocalDate fimMes = inicioMes.plusMonths(1).minusDays(1);
            BigDecimal receita = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.RECEITA, inicioMes, fimMes);
            BigDecimal despesa = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.DESPESA, inicioMes, fimMes);
            String rotulo = inicioMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")) + "/" + inicioMes.getYear();
            pontos.add(new SeriePontoResponse(rotulo, receita, despesa));
        }
        return pontos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaudeMediaDiariaResponse> graficoIndiceSaudeMedio(UUID empresaId, int dias) {
        LocalDate inicio = LocalDate.now().minusDays(dias);
        List<IndiceSaudeTanque> registros = indiceSaudeTanqueRepository
                .findByEmpresaIdAndDataBetweenOrderByDataAsc(empresaId, inicio, LocalDate.now());

        Map<LocalDate, List<IndiceSaudeTanque>> porData = registros.stream()
                .collect(Collectors.groupingBy(IndiceSaudeTanque::getData));

        return porData.entrySet().stream()
                .map(e -> new SaudeMediaDiariaResponse(e.getKey(),
                        e.getValue().stream().mapToInt(IndiceSaudeTanque::getScore).average().orElse(0)))
                .sorted(Comparator.comparing(SaudeMediaDiariaResponse::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPoint> graficoCrescimento(UUID empresaId, int dias) {
        List<Lote> lotesAtivos = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                .getContent();
        LocalDate inicio = LocalDate.now().minusDays(dias);

        List<RegistroCrescimento> todos = new ArrayList<>();
        for (Lote lote : lotesAtivos) {
            crescimentoRepository.findByEmpresaIdAndLoteId(empresaId, lote.getId(), Pageable.unpaged())
                    .getContent().stream()
                    .filter(r -> r.getDataPesagem() != null && !r.getDataPesagem().isBefore(inicio))
                    .forEach(todos::add);
        }

        return todos.stream()
                .collect(Collectors.groupingBy(RegistroCrescimento::getDataPesagem))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal pesoMedio = e.getValue().stream()
                            .map(RegistroCrescimento::getPesoMedioG)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(e.getValue().size()), 2, RoundingMode.HALF_UP);
                    return new TimeSeriesPoint(e.getKey(), pesoMedio);
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPoint> graficoConsumoRacao(UUID empresaId, int dias) {
        Instant inicio = Instant.now().minus(dias, ChronoUnit.DAYS);
        List<Lote> lotesAtivos = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                .getContent();

        List<RegistroAlimentacao> todos = new ArrayList<>();
        for (Lote lote : lotesAtivos) {
            todos.addAll(alimentacaoRepository.findByLoteIdAndHorarioBetween(lote.getId(), inicio, Instant.now()));
        }

        return todos.stream()
                .collect(Collectors.groupingBy(r -> r.getHorario().atZone(ZoneId.systemDefault()).toLocalDate()))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(RegistroAlimentacao::getQuantidadeKg)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TimeSeriesPoint(e.getKey(), total);
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPoint> graficoMortalidade(UUID empresaId, int dias) {
        LocalDate inicio = LocalDate.now().minusDays(dias);
        List<Lote> lotesAtivos = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                .getContent();

        List<RegistroMortalidade> todos = new ArrayList<>();
        for (Lote lote : lotesAtivos) {
            todos.addAll(mortalidadeRepository.findByLoteIdAndDataGreaterThanEqual(lote.getId(), inicio));
        }

        return todos.stream()
                .collect(Collectors.groupingBy(RegistroMortalidade::getData))
                .entrySet().stream()
                .map(e -> {
                    long total = e.getValue().stream().mapToLong(RegistroMortalidade::getQuantidade).sum();
                    return new TimeSeriesPoint(e.getKey(), BigDecimal.valueOf(total));
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualidadeAguaPoint> graficoQualidadeAgua(UUID empresaId, int dias) {
        Instant inicio = Instant.now().minus(dias, ChronoUnit.DAYS);
        List<Tanque> tanques = tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO);

        List<RegistroQualidadeAgua> todos = new ArrayList<>();
        for (Tanque tanque : tanques) {
            todos.addAll(qualidadeAguaRepository.findByTanqueIdAndMedidoEmBetween(tanque.getId(), inicio, Instant.now()));
        }

        return todos.stream()
                .collect(Collectors.groupingBy(r -> r.getMedidoEm().atZone(ZoneId.systemDefault()).toLocalDate()))
                .entrySet().stream()
                .map(e -> {
                    List<RegistroQualidadeAgua> regs = e.getValue();
                    BigDecimal tempMedia = media(regs.stream().map(RegistroQualidadeAgua::getTemperatura).filter(v -> v != null).toList());
                    BigDecimal phMedio = media(regs.stream().map(RegistroQualidadeAgua::getPh).filter(v -> v != null).toList());
                    BigDecimal o2Medio = media(regs.stream().map(RegistroQualidadeAgua::getOxigenioDissolvido).filter(v -> v != null).toList());
                    return new QualidadeAguaPoint(e.getKey(), tempMedia, phMedio, o2Medio);
                })
                .sorted(Comparator.comparing(QualidadeAguaPoint::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPoint> graficoBiomassa(UUID empresaId, int dias) {
        List<Lote> lotesAtivos = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                .getContent();
        LocalDate inicio = LocalDate.now().minusDays(dias);

        List<RegistroCrescimento> todos = new ArrayList<>();
        for (Lote lote : lotesAtivos) {
            crescimentoRepository.findByEmpresaIdAndLoteId(empresaId, lote.getId(), Pageable.unpaged())
                    .getContent().stream()
                    .filter(r -> r.getDataPesagem() != null && !r.getDataPesagem().isBefore(inicio))
                    .forEach(todos::add);
        }

        return todos.stream()
                .collect(Collectors.groupingBy(RegistroCrescimento::getDataPesagem))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal biomassa = e.getValue().stream()
                            .map(RegistroCrescimento::getBiomassaKg)
                            .filter(v -> v != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new TimeSeriesPoint(e.getKey(), biomassa);
                })
                .sorted(Comparator.comparing(TimeSeriesPoint::data))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProducaoPorTanqueResponse> producaoPorTanque(UUID empresaId) {
        List<Tanque> tanques = tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO);

        return tanques.stream().map(t -> {
            List<Lote> lotes = loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())
                    .getContent().stream()
                    .filter(l -> l.getTanque() != null && l.getTanque().getId().equals(t.getId()))
                    .toList();

            long peixes = lotes.stream().mapToLong(l -> l.getQuantidadeAtual() == null ? 0 : l.getQuantidadeAtual()).sum();
            BigDecimal biomassa = lotes.stream()
                    .map(l -> BigDecimal.valueOf(l.getQuantidadeAtual())
                            .multiply(l.getPesoAtualG())
                            .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var saude = indiceSaudeService.calcular(t.getId());
            Integer score = saude.semDadosSuficientes() ? null : saude.score();

            return new ProducaoPorTanqueResponse(t.getNome(), biomassa, peixes, score);
        }).toList();
    }

    private BigDecimal calcularFcrMedio(List<Lote> lotesAtivos) {
        Instant seteDiasAtras = Instant.now().minus(7, ChronoUnit.DAYS);
        BigDecimal racaoTotal = BigDecimal.ZERO;
        BigDecimal ganhoTotal = BigDecimal.ZERO;

        for (Lote lote : lotesAtivos) {
            BigDecimal racao = alimentacaoRepository.findByLoteIdAndHorarioBetween(lote.getId(), seteDiasAtras, Instant.now())
                    .stream().map(r -> r.getQuantidadeKg()).reduce(BigDecimal.ZERO, BigDecimal::add);

            List<RegistroCrescimento> ultimasPesagens = crescimentoRepository.findTop2ByLoteIdOrderByDataPesagemDesc(lote.getId());
            if (racao.compareTo(BigDecimal.ZERO) > 0 && ultimasPesagens.size() == 2) {
                BigDecimal ganho = ultimasPesagens.get(0).getBiomassaKg().subtract(ultimasPesagens.get(1).getBiomassaKg());
                if (ganho.compareTo(BigDecimal.ZERO) > 0) {
                    racaoTotal = racaoTotal.add(racao);
                    ganhoTotal = ganhoTotal.add(ganho);
                }
            }
        }
        return ganhoTotal.compareTo(BigDecimal.ZERO) > 0
                ? racaoTotal.divide(ganhoTotal, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private BigDecimal media(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
}
