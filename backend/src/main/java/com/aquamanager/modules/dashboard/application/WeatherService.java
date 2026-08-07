package com.aquamanager.modules.dashboard.application;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Value("${openweathermap.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Map<String, Object> getWeather(double lat, double lon) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Map.of("error", "API Key do OpenWeatherMap não configurada.");
        }

        String url = String.format(Locale.ROOT,
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=pt_br",
                lat, lon, apiKey);

        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Falha ao buscar previsão do tempo (lat={}, lon={})", lat, lon, e);
            return Map.of("error", "Erro ao buscar previsão do tempo.");
        }
    }
}
