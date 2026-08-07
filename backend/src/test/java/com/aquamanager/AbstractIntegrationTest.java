package com.aquamanager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para testes de integração: sobe um PostgreSQL real via Testcontainers e aplica as
 * migrations Flyway.
 *
 * Nota (Windows): em algumas combinações de Docker Desktop recente + Testcontainers no
 * transporte "npipe", a negociação inicial com o daemon pode falhar com
 * "BadRequestException (Status 400)" mesmo com o Docker funcionando normalmente via CLI/
 * docker-compose. Se isso ocorrer, rode estes testes de dentro do WSL2 (onde o socket Unix
 * padrão funciona sem essa fricção) ou em CI Linux — não é um defeito do código do projeto.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
}
