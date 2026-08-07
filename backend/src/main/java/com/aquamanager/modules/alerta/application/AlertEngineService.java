package com.aquamanager.modules.alerta.application;

import com.aquamanager.modules.alerta.domain.Alerta;
import com.aquamanager.modules.alerta.domain.Severidade;
import com.aquamanager.modules.alerta.domain.TipoAlerta;
import com.aquamanager.modules.alerta.infrastructure.persistence.AlertaRepository;
import com.aquamanager.modules.alimentacao.infrastructure.persistence.RegistroAlimentacaoRepository;
import com.aquamanager.modules.especie.domain.Especie;
import com.aquamanager.modules.estoque.domain.EstoqueItem;
import com.aquamanager.modules.estoque.infrastructure.persistence.EstoqueItemRepository;
import com.aquamanager.modules.financeiro.domain.LancamentoFinanceiro;
import com.aquamanager.modules.financeiro.domain.StatusLancamento;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.mortalidade.domain.RegistroMortalidade;
import com.aquamanager.modules.mortalidade.infrastructure.persistence.RegistroMortalidadeRepository;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Motor de regras de alertas. Cada empresa é processada em sua própria transação
 * (tenant corretamente ativado via TenantContext pelo chamador — ver AlertScanScheduler),
 * evitando que uma falha isolada derrube a varredura inteira.
 */
@Service
@RequiredArgsConstructor
public class AlertEngineService {

    private static final Duration COOLDOWN = Duration.ofHours(24);
    private static final BigDecimal LIMITE_MORTALIDADE_PERCENTUAL = BigDecimal.valueOf(0.10);

    private final EmpresaRepository empresaRepository;
    private final TanqueRepository tanqueRepository;
    private final LoteRepository loteRepository;
    private final RegistroQualidadeAguaRepository qualidadeAguaRepository;
    private final RegistroMortalidadeRepository mortalidadeRepository;
    private final RegistroAlimentacaoRepository alimentacaoRepository;
    private final EstoqueItemRepository estoqueItemRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AlertaRepository alertaRepository;

