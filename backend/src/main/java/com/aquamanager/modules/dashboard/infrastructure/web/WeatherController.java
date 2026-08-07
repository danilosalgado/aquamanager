package com.aquamanager.modules.dashboard.infrastructure.web;

import com.aquamanager.modules.dashboard.application.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWeather(@RequestParam double lat, @RequestParam double lon) {
        Map<String, Object> data = weatherService.getWeather(lat, lon);
        if (data.containsKey("error")) {
            return ResponseEntity.badRequest().body(data);
        }
        return ResponseEntity.ok(data);
    }
}
