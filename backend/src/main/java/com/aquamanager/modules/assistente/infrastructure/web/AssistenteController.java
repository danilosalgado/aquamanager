package com.aquamanager.modules.assistente.infrastructure.web;

import com.aquamanager.modules.assistente.application.AssistenteService;
import com.aquamanager.shared.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/assistente")
@RequiredArgsConstructor
public class AssistenteController {

    private final AssistenteService assistenteService;

    @PostMapping("/perguntar")
    public ResponseEntity<Map<String, String>> perguntar(@RequestBody Map<String, String> request) {
        String pergunta = request.get("pergunta");
        if (pergunta == null || pergunta.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("resposta", "A pergunta não pode estar vazia."));
        }
        
        String resposta = assistenteService.perguntar(SecurityUtils.currentEmpresaId(), pergunta);
        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}
