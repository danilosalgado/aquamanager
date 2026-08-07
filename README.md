# 🐟 AquaManager

**Plataforma SaaS de gestão para piscicultores** — controle tanques, lotes, alimentação, qualidade da água, crescimento, mortalidade, estoque, financeiro, agenda, clientes e fornecedores em um único lugar, com alertas automáticos, índice de saúde por tanque e um assistente de IA.

---

## ✨ Funcionalidades

- **Multi-tenant** com isolamento em duas camadas: filtro Hibernate por requisição + Row-Level Security (RLS) no PostgreSQL
- **Autenticação completa**: registro (com trial de 14 dias), login, refresh token (cookie httpOnly), esqueci senha, confirmação de e-mail, 2FA (TOTP), logs de login, rate limiting
- **RBAC**: 4 papéis (Administrador, Gerente, Funcionário, Consultor)
- **Dashboard** com KPIs em tempo real (tanques, biomassa, FCR, mortalidade, saúde, financeiro) e previsão do tempo
- **18 módulos de backend**: tenant/billing, auth, tanques, lotes, espécies, alimentação, qualidade da água, crescimento, mortalidade, estoque, financeiro, clientes, fornecedores, alertas, índice de saúde, dashboard, agenda (com sincronização Google Calendar), relatórios (PDF/Excel) e assistente de IA (Gemini)
- **Motor de alertas automático**: água fora da faixa, estoque baixo, mortalidade elevada, alimentação insuficiente, despesas acima da receita, pagamento próximo, trial vencendo
- **Índice de saúde do tanque**: algoritmo 0–100 ponderando água, conversão alimentar, mortalidade e crescimento
- **Gestão de assinaturas** com integração Asaas (mock disponível para desenvolvimento sem credenciais)
- **PWA**: instalável, com service worker e manifest
- **API documentada** via Swagger/OpenAPI
- **Dark mode** nativo

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Banco de dados | PostgreSQL 16, Flyway (22 migrations) |
| Auth | JWT (jjwt), TOTP (implementação própria RFC 6238), QR Code (ZXing) |
| Integrações | Asaas (pagamentos), Google Calendar, Gemini (IA), OpenWeatherMap (clima) |
| Frontend | React 18, TypeScript, Vite, TailwindCSS 3, Radix UI, TanStack Query, Zustand |
| Infra | Docker, Docker Compose, nginx |
| Testes | JUnit 5, Mockito, Testcontainers, Vitest |

---

## 🚀 Setup Local

### Pré-requisitos

- **JDK 17+**
- **Node.js 18+** e npm
- **Docker** e Docker Compose

### 1. Clone e configure variáveis de ambiente

```bash
git clone <repo-url>
cd aquamanager
cp .env.example .env
# Edite .env com seus valores (especialmente JWT_ACCESS_SECRET).
# As integrações (Asaas, Google, Gemini, OpenWeatherMap) são opcionais — o app
# funciona normalmente sem elas, usando implementações mock/desativadas.
```

### 2. Suba tudo via Docker Compose (recomendado)

```bash
docker compose up --build -d
```

- Backend: `http://localhost:8080` (Swagger em `/swagger-ui.html`, health em `/actuator/health`)
- Frontend: `http://localhost:5173`
- PostgreSQL: `localhost:5432`

### 3. Alternativa: rodar backend/frontend localmente

```bash
# Banco de dados
docker compose up -d postgres

# Backend
cd backend
./mvnw spring-boot:run

# Frontend (em outro terminal)
cd frontend
npm install
npm run dev
```

---

## 🧪 Testes

```bash
# Backend — testes unitários
cd backend && ./mvnw test

# Backend — inclui testes de integração (Testcontainers, requer Docker)
cd backend && ./mvnw verify

# Frontend — type-check
cd frontend && npx tsc -b

# Frontend — testes unitários
cd frontend && npm test
```

> **Nota (Windows):** em algumas combinações de Docker Desktop recente, os testes de integração (`*IT.java`) podem falhar ao negociar com o daemon via named pipe, mesmo com o Docker funcionando normalmente. Rode-os de dentro do WSL2 ou em CI Linux nesse caso — não é um defeito do projeto.

---

## 📁 Estrutura do Projeto

```
aquamanager/
├── backend/
│   ├── src/main/java/com/aquamanager/
│   │   ├── modules/           # 18 módulos de domínio
│   │   ├── shared/            # Kernel compartilhado (security, persistence, web)
│   │   └── config/            # Configurações Spring (CORS, OpenAPI, Security)
│   └── src/main/resources/
│       └── db/migration/      # 22 migrations Flyway
├── frontend/
│   ├── src/
│   │   ├── features/          # 19 features (auth, dashboard, tanques, agenda, ...)
│   │   ├── components/        # 22 UI primitivos (Radix) + componentes shared
│   │   ├── app/                # Layout, rotas, providers
│   │   ├── stores/             # Zustand (auth, UI)
│   │   ├── hooks/               # Custom hooks
│   │   ├── lib/                 # API client, utils
│   │   └── types/               # TypeScript types
│   └── Dockerfile
├── docs/                      # ARCHITECTURE.md, ROADMAP.md
├── docker-compose.yml
└── .env.example
```

---

## 🔑 Variáveis de Ambiente

Veja o arquivo [`.env.example`](.env.example) para a lista completa. As principais são:

| Variável | Descrição |
|---|---|
| `JWT_ACCESS_SECRET` | Segredo para assinar tokens JWT (mín. 32 caracteres) — **troque em produção** |
| `POSTGRES_PASSWORD` | Senha do banco PostgreSQL |
| `ASAAS_ENABLED` | `true` para integrar com Asaas, `false` para usar mock |
| `EMAIL_ENABLED` | `true` para enviar e-mails via SMTP, `false` para log no console |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Credenciais OAuth para sincronizar a Agenda com o Google Calendar (opcional) |
| `GEMINI_API_KEY` | Chave da API do Gemini para o assistente de IA (opcional, plano Professional+) |
| `OPENWEATHERMAP_API_KEY` | Chave da API do OpenWeatherMap para o widget de clima (opcional) |

---

## 📄 Licença

Projeto privado. Todos os direitos reservados.
