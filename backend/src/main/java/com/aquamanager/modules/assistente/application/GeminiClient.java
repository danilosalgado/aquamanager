package com.aquamanager.modules.assistente.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    // Configurável porque a Google descontinua modelos periodicamente (ex.: gemini-1.5-flash
    // saiu de linha, gemini-2.5-flash "não está mais disponível para novos usuários" nesta
    // conta) — gemini-flash-latest é um alias mantido pela Google que sempre aponta para o
    // modelo flash atual, reduzindo a recorrência desse problema. Pode ser sobrescrito via
    // env var GEMINI_MODEL sem precisar recompilar.
    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

    private final RestTemplate restTemplate;

    public GeminiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "API Key do Gemini não configurada.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
            return "Não consegui gerar uma resposta.";
        } catch (Exception e) {
            log.error("Falha ao chamar a API do Gemini", e);
            return "Erro ao contatar o assistente de IA.";
        }
    }
}
