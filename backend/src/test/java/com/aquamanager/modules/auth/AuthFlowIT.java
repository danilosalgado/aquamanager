package com.aquamanager.modules.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aquamanager.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Testa o fluxo completo de autenticação e o enforcement de RBAC ponta a ponta via HTTP. */
@AutoConfigureMockMvc
class AuthFlowIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String SENHA = "SenhaForte123";

    @Test
    void fluxoCompletoDeRegistroLoginRefreshELogout() throws Exception {
        String email = "admin+" + UUID.randomUUID() + "@fazenda.com.br";

        MvcResult registro = registrar(email);
        String accessToken = JsonPath.read(registro.getResponse().getContentAsString(), "$.data.accessToken");
        Cookie refreshCookie = registro.getResponse().getCookie("aquamanager_refresh_token");
        assertNotNullCookie(refreshCookie);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.role").value("ADMINISTRADOR"));

        MvcResult refresh = mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andReturn();
        Cookie novoRefreshCookie = refresh.getResponse().getCookie("aquamanager_refresh_token");

        mockMvc.perform(post("/api/v1/auth/logout").cookie(novoRefreshCookie))
                .andExpect(status().isOk());

        // Token de refresh revogado não deve mais funcionar
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(novoRefreshCookie))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void loginComSenhaErradaDeveRetornarNaoAutorizado() throws Exception {
        String email = "admin+" + UUID.randomUUID() + "@fazenda.com.br";
        registrar(email);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "senha", "SenhaErrada999"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioNaoPodeCriarTanque_masAdministradorPode() throws Exception {
        String emailAdmin = "admin+" + UUID.randomUUID() + "@fazenda.com.br";
        MvcResult registro = registrar(emailAdmin);
        String tokenAdmin = JsonPath.read(registro.getResponse().getContentAsString(), "$.data.accessToken");

        String emailFuncionario = "func+" + UUID.randomUUID() + "@fazenda.com.br";
        String payloadUsuario = objectMapper.writeValueAsString(Map.of(
                "nome", "Funcionário Teste",
                "email", emailFuncionario,
                "senha", SENHA,
                "role", "FUNCIONARIO"));

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadUsuario))
                .andExpect(status().isOk());

        MvcResult loginFuncionario = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", emailFuncionario, "senha", SENHA))))
                .andExpect(status().isOk())
                .andReturn();
        String tokenFuncionario = JsonPath.read(loginFuncionario.getResponse().getContentAsString(), "$.data.accessToken");

        String payloadTanque = objectMapper.writeValueAsString(Map.of(
                "nome", "Tanque Teste", "codigo", "TQ-" + UUID.randomUUID().toString().substring(0, 6),
                "tipo", "ESCAVADO"));

        mockMvc.perform(post("/api/v1/tanques")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTanque))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/tanques")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTanque))
                .andExpect(status().isOk());
    }

    private MvcResult registrar(String email) throws Exception {
        String documento = String.valueOf(System.nanoTime());
        Map<String, Object> payload = Map.of(
                "nomeEmpresa", "Fazenda Teste",
                "documento", documento,
                "emailEmpresa", email,
                "nomeUsuario", "Admin Teste",
                "emailUsuario", email,
                "senha", SENHA);

        return mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void assertNotNullCookie(Cookie cookie) {
        org.junit.jupiter.api.Assertions.assertNotNull(cookie, "Cookie de refresh deveria ter sido definido");
    }
}
