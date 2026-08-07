package com.aquamanager.modules.tanque;

import static org.assertj.core.api.Assertions.assertThat;

import com.aquamanager.AbstractIntegrationTest;
import com.aquamanager.modules.tanque.domain.StatusTanque;
import com.aquamanager.modules.tanque.domain.Tanque;
import com.aquamanager.modules.tanque.domain.TipoTanque;
import com.aquamanager.modules.tanque.infrastructure.persistence.TanqueRepository;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.modules.tenant.domain.EmpresaStatus;
import com.aquamanager.modules.tenant.domain.PlanoCodigo;
import com.aquamanager.modules.tenant.infrastructure.persistence.EmpresaRepository;
import com.aquamanager.modules.tenant.infrastructure.persistence.PlanoRepository;
import com.aquamanager.shared.infrastructure.persistence.TenantContext;
import com.aquamanager.shared.infrastructure.persistence.TenantSessionManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prova, no nível mais baixo possível (repositório, sem nenhuma checagem explícita de
 * empresa_id no código de aplicação), que o isolamento multi-tenant impede uma empresa
 * de enxergar dados de outra — mesmo com uma consulta totalmente irrestrita como
 * {@code findAll()}.
 *
 * Nota: o usuário de conexão padrão do Testcontainers ("test") é bootstrap/superuser do
 * cluster Postgres, e superusers sempre ignoram Row-Level Security (mesmo com FORCE RLS)
 * — por isso este teste, como está, valida efetivamente a primeira camada de defesa (o
 * filtro Hibernate). A segunda camada (RLS) é validada manualmente em ambiente com um
 * role de aplicação não-superuser (ver docs/ARCHITECTURE.md); reproduzir isso aqui exigiria
 * provisionar um role restrito dentro do container de teste.
 */
class TenantIsolationIT extends AbstractIntegrationTest {

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private PlanoRepository planoRepository;
    @Autowired
    private TanqueRepository tanqueRepository;
    @Autowired
    private TenantSessionManager tenantSessionManager;

    @AfterEach
    void limparContexto() {
        TenantContext.clear();
    }

    @Test
    @Transactional
    void tanqueDeUmaEmpresaNaoDeveSerVisivelParaOutra() {
        Empresa empresaA = criarEmpresa("Piscicultura A");
        Empresa empresaB = criarEmpresa("Piscicultura B");

        ativarTenant(empresaA.getId());
        Tanque tanqueA = new Tanque();
        tanqueA.setNome("Tanque 1");
        tanqueA.setCodigo("T1");
        tanqueA.setTipo(TipoTanque.ESCAVADO);
        tanqueA.setStatus(StatusTanque.ATIVO);
        tanqueRepository.save(tanqueA);
        assertThat(tanqueA.getEmpresaId()).isEqualTo(empresaA.getId());

        ativarTenant(empresaB.getId());
        assertThat(tanqueRepository.findAll()).isEmpty();

        ativarTenant(empresaA.getId());
        assertThat(tanqueRepository.findAll()).hasSize(1);
    }

    private void ativarTenant(UUID empresaId) {
        TenantContext.setTenantId(empresaId);
        tenantSessionManager.activate(empresaId);
    }

    private Empresa criarEmpresa(String nome) {
        var plano = planoRepository.findByCodigo(PlanoCodigo.STARTER).orElseThrow();
        Empresa empresa = new Empresa();
        empresa.setNome(nome);
        empresa.setDocumento(UUID.randomUUID().toString().substring(0, 14));
        empresa.setEmail(nome.toLowerCase().replace(" ", ".") + "@teste.com.br");
        empresa.setPlano(plano);
        empresa.setStatus(EmpresaStatus.TRIAL);
        empresa.setTrialEndsAt(Instant.now().plus(14, ChronoUnit.DAYS));
        return empresaRepository.save(empresa);
    }
}
