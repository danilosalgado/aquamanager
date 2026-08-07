package com.aquamanager.modules.auth.infrastructure.web;

import com.aquamanager.modules.auth.application.AuthService;
import com.aquamanager.modules.auth.application.AuthenticationResult;
import com.aquamanager.modules.auth.application.dto.ChangePasswordRequest;
import com.aquamanager.modules.auth.application.dto.Codigo2FaRequest;
import com.aquamanager.modules.auth.application.dto.ForgotPasswordRequest;
import com.aquamanager.modules.auth.application.dto.LoginRequest;
import com.aquamanager.modules.auth.application.dto.LoginResponse;
import com.aquamanager.modules.auth.application.dto.RegisterRequest;
import com.aquamanager.modules.auth.application.dto.ResetPasswordRequest;
import com.aquamanager.modules.auth.application.dto.Setup2FaResponse;
import com.aquamanager.modules.auth.application.dto.UsuarioResponse;
import com.aquamanager.modules.auth.infrastructure.mapper.UsuarioMapper;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "aquamanager_refresh_token";
    private static final int REFRESH_COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;

    private final AuthService authService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        var resultado = authService.register(request, clientIp(httpRequest), userAgent(httpRequest));
        return responder(resultado, httpResponse);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        var resultado = authService.login(request, clientIp(httpRequest), userAgent(httpRequest));
        return responder(resultado, httpResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String refreshToken = extrairRefreshCookie(httpRequest);
        var resultado = authService.refresh(refreshToken, clientIp(httpRequest), userAgent(httpRequest));
        return responder(resultado, httpResponse);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(extrairRefreshCookie(httpRequest));
        limparCookie(httpResponse);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.novaSenha());
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtils.currentUserId(), request.senhaAtual(), request.novaSenha());
    }

    @GetMapping("/confirm-email")
    public void confirmEmail(@RequestParam String token) {
        authService.confirmEmail(token);
    }

    @PostMapping("/resend-confirmation")
    public void resendConfirmation() {
        authService.resendConfirmationEmail(SecurityUtils.currentUserId());
    }

    @PostMapping("/2fa/setup")
    public ApiResponse<Setup2FaResponse> setup2Fa() {
        return ApiResponse.of(authService.setup2Fa(SecurityUtils.currentUserId()));
    }

    @PostMapping("/2fa/enable")
    public void enable2Fa(@Valid @RequestBody Codigo2FaRequest request) {
        authService.enable2Fa(SecurityUtils.currentUserId(), request.codigo());
    }

    @DeleteMapping("/2fa")
    public void disable2Fa(@Valid @RequestBody ChangePasswordRequestOnlyCurrent request) {
        authService.disable2Fa(SecurityUtils.currentUserId(), request.senhaAtual());
    }

    @GetMapping("/me")
    public ApiResponse<UsuarioResponse> me() {
        var usuario = authService.buscarPorId(SecurityUtils.currentUserId());
        return ApiResponse.of(usuarioMapper.toResponse(usuario));
    }

    public record ChangePasswordRequestOnlyCurrent(String senhaAtual) {
    }

    private ApiResponse<LoginResponse> responder(AuthenticationResult resultado, HttpServletResponse httpResponse) {
        definirCookie(httpResponse, resultado.refreshToken());
        var response = new LoginResponse(resultado.accessToken(), resultado.expiresIn(),
                usuarioMapper.toResponse(resultado.usuario()));
        return ApiResponse.of(response);
    }

    private void definirCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void limparCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extrairRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
