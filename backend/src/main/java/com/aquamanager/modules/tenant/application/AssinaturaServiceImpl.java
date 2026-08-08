package com.aquamanager.modules.tenant.application;

import com.aquamanager.config.FrontendProperties;
import com.aquamanager.modules.tenant.application.port.PaymentGateway;
import com.aquamanager.modules.tenant.application.port.PaymentGateway.CheckoutSessionResult;
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
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssinaturaServiceImpl implements AssinaturaService {

    private static final Logger log = LoggerFactory.getLogger(AssinaturaServiceImpl.class);

    private final AssinaturaRepository assinaturaRepository;
    private final EmpresaRepository empresaRepository;
    private final PlanoRepository planoRepository;
    private final PaymentGateway paymentGateway;
    private final FrontendProperties frontendProperties;

    @Override
    @Transactional(readOnly = true)
    public Assinatura obterAtual(UUID empresaId) {
        return assinaturaRepository.findFirstByEmpresaIdOrderByCreatedAtDesc(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura da empresa", empresaId));
    }

    @Override
    @Transactional
    public Assinatura alterarPlano(UUID empresaId, String codigoPlano) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        if (empresa.isIsentoCobranca()) {
            throw new BusinessException("ADMIN_EXEMPT", "Contas administrativas da plataforma não geram cobrança.");
        }

        PlanoCodigo codigo;
        try {
            codigo = PlanoCodigo.valueOf(codigoPlano);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_PLAN", "Plano inválido: " + codigoPlano);
        }
        Plano novoPlano = planoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Plano", codigoPlano));

        Assinatura atual = assinaturaRepository.findFirstByEmpresaIdOrderByCreatedAtDesc(empresaId).orElse(null);
        String customerId = atual != null && atual.getGatewayCustomerId() != null
                ? atual.getGatewayCustomerId()
                : paymentGateway.criarCliente(empresa).customerId();

        var resultado = paymentGateway.criarAssinatura(empresa, customerId, novoPlano);

        Assinatura nova = new Assinatura();
        nova.setEmpresaId(empresaId);
        nova.setPlano(novoPlano);
        nova.setStatus(AssinaturaStatus.ATIVA);
        nova.setGatewayCustomerId(customerId);
        nova.setGatewaySubscriptionId(resultado.subscriptionId());
        nova.setDataInicio(LocalDate.now());
        nova.setProximoVencimento(resultado.proximoVencimento());
        assinaturaRepository.save(nova);

        empresa.setPlano(novoPlano);
        empresa.setStatus(EmpresaStatus.ATIVA);
        empresaRepository.save(empresa);

        return nova;
    }

    @Override
    @Transactional
    public void cancelar(UUID empresaId) {
        Assinatura atual = obterAtual(empresaId);
        if (atual.getGatewaySubscriptionId() != null) {
            paymentGateway.cancelarAssinatura(atual.getGatewaySubscriptionId());
        }
        atual.setStatus(AssinaturaStatus.CANCELADA);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        empresa.setStatus(EmpresaStatus.CANCELADA);
    }

    @Override
    @Transactional
    public void processarEventoPagamento(String gatewaySubscriptionId, String evento) {
        Assinatura assinatura = assinaturaRepository.findByGatewaySubscriptionId(gatewaySubscriptionId).orElse(null);
        if (assinatura == null) {
            log.warn("Webhook recebido para assinatura desconhecida: {}", gatewaySubscriptionId);
            return;
        }

        Empresa empresa = empresaRepository.findById(assinatura.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", assinatura.getEmpresaId()));

        switch (evento) {
            case "PAYMENT_CONFIRMED", "PAYMENT_RECEIVED" -> {
                assinatura.setStatus(AssinaturaStatus.ATIVA);
                assinatura.setProximoVencimento(LocalDate.now().plusMonths(1));
                empresa.setStatus(EmpresaStatus.ATIVA);
            }
            case "PAYMENT_OVERDUE" -> {
                assinatura.setStatus(AssinaturaStatus.INADIMPLENTE);
                // Uma assinatura PENDENTE que nunca chegou a ser paga (checkout abandonado)
                // não pode derrubar uma empresa que ainda está em TRIAL — só demove empresas
                // que já estavam com acesso pago ativo.
                if (empresa.getStatus() == EmpresaStatus.ATIVA) {
                    empresa.setStatus(EmpresaStatus.INADIMPLENTE);
                }
            }
            case "SUBSCRIPTION_DELETED", "PAYMENT_DELETED" -> {
                assinatura.setStatus(AssinaturaStatus.CANCELADA);
                if (empresa.getStatus() == EmpresaStatus.ATIVA) {
                    empresa.setStatus(EmpresaStatus.CANCELADA);
                }
            }
            default -> log.info("Evento de webhook ignorado: {}", evento);
        }
    }

    @Override
    @Transactional
    public CheckoutSessionResult criarCheckoutSession(UUID empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        if (empresa.isIsentoCobranca()) {
            throw new BusinessException("ADMIN_EXEMPT", "Contas administrativas da plataforma não geram cobrança.");
        }
        Plano plano = planoRepository.findByCodigo(PlanoCodigo.PROFESSIONAL)
                .orElseThrow(() -> new IllegalStateException("Plano padrão não encontrado — seeds do Flyway ausentes."));

        Assinatura atual = assinaturaRepository.findFirstByEmpresaIdOrderByCreatedAtDesc(empresaId).orElse(null);
        String customerId = atual != null ? atual.getGatewayCustomerId() : null;

        String successUrl = frontendProperties.baseUrl() + "/configuracoes?checkout=success";
        String cancelUrl = frontendProperties.baseUrl() + "/configuracoes?checkout=cancel";

        try {
            CheckoutSessionResult resultado = paymentGateway.criarCheckoutSession(empresa, customerId, plano, successUrl, cancelUrl);
            if (resultado.customerId() != null) {
                registrarAssinaturaPendente(empresaId, atual, plano, resultado);
            }
            // customerId nulo (Stripe): a assinatura só é criada quando o webhook
            // checkout.session.completed confirmar — nada a persistir aqui ainda.
            return resultado;
        } catch (UnsupportedOperationException ex) {
            // Gateway sem checkout hospedado nem fatura própria (Mock): ativa a assinatura
            // de forma síncrona, igual ao fluxo legado de alterarPlano.
            alterarPlano(empresaId, plano.getCodigo().name());
            return new CheckoutSessionResult("sync-" + UUID.randomUUID(), successUrl, null);
        }
    }

    /**
     * Asaas já cria a assinatura no gateway (com uma fatura em aberto) ao gerar o link de
     * checkout — a assinatura fica PENDENTE no nosso banco até o webhook confirmar o
     * pagamento; até lá a empresa não ganha acesso.
     */
    private void registrarAssinaturaPendente(UUID empresaId, Assinatura atual, Plano plano, CheckoutSessionResult resultado) {
        if (atual != null && atual.getStatus() == AssinaturaStatus.PENDENTE && atual.getGatewaySubscriptionId() != null) {
            // Checkout reaberto sem pagar o anterior: cancela a fatura anterior no gateway
            // pra não acumular assinaturas/cobranças duplicadas.
            paymentGateway.cancelarAssinatura(atual.getGatewaySubscriptionId());
        }

        Assinatura pendente = new Assinatura();
        pendente.setEmpresaId(empresaId);
        pendente.setPlano(plano);
        pendente.setStatus(AssinaturaStatus.PENDENTE);
        pendente.setGatewayCustomerId(resultado.customerId());
        pendente.setGatewaySubscriptionId(resultado.sessionId());
        pendente.setDataInicio(LocalDate.now());
        assinaturaRepository.save(pendente);
    }

    @Override
    @Transactional
    public String criarPortalSession(UUID empresaId) {
        Assinatura atual = obterAtual(empresaId);
        if (atual.getGatewayCustomerId() == null) {
            throw new BusinessException("NO_GATEWAY_CUSTOMER",
                    "Nenhum cliente de pagamento encontrado para esta empresa ainda.");
        }
        String returnUrl = frontendProperties.baseUrl() + "/configuracoes";
        try {
            return paymentGateway.criarPortalSession(atual.getGatewayCustomerId(), returnUrl);
        } catch (UnsupportedOperationException ex) {
            throw new BusinessException("PORTAL_NOT_SUPPORTED",
                    "Este gateway de pagamento não possui portal de autoatendimento. Use o botão \"Cancelar assinatura\".");
        }
    }

    @Override
    @Transactional
    public void ativarAssinaturaViaCheckout(UUID empresaId, String customerId, String subscriptionId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", empresaId));
        Plano plano = planoRepository.findByCodigo(PlanoCodigo.PROFESSIONAL)
                .orElseThrow(() -> new IllegalStateException("Plano padrão não encontrado — seeds do Flyway ausentes."));

        Assinatura existente = assinaturaRepository.findByGatewaySubscriptionId(subscriptionId).orElse(null);
        if (existente != null) {
            log.info("Checkout já processado anteriormente para a assinatura {}", subscriptionId);
            return;
        }

        Assinatura nova = new Assinatura();
        nova.setEmpresaId(empresaId);
        nova.setPlano(plano);
        nova.setStatus(AssinaturaStatus.ATIVA);
        nova.setGatewayCustomerId(customerId);
        nova.setGatewaySubscriptionId(subscriptionId);
        nova.setDataInicio(LocalDate.now());
        nova.setProximoVencimento(LocalDate.now().plusMonths(1));
        assinaturaRepository.save(nova);

        empresa.setPlano(plano);
        empresa.setStatus(EmpresaStatus.ATIVA);
        empresaRepository.save(empresa);
    }
}
