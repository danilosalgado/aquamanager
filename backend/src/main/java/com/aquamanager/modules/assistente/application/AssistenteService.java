package com.aquamanager.modules.assistente.application;

import com.aquamanager.modules.financeiro.infrastructure.persistence.LancamentoFinanceiroRepository;
import com.aquamanager.modules.financeiro.domain.TipoLancamento;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistenteService {

    private final GeminiClient geminiClient;
    private final TanqueRepository tanqueRepository;
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
        
        String prompt = "Você é um assistente especialista em aquicultura e piscicultura. "
                + "Você está ajudando um produtor que usa o sistema AquaManager. "
                + "Aqui estão os dados atuais da fazenda dele:\n"
                + contexto + "\n\n"
                + "Com base nisso e no seu conhecimento, responda de forma clara, concisa e amigável à seguinte pergunta do produtor:\n"
                + pergunta;
                
        return geminiClient.generateContent(prompt);
    }

    private String montarContexto(UUID empresaId) {
        StringBuilder sb = new StringBuilder();
        
        List<Tanque> tanques = tanqueRepository.findByEmpresaId(empresaId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        sb.append("- Total de tanques cadastrados: ").append(tanques.size()).append("\n");
        if (!tanques.isEmpty()) {
            sb.append("- Detalhes dos tanques: ").append(
                tanques.stream().map(t -> t.getNome() + " (" + t.getStatus() + ")")
                       .collect(Collectors.joining(", "))
            ).append("\n");
        }

        BigDecimal pendenteReceita = financeiroRepository.somarPendentePorTipo(empresaId, TipoLancamento.RECEITA);
        BigDecimal pendenteDespesa = financeiroRepository.somarPendentePorTipo(empresaId, TipoLancamento.DESPESA);
        
        sb.append("- Financeiro pendente: Receitas a receber R$").append(pendenteReceita)
          .append(", Despesas a pagar R$").append(pendenteDespesa).append("\n");

        return sb.toString();
    }
}