    @Transactional
    public void processarEmpresa(UUID empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) {
            return;
        }
        avaliarQualidadeAgua(empresaId);
        avaliarEstoque(empresaId);
        avaliarMortalidade(empresaId);
        avaliarAlimentacao(empresaId);
        avaliarFinanceiro(empresaId);
        avaliarAssinatura(empresa);
    }

    private void avaliarQualidadeAgua(UUID empresaId) {
        for (Tanque tanque : tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO)) {
            RegistroQualidadeAgua ultima = qualidadeAguaRepository
                    .findFirstByTanqueIdOrderByMedidoEmDesc(tanque.getId()).orElse(null);
            if (ultima == null) {
                continue;
            }
            List<Lote> lotesAtivos = loteRepository.findByTanqueIdAndStatus(tanque.getId(), StatusLote.ATIVO);
            if (lotesAtivos.isEmpty()) {
                continue;
            }
            Especie especie = lotesAtivos.get(0).getEspecie();

            StringBuilder problemas = new StringBuilder();
            boolean critico = false;

            if (foraDaFaixa(ultima.getTemperatura(), especie.getTempMin(), especie.getTempMax())) {
                problemas.append("temperatura ").append(ultima.getTemperatura()).append("°C fora da faixa ideal; ");
            }
            if (foraDaFaixa(ultima.getPh(), especie.getPhMin(), especie.getPhMax())) {
                problemas.append("pH ").append(ultima.getPh()).append(" fora da faixa ideal; ");
            }
            if (ultima.getOxigenioDissolvido() != null && especie.getOxigenioMin() != null
                    && ultima.getOxigenioDissolvido().compareTo(especie.getOxigenioMin()) < 0) {
                problemas.append("oxigênio dissolvido ").append(ultima.getOxigenioDissolvido()).append(" abaixo do mínimo; ");
                critico = true;
            }
            if (ultima.getAmonia() != null && especie.getAmoniaMax() != null
                    && ultima.getAmonia().compareTo(especie.getAmoniaMax()) > 0) {
                problemas.append("amônia ").append(ultima.getAmonia()).append(" acima do máximo; ");
            }
            if (ultima.getNitrito() != null && especie.getNitritoMax() != null
                    && ultima.getNitrito().compareTo(especie.getNitritoMax()) > 0) {
                problemas.append("nitrito ").append(ultima.getNitrito()).append(" acima do máximo; ");
            }

            if (!problemas.isEmpty()) {
                criarAlertaSeNovo(empresaId, TipoAlerta.QUALIDADE_AGUA_FORA_DA_FAIXA, tanque.getId(), "TANQUE",
                        critico ? Severidade.CRITICO : Severidade.ATENCAO,
                        "Água fora da faixa ideal — " + tanque.getNome(),
                        "Parâmetros fora da faixa ideal para a espécie cultivada: " + problemas);
            }
        }
    }

    private void avaliarEstoque(UUID empresaId) {
        for (EstoqueItem item : estoqueItemRepository.findAbaixoDoMinimo(empresaId)) {
            criarAlertaSeNovo(empresaId, TipoAlerta.ESTOQUE_BAIXO, item.getId(), "ESTOQUE_ITEM", Severidade.ATENCAO,
                    "Estoque baixo — " + item.getNome(),
                    "Quantidade atual (%s %s) está no ou abaixo do mínimo definido (%s %s)."
                            .formatted(item.getQuantidadeAtual(), item.getUnidade(), item.getQuantidadeMinima(), item.getUnidade()));
        }
    }

    private void avaliarMortalidade(UUID empresaId) {
        for (Lote lote : loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())) {
            List<RegistroMortalidade> recentes = mortalidadeRepository
                    .findByLoteIdAndDataGreaterThanEqual(lote.getId(), LocalDate.now().minusDays(7));
            int totalMorto = recentes.stream().mapToInt(RegistroMortalidade::getQuantidade).sum();
            if (totalMorto == 0 || lote.getQuantidadeAtual() == 0) {
                continue;
            }
            BigDecimal percentual = BigDecimal.valueOf(totalMorto)
                    .divide(BigDecimal.valueOf(lote.getQuantidadeAtual() + totalMorto), 4, java.math.RoundingMode.HALF_UP);
            if (percentual.compareTo(LIMITE_MORTALIDADE_PERCENTUAL) >= 0) {
                criarAlertaSeNovo(empresaId, TipoAlerta.MORTALIDADE_ELEVADA, lote.getId(), "LOTE", Severidade.CRITICO,
                        "Mortalidade elevada — " + lote.getTanque().getNome(),
                        "%d peixes perdidos nos últimos 7 dias (%.1f%% do lote).".formatted(totalMorto, percentual.doubleValue() * 100));
            }
        }
    }

    private void avaliarAlimentacao(UUID empresaId) {
        Instant doisDiasAtras = Instant.now().minus(2, ChronoUnit.DAYS);
        for (Lote lote : loteRepository.findByEmpresaIdAndStatus(empresaId, StatusLote.ATIVO, Pageable.unpaged())) {
            List<?> recentes = alimentacaoRepository.findByLoteIdAndHorarioBetween(lote.getId(), doisDiasAtras, Instant.now());
            if (recentes.isEmpty()) {
                criarAlertaSeNovo(empresaId, TipoAlerta.ALIMENTACAO_INSUFICIENTE, lote.getId(), "LOTE", Severidade.ATENCAO,
                        "Alimentação insuficiente — " + lote.getTanque().getNome(),
                        "Nenhum registro de alimentação nas últimas 48 horas para este lote.");
            }
        }
    }

    private void avaliarFinanceiro(UUID empresaId) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        BigDecimal receita = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.RECEITA, inicioMes, hoje);
        BigDecimal despesa = lancamentoRepository.somarPagoPorTipoEPeriodo(empresaId, TipoLancamento.DESPESA, inicioMes, hoje);
        if (despesa.compareTo(BigDecimal.ZERO) > 0 && despesa.compareTo(receita) > 0) {
            criarAlertaSeNovo(empresaId, TipoAlerta.DESPESA_ACIMA_RECEITA, empresaId, "EMPRESA", Severidade.ATENCAO,
                    "Despesas acima da receita este mês",
                    "Despesas pagas (%s) superam as receitas pagas (%s) no mês corrente.".formatted(despesa, receita));
        }

        for (LancamentoFinanceiro lancamento : lancamentoRepository
                .findByEmpresaIdAndDataVencimentoBetween(empresaId, hoje, hoje.plusDays(3))) {
            if (lancamento.getStatus() == StatusLancamento.PENDENTE) {
                criarAlertaSeNovo(empresaId, TipoAlerta.PAGAMENTO_PROXIMO, lancamento.getId(), "LANCAMENTO_FINANCEIRO",
                        Severidade.INFO, "Pagamento próximo do vencimento",
                        "\"%s\" (%s) vence em %s.".formatted(lancamento.getDescricao(), lancamento.getValor(), lancamento.getDataVencimento()));
            }
        }
    }

    private void avaliarAssinatura(Empresa empresa) {
        if (empresa.getStatus() == EmpresaStatus.TRIAL) {
            long diasRestantes = Duration.between(Instant.now(), empresa.getTrialEndsAt()).toDays();
            if (diasRestantes <= 3) {
                criarAlertaSeNovo(empresa.getId(), TipoAlerta.ASSINATURA_VENCENDO, empresa.getId(), "EMPRESA",
                        Severidade.ATENCAO, "Seu período de teste está acabando",
                        diasRestantes >= 0
                                ? "Faltam %d dia(s) para o fim do seu trial gratuito. Escolha um plano para não perder o acesso."
                                        .formatted(diasRestantes)
                                : "Seu período de teste expirou. Escolha um plano para reativar o acesso.");
            }
        }
    }

    private boolean foraDaFaixa(BigDecimal valor, BigDecimal min, BigDecimal max) {
        if (valor == null) {
            return false;
        }
        if (min != null && valor.compareTo(min) < 0) {
            return true;
        }
        return max != null && valor.compareTo(max) > 0;
    }

    private void criarAlertaSeNovo(UUID empresaId, TipoAlerta tipo, UUID entidadeId, String entidadeTipo,
                                    Severidade severidade, String titulo, String mensagem) {
        Instant desde = Instant.now().minus(COOLDOWN);
        boolean jaExiste = alertaRepository
                .existsByEmpresaIdAndTipoAndEntidadeIdAndCreatedAtAfter(empresaId, tipo, entidadeId, desde);
        if (jaExiste) {
            return;
        }

        Alerta alerta = new Alerta();
        alerta.setEmpresaId(empresaId);
        alerta.setTipo(tipo);
        alerta.setSeveridade(severidade);
        alerta.setTitulo(titulo);
        alerta.setMensagem(mensagem);
        alerta.setEntidadeTipo(entidadeTipo);
        alerta.setEntidadeId(entidadeId);
        alertaRepository.save(alerta);
    }
}
