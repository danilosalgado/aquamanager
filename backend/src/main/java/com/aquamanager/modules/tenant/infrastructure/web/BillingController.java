package com.aquamanager.modules.tenant.infrastructure.web;

import com.aquamanager.modules.tenant.application.AssinaturaService;
import com.aquamanager.modules.tenant.application.dto.AlterarPlanoRequest;
import com.aquamanager.modules.tenant.application.dto.AssinaturaResponse;
import com.aquamanager.modules.tenant.application.dto.CheckoutSessionResponse;
import com.aquamanager.modules.tenant.application.dto.PortalSessionResponse;
import com.aquamanager.modules.tenant.infrastructure.mapper.AssinaturaMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final AssinaturaService assinaturaService;
    private final AssinaturaMapper assinaturaMapper;

    @GetMapping("/assinatura")
    public ApiResponse<AssinaturaResponse> assinaturaAtual() {
        var assinatura = assinaturaService.obterAtual(SecurityUtils.currentEmpresaId());
        return ApiResponse.of(assinaturaMapper.toResponse(assinatura));
    }

    @PostMapping("/plano")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<AssinaturaResponse> alterarPlano(@Valid @RequestBody AlterarPlanoRequest request) {
        var assinatura = assinaturaService.alterarPlano(SecurityUtils.currentEmpresaId(), request.codigoPlano());
        return ApiResponse.of(assinaturaMapper.toResponse(assinatura));
    }

    @DeleteMapping("/assinatura")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void cancelar() {
        assinaturaService.cancelar(SecurityUtils.currentEmpresaId());
    }

    @PostMapping("/checkout-session")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<CheckoutSessionResponse> criarCheckoutSession() {
        var resultado = assinaturaService.criarCheckoutSession(SecurityUtils.currentEmpresaId());
        return ApiResponse.of(new CheckoutSessionResponse(resultado.checkoutUrl()));
    }

    @PostMapping("/portal-session")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<PortalSessionResponse> criarPortalSession() {
        var url = assinaturaService.criarPortalSession(SecurityUtils.currentEmpresaId());
        return ApiResponse.of(new PortalSessionResponse(url));
    }
}
