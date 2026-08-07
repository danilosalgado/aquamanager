package com.aquamanager.modules.tenant.application;

import com.aquamanager.config.TenantProperties;
import com.aquamanager.modules.tenant.application.dto.AtualizarEmpresaRequest;
import com.aquamanager.modules.tenant.domain.Assinatura;
import com.aquamanager.modules.tenant.domain.AssinaturaStatus;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.domain.Plano;
import com.aquamanager.modules.tenant.domain.PlanoCodigo;
import com.aquamanager.modules.tenant.infrastructure.persistence.AssinaturaRepository;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.modules.tenant.infrastructure.persistence.PlanoRepository;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ConflictException;
import com.aquamanager.shared.domain.exception.PlanLimitExceededException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final TenantSessionManager tenantSessionManager;
    private final TenantProperties tenantProperties;

    @Override
    @Transactional
    public Empresa criarComTrial(String nome, String documento, String endereco, String cidade, String estado,
                                  String telefone, String email) {
        if (empresaRepository.existsByDocumento(documento)) {
            throw new ConflictException("Já existe uma empresa cadastrada com este CPF/CNPJ.");
        }

        // A plataforma opera com um único plano comercial (ver V23__plano_unico_stripe.sql);
        // o código PROFESSIONAL foi mantido internamente para minimizar o diff, mas hoje é o
        // único plano cadastrado.
        Plano plano = planoRepository.findByCodigo(PlanoCodigo.PROFESSIONAL)
                .orElseThrow(() -> new IllegalStateException("Plano padrão não encontrado — seeds do Flyway ausentes."));

        Empresa empresa = new Empresa();
        empresa.setNome(nome);
        empresa.setDocumento(documento);
        empresa.setEndereco(endereco);
        empresa.setCidade(cidade);
        empresa.setEstado(estado);
        empresa.setTelefone(telefone);
        empresa.setEmail(email);
        empresa.setPlano(plano);
        empresa.setStatus(EmpresaStatus.TRIAL);
        empresa.setTrialEndsAt(Instant.now().plus(tenantProperties.trialDays(), ChronoUnit.DAYS));
        empresa = empresaRepository.save(empresa);

        // A partir daqui a empresa já existe: ativamos o contexto de tenant para que
        // o restante desta transação (assinatura, e futuramente o primeiro usuário)
        // seja corretamente isolado pelo filtro Hibernate + RLS.
        TenantContext.setTenantId(empresa.getId());
        tenantSessionManager.activate(empresa.getId());

        Assinatura assinatura = new Assinatura();
        assinatura.setEmpresaId(empresa.getId());
        assinatura.setPlano(plano);
        assinatura.setStatus(AssinaturaStatus.TRIAL);
        assinatura.setDataInicio(LocalDate.now());
        assinaturaRepository.save(assinatura);

        return empresa;
    }

    @Override
    @Transactional(readOnly = true)
    public Empresa buscarPorId(UUID empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
    }

    @Override
    @Transactional
    public Empresa atualizar(UUID empresaId, AtualizarEmpresaRequest request) {
        Empresa empresa = buscarPorId(empresaId);
        empresa.setNome(request.nome());
        empresa.setEndereco(request.endereco());
        empresa.setCidade(request.cidade());
        empresa.setEstado(request.estado());
        empresa.setTelefone(request.telefone());
        empresa.setEmail(request.email());
        return empresa;
    }

    @Override
    @Transactional(readOnly = true)
    public void garantirLimiteTanques(UUID empresaId, long quantidadeAtual) {
        Empresa empresa = buscarPorId(empresaId);
        Plano plano = empresa.getPlano();
        if (!plano.tanquesIlimitados() && quantidadeAtual >= plano.getLimiteTanques()) {
            throw new PlanLimitExceededException(
                    "Limite de %d tanques do plano %s atingido. Faça upgrade para continuar."
                            .formatted(plano.getLimiteTanques(), plano.getNome()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void garantirLimiteUsuarios(UUID empresaId, long quantidadeAtual) {
        Empresa empresa = buscarPorId(empresaId);
        Plano plano = empresa.getPlano();
        if (!plano.usuariosIlimitados() && quantidadeAtual >= plano.getLimiteUsuarios()) {
            throw new PlanLimitExceededException(
                    "Limite de %d usuários do plano %s atingido. Faça upgrade para continuar."
                            .formatted(plano.getLimiteUsuarios(), plano.getNome()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void garantirAcessoLiberado(UUID empresaId) {
        Empresa empresa = buscarPorId(empresaId);
        if (empresa.getStatus().acessoBloqueado()) {
            throw new BusinessException("SUBSCRIPTION_BLOCKED",
                    "O acesso da sua empresa está bloqueado (status: %s). Regularize a assinatura para continuar."
                            .formatted(empresa.getStatus()));
        }
    }
}
