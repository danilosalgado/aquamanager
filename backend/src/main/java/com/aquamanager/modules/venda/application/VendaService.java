package com.aquamanager.modules.venda.application;

import com.aquamanager.modules.venda.application.dto.LucroBrutoPorTanqueResponse;
import com.aquamanager.modules.venda.application.dto.VendaRequest;
import com.aquamanager.modules.venda.domain.Venda;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendaService {

    Page<Venda> listar(UUID empresaId, UUID loteId, String categoriaProduto, Pageable pageable);

    Venda buscar(UUID empresaId, UUID id);

    Venda criar(UUID empresaId, VendaRequest request);

    Venda atualizar(UUID empresaId, UUID id, VendaRequest request);

    void remover(UUID empresaId, UUID id);

    List<String> listarCategorias(UUID empresaId);

    List<LucroBrutoPorTanqueResponse> relatorioLucroBrutoPorTanque(UUID empresaId);
}
