package com.aquamanager.modules.agenda.infrastructure.web;

import com.aquamanager.config.FrontendProperties;
import com.aquamanager.modules.agenda.application.GoogleCalendarSyncService;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * O endpoint {@code /callback} é necessariamente público (é o navegador, redirecionado
 * pelo Google, quem o acessa — sem Bearer token). A segurança do fluxo depende
 * inteiramente do {@code state} assinado por {@link com.aquamanager.modules.agenda.infrastructure.security.OAuthStateSigner}
 * — ver {@code SecurityConfig.PUBLIC_ENDPOINTS}.
 */
@RestController
@RequestMapping("/api/v1/integracoes/google")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarSyncService googleCalendarSyncService;
    private final FrontendProperties frontendProperties;

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String url = googleCalendarSyncService.getAuthorizationUrl(SecurityUtils.currentUserId());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam String state) {
        googleCalendarSyncService.processCallback(code, state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendProperties.baseUrl() + "/agenda")
                .build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> status() {
        boolean isConnected = googleCalendarSyncService.isConnected(SecurityUtils.currentUserId());
        return ResponseEntity.ok(Map.of("connected", isConnected));
    }

    @DeleteMapping
    public ResponseEntity<Void> desconectar() {
        googleCalendarSyncService.disconnect(SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
