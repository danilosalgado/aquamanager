package com.aquamanager.modules.agenda.application;

import com.aquamanager.modules.agenda.domain.Evento;
import com.aquamanager.modules.agenda.infrastructure.security.OAuthStateSigner;
import com.aquamanager.modules.auth.domain.Usuario;
import com.aquamanager.modules.auth.infrastructure.persistence.UsuarioRepository;
import com.aquamanager.shared.domain.exception.ResourceNotFoundException;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarSyncService {

    @Value("${google.client.id:}")
    private String clientId;

    @Value("${google.client.secret:}")
    private String clientSecret;

    @Value("${google.redirect.uri:}")
    private String redirectUri;

    private final UsuarioRepository usuarioRepository;
    private final OAuthStateSigner oAuthStateSigner;

    private GoogleAuthorizationCodeFlow flow;

    private GoogleAuthorizationCodeFlow getFlow() {
        if (flow == null) {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientId,
                    clientSecret,
                    Collections.singletonList(CalendarScopes.CALENDAR_EVENTS))
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .build();
        }
        return flow;
    }

    public String getAuthorizationUrl(UUID usuarioId) {
        if (clientId == null || clientId.isEmpty()) {
            throw new IllegalStateException("Google Client ID não configurado");
        }
        return getFlow().newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(oAuthStateSigner.sign(usuarioId))
                .build();
    }

    /** {@code state} deve ser exatamente o valor assinado devolvido por {@link #getAuthorizationUrl}. */
    public void processCallback(String code, String state) {
        UUID usuarioId = oAuthStateSigner.validar(state);
        try {
            TokenResponse response = getFlow().newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            
            usuario.setGoogleRefreshToken(response.getRefreshToken());
            usuarioRepository.save(usuario);
        } catch (IOException e) {
            log.error("Erro ao processar callback do Google OAuth", e);
            throw new RuntimeException("Falha ao autenticar com o Google", e);
        }
    }

    public boolean isConnected(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return usuario.getGoogleRefreshToken() != null && !usuario.getGoogleRefreshToken().isEmpty();
    }
    
    public void disconnect(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.setGoogleRefreshToken(null);
        usuarioRepository.save(usuario);
    }

    public void syncEvent(Evento evento, UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null || usuario.getGoogleRefreshToken() == null) {
            return;
        }

        try {
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setTokenServerUrl(new com.google.api.client.http.GenericUrl("https://oauth2.googleapis.com/token"))
                    .setClientAuthentication(new com.google.api.client.auth.oauth2.ClientParametersAuthentication(clientId, clientSecret))
                    .build();
            
            credential.setRefreshToken(usuario.getGoogleRefreshToken());

            Calendar service = new Calendar.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName("AquaManager")
                    .build();

            Event event = new Event()
                .setSummary(evento.getTitulo())
                .setDescription(evento.getDescricao() + "\n\nGerado via AquaManager - Tipo: " + evento.getTipo());

            DateTime startDateTime = new DateTime(evento.getDataInicio().toEpochMilli());
            EventDateTime start = new EventDateTime().setDateTime(startDateTime);
            event.setStart(start);

            if (evento.getDataFim() != null) {
                DateTime endDateTime = new DateTime(evento.getDataFim().toEpochMilli());
                EventDateTime end = new EventDateTime().setDateTime(endDateTime);
                event.setEnd(end);
            } else {
                event.setEnd(start); // Se não tem fim, define mesma hora do início
            }

            service.events().insert("primary", event).execute();

        } catch (Exception e) {
            log.error("Erro ao sincronizar evento com o Google Calendar", e);
        }
    }
}
