package com.aquamanager.modules.auth.application;

import com.aquamanager.modules.auth.application.dto.LoginRequest;
import com.aquamanager.modules.auth.application.dto.RegisterRequest;
import com.aquamanager.modules.auth.application.dto.Setup2FaResponse;
import com.aquamanager.modules.auth.domain.Usuario;
import java.util.UUID;

public interface AuthService {

    AuthenticationResult register(RegisterRequest request, String ip, String userAgent);

    AuthenticationResult login(LoginRequest request, String ip, String userAgent);

    AuthenticationResult refresh(String refreshTokenBruto, String ip, String userAgent);

    void logout(String refreshTokenBruto);

    void forgotPassword(String email);

    void resetPassword(String token, String novaSenha);

    void changePassword(UUID usuarioId, String senhaAtual, String novaSenha);

    void confirmEmail(String token);

    void resendConfirmationEmail(UUID usuarioId);

    Setup2FaResponse setup2Fa(UUID usuarioId);

    void enable2Fa(UUID usuarioId, String codigo);

    void disable2Fa(UUID usuarioId, String senhaAtual);

    Usuario buscarPorId(UUID usuarioId);
}
