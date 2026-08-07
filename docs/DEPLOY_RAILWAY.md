# Deploy no Railway

O Railway hospeda os 3 componentes do AquaManager como serviços separados dentro do
mesmo projeto: **Postgres** (plugin gerenciado), **backend** (Spring Boot, a partir de
`backend/Dockerfile`) e **frontend** (Vite + nginx, a partir de `frontend/Dockerfile`).

Cada serviço de código aponta pra um subdiretório do mesmo repositório (monorepo) — o
Railway detecta o `Dockerfile` e o `railway.json` de cada pasta automaticamente.

## Por que a ordem importa

O frontend faz *bake* da URL da API no bundle JavaScript **em tempo de build** (Vite lê
`VITE_API_BASE_URL` uma vez, na hora do `npm run build` — não dá pra trocar depois só
mudando uma variável de ambiente em runtime, como dá pro backend). Por isso:

1. Postgres primeiro
2. Backend depois (ele só precisa do Postgres)
3. Frontend por último (ele precisa saber a URL pública do backend *antes* de buildar)

## 1. Postgres

No projeto Railway: **New → Database → PostgreSQL**. Não precisa configurar nada — o
Railway expõe as variáveis `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
automaticamente para os outros serviços do mesmo projeto via `${{Postgres.PGHOST}}` etc.

## 2. Backend

**New → GitHub Repo** → escolher este repositório → em **Settings → Root Directory**,
apontar para `backend`. O Railway encontra `backend/Dockerfile` e `backend/railway.json`
sozinho (healthcheck em `/actuator/health` já configurado).

### Variáveis de ambiente do backend

Usa referências entre serviços do próprio Railway (`${{Postgres.PGHOST}}`) em vez de
copiar valores manualmente — assim, se o Postgres for recriado, não precisa atualizar
nada aqui.

```
SPRING_PROFILES_ACTIVE=docker
DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

JWT_ACCESS_SECRET=<gere um segredo forte — nunca reaproveite o de dev>
JWT_ACCESS_EXPIRATION_MINUTES=15
JWT_REFRESH_EXPIRATION_DAYS=30
JWT_ISSUER=aquamanager

CORS_ALLOWED_ORIGINS=<preencher depois de criar o serviço do frontend>
FRONTEND_BASE_URL=<preencher depois de criar o serviço do frontend>

TRIAL_DAYS=14
```

Não defina `PORT` — o Railway injeta essa variável sozinho e o `application.yml` já lê
`${PORT:${BACKEND_PORT:8080}}`.

Depois de deployado, o Railway atribui um domínio público (**Settings → Networking →
Generate Domain**). Anota essa URL — é a `VITE_API_BASE_URL` do passo 3 (`https://
<dominio-do-backend>/api/v1`).

### Variáveis opcionais (mesmas do `.env.example`, mesmo significado)

Só configure as que for usar de verdade — tudo tem fallback seguro (Mock/desabilitado):

```
ASAAS_ENABLED=false
STRIPE_ENABLED=false
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
STRIPE_PRICE_ID=
EMAIL_ENABLED=false
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=https://<dominio-do-backend>/api/v1/integracoes/google/callback
GEMINI_API_KEY=
GEMINI_MODEL=gemini-flash-latest
OPENWEATHERMAP_API_KEY=
```

**Se for ativar Stripe/Asaas em produção**: os webhooks configurados hoje apontam pro
túnel ngrok de desenvolvimento — isso não existe em produção. Depois do deploy, crie um
novo endpoint de webhook no Dashboard do Stripe/Asaas apontando para
`https://<dominio-do-backend>/api/v1/billing/webhooks/stripe` (ou `/asaas`), copie o
novo secret gerado e atualize `STRIPE_WEBHOOK_SECRET`/`ASAAS_WEBHOOK_TOKEN` no Railway.

## 3. Frontend

**New → GitHub Repo** → mesmo repositório → **Root Directory** = `frontend`.

### Build-time variable (crítico — precisa estar em "Build Variables", não só "Variables")

```
VITE_API_BASE_URL=https://<dominio-do-backend-do-passo-2>/api/v1
```

Sem isso, o frontend builda apontando pra `http://localhost:8080/api/v1` (o default do
código) e nada funciona em produção.

Não defina `PORT` aqui também — o Dockerfile já tem `ENV PORT=80` como fallback e o
Railway sobrescreve isso com a porta real dele em runtime.

Depois do deploy, gera o domínio público do frontend (mesma tela de Networking) e volta
no serviço do **backend** para preencher `CORS_ALLOWED_ORIGINS` e `FRONTEND_BASE_URL`
com essa URL — sem isso, o navegador bloqueia as chamadas à API por CORS.

## 4. Checklist pós-deploy

- [ ] `https://<dominio-do-backend>/actuator/health` retorna `{"status":"UP"}`
- [ ] `https://<dominio-do-frontend>` carrega a tela de login
- [ ] Cadastro de uma empresa de teste funciona ponta a ponta (login → dashboard)
- [ ] Se Stripe/Asaas ativos: webhook configurado com a URL de produção (não o ngrok)
- [ ] Se Google Calendar ativo: `GOOGLE_REDIRECT_URI` atualizado no Google Cloud Console
      com a URL de produção
- [ ] `JWT_ACCESS_SECRET` é um valor forte gerado pra produção, não o de exemplo/dev

## Migrações de banco

O Flyway roda automaticamente no start do backend (`spring.flyway.enabled: true`) — não
precisa rodar nada manualmente, mas o primeiro deploy demora alguns segundos a mais
enquanto as ~23 migrações são aplicadas num banco vazio.
