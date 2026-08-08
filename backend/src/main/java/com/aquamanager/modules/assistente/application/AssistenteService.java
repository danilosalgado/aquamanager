package com.aquamanager.modules.assistente.application;

import com.aquamanager.modules.crescimento.domain.RegistroCrescimento;
import com.aquamanager.modules.crescimento.infrastructure.persistence.RegistroCrescimentoRepository;
import com.aquamanager.modules.dashboard.application.DashboardService;
import com.aquamanager.modules.dashboard.application.dto.DashboardResumoResponse;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.lote.domain.Lote;
import com.aquamanager.modules.lote.domain.StatusLote;
import com.aquamanager.modules.lote.infrastructure.persistence.LoteRepository;
import com.aquamanager.modules.qualidadeagua.domain.RegistroQualidadeAgua;
import com.aquamanager.modules.qualidadeagua.infrastructure.persistence.RegistroQualidadeAguaRepository;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Monta o contexto real da fazenda do tenant (reaproveitando os mesmos agregados do
 * dashboard, mais detalhe por tanque/lote) e injeta no prompt do Gemini — sem isso, o
 * assistente só "chuta" respostas genéricas de conhecimento geral de aquicultura, sem
 * nenhuma noção dos dados reais do produtor.
 */
@Service
@RequiredArgsConstructor
public class AssistenteService {

    private final GeminiClient geminiClient;
    private final DashboardService dashboardService;
    private final TanqueRepository tanqueRepository;
    private final LoteRepository loteRepository;
    private final RegistroCrescimentoRepository crescimentoRepository;
    private final RegistroQualidadeAguaRepository qualidadeAguaRepository;
    private final LancamentoFinanceiroRepository financeiroRepository;
    private final EmpresaRepository empresaRepository;

    public String perguntar(UUID empresaId, String pergunta) {
        var empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        if (!empresa.getPlano().possuiRecurso("ia")) {
            throw new BusinessException("FEATURE_NOT_AVAILABLE",
                    "O assistente de IA está disponível a partir do plano Professional. Faça upgrade para usá-lo.");
        }

        String contexto = montarContexto(empresaId);

        String prompt = "Você é o AquaIA, assistente especialista em aquicultura e piscicultura dentro do "
                + "sistema AquaManager. Responda SEMPRE em português do Brasil, de forma clara, direta e "
                + "amigável, usando Markdown (negrito nos números/nomes importantes, listas com \"-\" quando "
                + "fizer sentido) — o app renderiza Markdown de verdade, então formate à vontade.\n\n"
                + "Use os dados reais abaixo para responder com precisão; não invente números que não estão "
                + "aqui. Se a pergunta pedir algo que não está nos dados, diga isso claramente e, se puder, "
                + "complemente com seu conhecimento geral de piscicultura.\n\n"
                + "=== DADOS ATUAIS DA FAZENDA ===\n"
                + contexto
                + "\n=== FIM DOS DADOS ===\n\n"
                + "Pergunta do produtor: " + pergunta;

        return geminiClient.generateContent(prompt);
    }

