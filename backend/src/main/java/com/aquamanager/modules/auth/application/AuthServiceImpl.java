package com.aquamanager.modules.auth.application;

import com.aquamanager.config.FrontendProperties;
import com.aquamanager.modules.auth.application.dto.LoginRequest;
import com.aquamanager.modules.auth.application.dto.RegisterRequest;
import com.aquamanager.modules.auth.application.dto.Setup2FaResponse;
import com.aquamanager.modules.auth.domain.EmailConfirmationToken;
import com.aquamanager.modules.auth.domain.LoginLog;
import com.aquamanager.modules.auth.domain.PasswordResetToken;
import com.aquamanager.modules.auth.domain.RefreshToken;
import com.aquamanager.modules.auth.domain.Usuario;
import com.aquamanager.modules.auth.infrastructure.persistence.EmailConfirmationTokenRepository;
import com.aquamanager.modules.auth.infrastructure.persistence.LoginLogRepository;
import com.aquamanager.modules.auth.infrastructure.persistence.PasswordResetTokenRepository;
import com.aquamanager.modules.auth.infrastructure.persistence.RefreshTokenRepository;
import com.aquamanager.modules.auth.infrastructure.persistence.UsuarioRepository;
import com.aquamanager.modules.auth.infrastructure.security.QrCodeGenerator;
import com.aquamanager.modules.auth.infrastructure.security.TotpService;
import com.aquamanager.modules.tenant.application.EmpresaService;
import com.aquamanager.modules.tenant.domain.Empresa;
import com.aquamanager.shared.application.port.EmailSender;
import com.aquamanager.shared.domain.Role;
import com.aquamanager.shared.domain.exception.BusinessException;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.aquamanager.shared.infrastructure.security.JwtService;
import com.aquamanager.shared.infrastructure.security.RateLimiter;
import com.aquamanager.shared.infrastructure.security.TokenHasher;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MAX_TENTATIVAS_LOGIN = 5;
    private static final Duration BLOQUEIO_CONTA = Duration.ofMinutes(15);
    private static final Duration RESET_SENHA_TTL = Duration.ofHours(1);
    private static final Duration CONFIRMACAO_EMAIL_TTL = Duration.ofHours(48);

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginLogRepository loginLogRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final EmpresaService empresaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final QrCodeGenerator qrCodeGenerator;
    private final EmailSender emailSender;
    private final RateLimiter rateLimiter;
    private final TokenHasher tokenHasher;
    private final FrontendProperties frontendProperties;

    @Override
    @Transactional
    public AuthenticationResult register(RegisterRequest request, String ip, String userAgent) {
        exigirLimite("register:" + ip, 5, Duration.ofHours(1));

        if (usuarioRepository.existsByEmailIgnoreCase(request.emailUsuario())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", "Já existe uma conta com este e-mail.");
        }

        Empresa empresa = empresaService.criarComTrial(
                request.nomeEmpresa(), request.documento(), request.endereco(), request.cidade(),
                request.estado(), request.telefone(), request.emailEmpresa());

        Usuario usuario = new Usuario();
        usuario.setEmpresaId(empresa.getId());
        usuario.setNome(request.nomeUsuario());
        usuario.setEmail(request.emailUsuario());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(Role.ADMINISTRADOR);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        enviarEmailConfirmacao(usuario);

        return autenticar(usuario, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthenticationResult login(LoginRequest request, String ip, String userAgent) {
        exigirLimite("login:" + ip, 15, Duration.ofMinutes(1));
        exigirLimite("login:" + request.email().toLowerCase(), MAX_TENTATIVAS_LOGIN, BLOQUEIO_CONTA);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (usuario == null) {
            registrarLogin(null, null, request.email(), ip, userAgent, false, "USUARIO_NAO_ENCONTRADO");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        if (usuario.estaBloqueado()) {
            registrarLogin(usuario.getId(), usuario.getEmpresaId(), request.email(), ip, userAgent, false, "CONTA_BLOQUEADA");
            throw new BusinessException("ACCOUNT_LOCKED",
                    "Conta temporariamente bloqueada por excesso de tentativas. Tente novamente mais tarde.");
        }

        if (!usuario.isAtivo() || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            registrarFalhaSenha(usuario);
            registrarLogin(usuario.getId(), usuario.getEmpresaId(), request.email(), ip, userAgent, false, "SENHA_INVALIDA");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        empresaService.garantirAcessoLiberado(usuario.getEmpresaId());

        if (usuario.isTwoFactorEnabled()) {
            if (request.codigo2fa() == null || request.codigo2fa().isBlank()) {
                throw new BusinessException("2FA_REQUIRED", "Informe o código do autenticador de dois fatores.");
            }
            if (!totpService.validar(usuario.getTwoFactorSecret(), request.codigo2fa())) {
                registrarLogin(usuario.getId(), usuario.getEmpresaId(), request.email(), ip, userAgent, false, "2FA_INVALIDO");
                throw new BusinessException("2FA_INVALID", "Código de dois fatores inválido.");
            }
        }

        usuario.setFailedLoginAttempts(0);
        usuario.setLockedUntil(null);
        registrarLogin(usuario.getId(), usuario.getEmpresaId(), request.email(), ip, userAgent, true, null);

        return autenticar(usuario, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String refreshTokenBruto, String ip, String userAgent) {
        String hash = tokenHasher.hash(refreshTokenBruto);
        RefreshToken tokenAtual = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Sessão expirada. Faça login novamente."));

        if (!tokenAtual.valido()) {
            throw new BusinessException("INVALID_REFRESH_TOKEN", "Sessão expirada. Faça login novamente.");
        }

        tokenAtual.setRevoked(true);
        Usuario usuario = tokenAtual.getUsuario();
        AuthenticationResult resultado = autenticar(usuario, ip, userAgent);

        refreshTokenRepository.findByTokenHash(tokenHasher.hash(resultado.refreshToken()))
                .ifPresent(novo -> tokenAtual.setReplacedById(novo.getId()));

        return resultado;
    }

    @Override
    @Transactional
    public void logout(String refreshTokenBruto) {
        if (refreshTokenBruto == null || refreshTokenBruto.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(tokenHasher.hash(refreshTokenBruto))
                .ifPresent(token -> token.setRevoked(true));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        exigirLimite("forgot-password:" + email.toLowerCase(), 5, Duration.ofHours(1));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null) {
            log.info("Solicitação de reset de senha para e-mail não cadastrado: {}", email);
            return; // não revela se o e-mail existe
        }

        String tokenBruto = tokenHasher.gerarTokenOpaco();
        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setTokenHash(tokenHasher.hash(tokenBruto));
        token.setExpiresAt(Instant.now().plus(RESET_SENHA_TTL));
        passwordResetTokenRepository.save(token);

        String link = frontendProperties.baseUrl() + "/redefinir-senha?token=" + tokenBruto;
        emailSender.enviar(usuario.getEmail(), "Recuperação de senha - AquaManager",
                "Clique no link para redefinir sua senha (válido por 1 hora): <a href=\"%s\">%s</a>"
                        .formatted(link, link));
    }

    @Override
    @Transactional
    public void resetPassword(String token, String novaSenha) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHasher.hash(token))
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Token inválido ou expirado."));

        if (!resetToken.valido()) {
            throw new BusinessException("INVALID_TOKEN", "Token inválido ou expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setFailedLoginAttempts(0);
        usuario.setLockedUntil(null);
        resetToken.setUsed(true);
        refreshTokenRepository.revogarTodosDoUsuario(usuario.getId());
    }

    @Override
    @Transactional
    public void changePassword(UUID usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarPorId(usuarioId);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Senha atual incorreta.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        refreshTokenRepository.revogarTodosDoUsuario(usuarioId);
    }

    @Override
    @Transactional
    public void confirmEmail(String token) {
        EmailConfirmationToken confirmToken = emailConfirmationTokenRepository.findByTokenHash(tokenHasher.hash(token))
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Token inválido ou expirado."));

        if (!confirmToken.valido()) {
            throw new BusinessException("INVALID_TOKEN", "Token inválido ou expirado.");
        }

        confirmToken.getUsuario().setEmailConfirmado(true);
        confirmToken.setUsed(true);
    }

    @Override
    @Transactional
    public void resendConfirmationEmail(UUID usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        if (usuario.isEmailConfirmado()) {
            throw new BusinessException("EMAIL_ALREADY_CONFIRMED", "E-mail já confirmado.");
        }
        enviarEmailConfirmacao(usuario);
    }

    @Override
    @Transactional
    public Setup2FaResponse setup2Fa(UUID usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        String segredo = totpService.gerarSegredo();
        usuario.setTwoFactorSecret(segredo);
        usuario.setTwoFactorEnabled(false);

        String uri = totpService.gerarUriProvisionamento(segredo, usuario.getEmail());
        return new Setup2FaResponse(segredo, qrCodeGenerator.gerarDataUri(uri));
    }

    @Override
    @Transactional
    public void enable2Fa(UUID usuarioId, String codigo) {
        Usuario usuario = buscarPorId(usuarioId);
        if (usuario.getTwoFactorSecret() == null) {
            throw new BusinessException("2FA_NOT_SETUP", "Inicie a configuração do 2FA antes de ativar.");
        }
        if (!totpService.validar(usuario.getTwoFactorSecret(), codigo)) {
            throw new BusinessException("2FA_INVALID_CODE", "Código inválido.");
        }
        usuario.setTwoFactorEnabled(true);
    }

    @Override
    @Transactional
    public void disable2Fa(UUID usuarioId, String senhaAtual) {
        Usuario usuario = buscarPorId(usuarioId);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Senha incorreta.");
        }
        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
    }

    // ---- helpers privados ----

    private AuthenticationResult autenticar(Usuario usuario, String ip, String userAgent) {
        String accessToken = jwtService.generateAccessToken(
                usuario.getId(), usuario.getEmpresaId(), usuario.getEmail(), usuario.getNome(), usuario.getRole());

        String refreshTokenBruto = tokenHasher.gerarTokenOpaco();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(tokenHasher.hash(refreshTokenBruto));
        refreshToken.setExpiresAt(Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS));
        refreshToken.setIp(ip);
        refreshToken.setUserAgent(userAgent);
        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResult(accessToken, jwtService.accessExpirationSeconds(), refreshTokenBruto, usuario);
    }

    private void registrarFalhaSenha(Usuario usuario) {
        usuario.setFailedLoginAttempts(usuario.getFailedLoginAttempts() + 1);
        if (usuario.getFailedLoginAttempts() >= MAX_TENTATIVAS_LOGIN) {
            usuario.setLockedUntil(Instant.now().plus(BLOQUEIO_CONTA));
        }
    }

    private void registrarLogin(UUID usuarioId, UUID empresaId, String email, String ip, String userAgent,
                                 boolean sucesso, String motivoFalha) {
        LoginLog registro = new LoginLog();
        registro.setUsuarioId(usuarioId);
        registro.setEmpresaId(empresaId);
        registro.setEmailTentativa(email);
        registro.setIp(ip);
        registro.setUserAgent(userAgent);
        registro.setSucesso(sucesso);
        registro.setMotivoFalha(motivoFalha);
        loginLogRepository.save(registro);
    }

    private void enviarEmailConfirmacao(Usuario usuario) {
        String tokenBruto = tokenHasher.gerarTokenOpaco();
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setUsuario(usuario);
        token.setTokenHash(tokenHasher.hash(tokenBruto));
        token.setExpiresAt(Instant.now().plus(CONFIRMACAO_EMAIL_TTL));
        emailConfirmationTokenRepository.save(token);

        String link = frontendProperties.baseUrl() + "/confirmar-email?token=" + tokenBruto;
        try {
            emailSender.enviar(usuario.getEmail(), "Confirme seu e-mail - AquaManager",
                    "Bem-vindo(a) ao AquaManager! Confirme seu e-mail: <a href=\"%s\">%s</a>"
                            .formatted(link, link));
        } catch (Exception ex) {
            log.warn("Falha ao enviar e-mail de confirmação para {}, o cadastro prosseguiu normalmente.",
                    usuario.getEmail(), ex);
        }
    }

    private void exigirLimite(String chave, int maxTentativas, Duration janela) {
        if (!rateLimiter.tryAcquire(chave, maxTentativas, janela)) {
            throw new BusinessException("RATE_LIMITED", "Muitas tentativas. Aguarde alguns instantes e tente novamente.");
        }
    }
}
