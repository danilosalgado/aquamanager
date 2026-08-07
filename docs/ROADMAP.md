# Roadmap — AquaManager

## ✅ Entregue

### Backend
- [x] Scaffold Spring Boot com multi-tenant (filtro Hibernate + RLS), security, error handling, OpenAPI
- [x] Flyway: 22 migrations com seeds (planos, espécies, parâmetros ideais)
- [x] Módulo tenant: empresas, planos, assinaturas + billing gateway (Asaas + mock), trial de 14 dias
- [x] Módulo auth completo: registro, login, refresh, logout, esqueci senha, confirmação de e-mail, 2FA (TOTP), RBAC, rate limit, login logs
- [x] Módulos operacionais: tanque, espécie, lote, alimentação, qualidade da água, crescimento, mortalidade, estoque, fornecedor, cliente
- [x] Módulo financeiro: lançamentos (receitas/despesas) com categorias, marcação de pago, resumo do período
- [x] Transversais: motor de alertas automático (7 tipos de regra), índice de saúde do tanque (0–100), dashboard com agregações
- [x] Agenda/calendário com sincronização opcional ao Google Calendar (OAuth com state assinado)
- [x] Relatórios em PDF/Excel (financeiro, produção, mortalidade, estoque)
- [x] Assistente de IA (Gemini) com contexto isolado por empresa, gateado pelo plano contratado
- [x] Widget de previsão do tempo (OpenWeatherMap)
- [x] Testes: `AuthFlowIT`, `TenantIsolationIT` (Testcontainers), `JwtServiceTest`, `TotpServiceTest`, `IndiceSaudeServiceImplTest`

### Frontend
- [x] Scaffold com Vite, React 18, TypeScript, TailwindCSS, Radix UI
- [x] Design system: 22 componentes UI + 5 componentes shared
- [x] Auth: login, cadastro, esqueci senha, redefinir senha, confirmar e-mail, 2FA
- [x] Dashboard com KPIs, gráficos (Recharts), índice de saúde, widget de clima
- [x] 19 features de CRUD com formulários, validação (Zod), paginação, filtros
- [x] Perfil do usuário: dados, troca de senha, configuração 2FA
- [x] Configurações: dados da empresa, gestão do plano/assinatura (pricing table)
- [x] Agenda com sincronização Google Calendar
- [x] Relatórios (download PDF/Excel)
- [x] Assistente de IA (chat flutuante)
- [x] Simulador de lucro por lote
- [x] Layout responsivo: sidebar, topbar, sheet mobile
- [x] Dark mode
- [x] PWA (manifest + service worker via vite-plugin-pwa)
- [x] Lazy loading em todas as rotas

### Infra
- [x] Docker Compose (PostgreSQL + Backend + Frontend), verificado ponta a ponta
- [x] Dockerfiles multi-stage para backend e frontend (nginx com SPA fallback + cache + headers de segurança)
- [x] Documentação (README, ARCHITECTURE, ROADMAP)

---

## 🔜 Próximos passos

### UX e produtividade
- [ ] Busca global (Ctrl+K)
- [ ] Filtros avançados com intervalo de datas em todas as listagens
- [ ] Drag-and-drop no calendário da agenda
- [ ] Notificações push via Service Worker (a base de PWA já existe)
- [ ] Modo offline-first para leituras (cache de listas já visitadas)

### Integrações e automação
- [ ] Integração com sensores IoT (pH, temperatura, oxigênio dissolvido) alimentando `qualidadeagua` automaticamente
- [ ] Regras de alerta customizáveis pelo usuário (hoje o motor de alertas usa limiares fixos no código)
- [ ] API pública com chave de API própria (hoje a API é apenas para o frontend, via JWT de usuário)
- [ ] Webhooks para eventos do domínio (alerta criado, lote encerrado, pagamento confirmado)

### Segurança e operação
- [ ] Papel de administrador de plataforma (hoje o RBAC é só por empresa) para o time do AquaManager gerenciar clientes/planos/inadimplência
- [ ] Auditoria detalhada (quem alterou o quê) além dos `login_logs` atuais
- [ ] Rate limiting distribuído (Redis) para múltiplas instâncias — hoje é em memória, correto para uma instância
- [ ] Rodar `AuthFlowIT`/`TenantIsolationIT` em CI (GitHub Actions) — hoje validados localmente via Docker

### v1.0 — Produção
- [ ] App mobile (React Native ou Flutter) ou PWA instalável como caminho principal mobile
- [ ] Multi-idioma (pt-BR, en, es)
- [ ] Backups automatizados do PostgreSQL
- [ ] Monitoramento (Prometheus + Grafana / equivalente gerenciado)
- [ ] CI/CD pipeline
- [ ] Testes E2E (Playwright)
- [ ] Suporte a múltiplas unidades produtivas por empresa

---

## 💡 Ideias futuras

- Marketplace de insumos entre piscicultores
- Benchmarking anônimo entre fazendas
- IA para previsão de crescimento e mortalidade (hoje o assistente responde perguntas; um modelo preditivo é o próximo passo natural)
- Rastreabilidade (da despesca ao consumidor final)
- Integração com ERPs (SAP, TOTVS)
- White-label completo para o plano Enterprise
