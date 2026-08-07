package com.aquamanager.modules.tenant.infrastructure.web;

import com.aquamanager.modules.tenant.application.dto.PlanoResponse;
import com.aquamanager.modules.tenant.infrastructure.mapper.PlanoMapper;
import com.aquamanager.modules.tenant.infrastructure.persistence.PlanoRepository;
import com.aquamanager.shared.infrastructure.web.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoRepository planoRepository;
    private final PlanoMapper planoMapper;

    @GetMapping
    public ApiResponse<List<PlanoResponse>> listar() {
        return ApiResponse.of(planoRepository.findAll().stream().map(planoMapper::toResponse).toList());
    }
}