    private String montarContexto(UUID empresaId) {
        StringBuilder sb = new StringBuilder();

        DashboardResumoResponse resumo = dashboardService.resumo(empresaId);
        sb.append("Resumo geral:\n")
                .append("- Tanques cadastrados: ").append(resumo.quantidadeTanques()).append("\n")
                .append("- Lotes ativos: ").append(resumo.quantidadeLotesAtivos()).append("\n")
                .append("- Peixes em produção: ").append(resumo.quantidadePeixes()).append("\n")
                .append("- Biomassa total: ").append(resumo.biomassaTotalKg()).append(" kg\n")
                .append("- Peso médio dos peixes: ").append(resumo.pesoMedioG()).append(" g\n")
                .append("- Conversão alimentar média (FCR, últimos 7 dias): ").append(resumo.conversaoAlimentarMedia()).append("\n")
                .append("- Receitas (30 dias): R$ ").append(resumo.receita30Dias()).append("\n")
                .append("- Despesas (30 dias): R$ ").append(resumo.despesa30Dias()).append("\n")
                .append("- Lucro (30 dias): R$ ").append(resumo.lucro30Dias()).append("\n")
                .append("- Mortalidade (30 dias): ").append(resumo.mortalidade30Dias()).append(" peixes\n")
                .append("- Alertas não lidos: ").append(resumo.alertasNaoLidos()).append("\n");
        if (resumo.indiceSaudeMedio() != null) {
            sb.append("- Índice de saúde médio dos tanques: ").append(Math.round(resumo.indiceSaudeMedio())).append("/100\n");
        }

        List<Tanque> tanques = tanqueRepository.findByEmpresaIdAndStatus(empresaId, StatusTanque.ATIVO);
        if (tanques.isEmpty()) {
            sb.append("\nNenhum tanque ativo cadastrado ainda.\n");
            return sb.toString();
        }

        sb.append("\nDetalhe por tanque:\n");
        for (Tanque tanque : tanques) {
            sb.append("* ").append(tanque.getNome()).append(" (código ").append(tanque.getCodigo()).append(")");

            List<Lote> lotesDoTanque = loteRepository.findByTanqueIdAndStatus(tanque.getId(), StatusLote.ATIVO);
            if (lotesDoTanque.isEmpty()) {
                sb.append(" — sem lote ativo no momento.\n");
            } else {
                sb.append(":\n");
                for (Lote lote : lotesDoTanque) {
                    long diasDeVida = ChronoUnit.DAYS.between(lote.getDataCompra(), LocalDate.now());
                    sb.append("  - Lote de ").append(lote.getEspecie().getNome())
                            .append(", povoado em ").append(lote.getDataCompra())
                            .append(" (").append(diasDeVida).append(" dias), ")
                            .append(lote.getQuantidadeAtual()).append(" peixes, peso médio atual ")
                            .append(lote.getPesoAtualG()).append(" g\n");

                    List<RegistroCrescimento> ultimasPesagens = crescimentoRepository.findTop2ByLoteIdOrderByDataPesagemDesc(lote.getId());
                    if (!ultimasPesagens.isEmpty()) {
                        RegistroCrescimento ultima = ultimasPesagens.get(0);
                        sb.append("    Última biometria: ").append(ultima.getDataPesagem())
                                .append(" — ").append(ultima.getPesoMedioG()).append(" g");
                        if (ultimasPesagens.size() == 2) {
                            var ganhoG = ultima.getPesoMedioG().subtract(ultimasPesagens.get(1).getPesoMedioG());
                            sb.append(" (variação de ").append(ganhoG).append(" g desde a pesagem anterior)");
                        }
                        sb.append("\n");
                    }
                }
            }

            qualidadeAguaRepository.findFirstByTanqueIdOrderByMedidoEmDesc(tanque.getId()).ifPresent(ultimaAgua ->
                    sb.append("    Última medição de água (").append(formatarData(ultimaAgua))
                            .append("): temperatura ").append(valorOuIndisponivel(ultimaAgua.getTemperatura())).append(" °C, pH ")
                            .append(valorOuIndisponivel(ultimaAgua.getPh())).append(", oxigênio dissolvido ")
                            .append(valorOuIndisponivel(ultimaAgua.getOxigenioDissolvido())).append(" mg/L\n"));
        }

        var pendenteReceita = financeiroRepository.somarPendentePorTipo(empresaId, TipoLancamento.RECEITA);
        var pendenteDespesa = financeiroRepository.somarPendentePorTipo(empresaId, TipoLancamento.DESPESA);
        sb.append("\nFinanceiro pendente: receitas a receber R$ ").append(pendenteReceita)
                .append(", despesas a pagar R$ ").append(pendenteDespesa).append("\n");

        return sb.toString();
    }

    private static String formatarData(RegistroQualidadeAgua registro) {
        return registro.getMedidoEm().toString();
    }

    private static String valorOuIndisponivel(Object valor) {
        return valor != null ? valor.toString() : "não medido";
    }
}
