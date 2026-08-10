package com.aquamanager.modules.crescimento.application;

import com.aquamanager.modules.crescimento.application.dto.CrescimentoPotencialResponse;
import com.aquamanager.modules.crescimento.application.dto.CrescimentoPotencialResponse.ProjecaoPeso;
import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.lote.application.LoteService;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Estima quanto tempo falta para um lote atingir determinados pesos-alvo, a partir da
 * taxa de ganho de peso observada no histórico real de pesagens (regressão linear
 * simples peso x dias) — não é um valor tabelado por espécie, é calculado em cima do
 * que o produtor efetivamente mediu naquele lote.
 */
@Service
@RequiredArgsConstructor
public class CrescimentoPotencialServiceImpl implements CrescimentoPotencialService {

    private static final BigDecimal[] METAS_PADRAO_G = {
            BigDecimal.valueOf(800), BigDecimal.valueOf(900), BigDecimal.valueOf(1000),
    };

    private final LoteService loteService;
    private final LoteRepository loteRepository;
    private final RegistroCrescimentoRepository registroCrescimentoRepository;

    @Override
    @Transactional(readOnly = true)
    public CrescimentoPotencialResponse calcular(UUID empresaId, UUID loteId) {
        Lote lote = loteService.buscar(empresaId, loteId);
        return calcularParaLote(lote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrescimentoPotencialResponse> calcularTodosAtivos(UUID empresaId) {
        return loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged()).stream()
                .map(this::calcularParaLote)
                .toList();
    }

    private CrescimentoPotencialResponse calcularParaLote(Lote lote) {
        List<RegistroCrescimento> historico = registroCrescimentoRepository.findByLoteIdOrderByDataPesagemAsc(lote.getId());

        TaxaCrescimento taxa = calcularTaxa(lote, historico);

        // stripTrailingZeros() normaliza a escala antes de comparar — sem isso, 800 (dos
        // padrões) e 800.00 (vindo do banco, em Especie.pesoAbatePadraoG) são != para
        // equals()/hashCode() mesmo sendo o mesmo valor, e o Set não deduplica.
        Set<BigDecimal> metas = new LinkedHashSet<>();
        for (BigDecimal padrao : METAS_PADRAO_G) {
            metas.add(padrao.stripTrailingZeros());
        }
        BigDecimal pesoAbatePadrao = lote.getEspecie().getPesoAbatePadraoG();
        if (pesoAbatePadrao != null && pesoAbatePadrao.signum() > 0) {
            metas.add(pesoAbatePadrao.stripTrailingZeros());
        }

        List<ProjecaoPeso> projecoes = new ArrayList<>();
        for (BigDecimal metaG : metas) {
            projecoes.add(projetar(metaG, lote.getPesoAtualG(), taxa.gramasPorDia()));
        }
        projecoes.sort((a, b) -> a.pesoAlvoG().compareTo(b.pesoAlvoG()));

        return new CrescimentoPotencialResponse(
                lote.getId(),
                lote.getTanque().getNome(),
                lote.getEspecie().getNome(),
                lote.getPesoAtualG(),
                taxa.gramasPorDia(),
                taxa.confiabilidade(),
                taxa.pesagensConsideradas(),
                projecoes
        );
    }

    private ProjecaoPeso projetar(BigDecimal metaG, BigDecimal pesoAtualG, BigDecimal taxaGDia) {
        String rotulo = formatarRotulo(metaG);

        if (pesoAtualG.compareTo(metaG) >= 0) {
            return new ProjecaoPeso(rotulo, metaG, true, 0, LocalDate.now());
        }
        if (taxaGDia == null || taxaGDia.signum() <= 0) {
            return new ProjecaoPeso(rotulo, metaG, false, null, null);
        }

        BigDecimal faltaG = metaG.subtract(pesoAtualG);
        int dias = faltaG.divide(taxaGDia, MathContext.DECIMAL64)
                .setScale(0, RoundingMode.CEILING)
                .intValueExact();
        return new ProjecaoPeso(rotulo, metaG, false, dias, LocalDate.now().plusDays(dias));
    }

    private static String formatarRotulo(BigDecimal metaG) {
        BigDecimal kg = metaG.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP).stripTrailingZeros();
        return kg.toPlainString().replace('.', ',') + " kg";
    }

    private record TaxaCrescimento(BigDecimal gramasPorDia, String confiabilidade, int pesagensConsideradas) {
    }

    /**
     * Regressão linear simples (mínimos quadrados) de peso médio x dias desde a primeira
     * pesagem — suaviza ruído de medição melhor do que só olhar as duas últimas pesagens.
     * Sem histórico suficiente, cai para o ganho médio desde a compra do lote.
     */
    private TaxaCrescimento calcularTaxa(Lote lote, List<RegistroCrescimento> historico) {
        if (historico.size() >= 2) {
            LocalDate inicio = historico.get(0).getDataPesagem();
            long n = historico.size();
            double sx = 0, sy = 0, sxy = 0, sxx = 0;
            for (RegistroCrescimento r : historico) {
                double x = ChronoUnit.DAYS.between(inicio, r.getDataPesagem());
                double y = r.getPesoMedioG().doubleValue();
                sx += x;
                sy += y;
                sxy += x * y;
                sxx += x * x;
            }
            double denominador = n * sxx - sx * sx;
            if (denominador != 0) {
                double slope = (n * sxy - sx * sy) / denominador;
                long amplitudeDias = ChronoUnit.DAYS.between(inicio, historico.get(historico.size() - 1).getDataPesagem());
                String confiabilidade = (n >= 3 && amplitudeDias >= 14) ? "ALTA" : "MEDIA";
                return new TaxaCrescimento(
                        BigDecimal.valueOf(slope).setScale(2, RoundingMode.HALF_UP),
                        confiabilidade,
                        historico.size()
                );
            }
        }

        // Fallback: ganho médio desde a compra do lote (0, 1 pesagem, ou pesagens no mesmo dia).
        long diasDesdeCompra = ChronoUnit.DAYS.between(lote.getDataCompra(), LocalDate.now());
        if (diasDesdeCompra <= 0) {
            return new TaxaCrescimento(null, "BAIXA", historico.size());
        }
        BigDecimal ganhoTotal = lote.getPesoAtualG().subtract(lote.getPesoInicialG());
        BigDecimal taxa = ganhoTotal.divide(BigDecimal.valueOf(diasDesdeCompra), 2, RoundingMode.HALF_UP);
        return new TaxaCrescimento(taxa, "BAIXA", historico.size());
    }
}
