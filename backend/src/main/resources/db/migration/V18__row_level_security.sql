-- ============================================================================
-- Row-Level Security: segunda camada de isolamento multi-tenant, independente
-- do filtro Hibernate (com.aquamanager.shared.infrastructure.persistence.TenantRlsAspect).
-- A variável de sessão "app.tenant_id" é setada via SELECT set_config(...) no
-- início de cada transação de negócio (ver TenantRlsAspect / TenantSessionManager).
--
-- Exceção deliberada: usuarios, refresh_tokens, login_logs, password_reset_tokens
-- e email_confirmation_tokens NÃO recebem RLS. São tabelas de fronteira de
-- autenticação, consultadas por e-mail/token antes de o tenant ser conhecido
-- (ex.: login precisa localizar o usuário por e-mail em toda a base). O
-- isolamento delas é garantido pelo filtro Hibernate (após login) e por chaves
-- opacas com hash (tokens).
--
-- Bypass de sistema: toda policy também libera acesso quando a variável de
-- sessão "app.bypass_tenant_check" = 'true'. Usado exclusivamente por jobs
-- internos e legitimamente cross-tenant (ex.: expiração de trial), ativado via
-- TenantSessionManager#runAsSystem — nunca exposto a requisições HTTP.
-- ============================================================================

-- ---- empresas: linha representa o próprio tenant (id = tenant), não empresa_id.
-- Precisa de política de INSERT permissiva porque o cadastro cria a empresa
-- ANTES de o contexto de tenant existir (bootstrap). Leitura/edição/remoção
-- seguem restritas ao próprio tenant (ou ao bypass de sistema).
ALTER TABLE empresas ENABLE ROW LEVEL SECURITY;
ALTER TABLE empresas FORCE ROW LEVEL SECURITY;

CREATE POLICY empresas_select ON empresas FOR SELECT
    USING (id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY empresas_update ON empresas FOR UPDATE
    USING (id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY empresas_delete ON empresas FOR DELETE
    USING (id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY empresas_insert ON empresas FOR INSERT
    WITH CHECK (true);

-- ---- assinaturas: mesma exceção de bootstrap (criada junto com a empresa).
ALTER TABLE assinaturas ENABLE ROW LEVEL SECURITY;
ALTER TABLE assinaturas FORCE ROW LEVEL SECURITY;

CREATE POLICY assinaturas_select ON assinaturas FOR SELECT
    USING (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY assinaturas_update ON assinaturas FOR UPDATE
    USING (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY assinaturas_delete ON assinaturas FOR DELETE
    USING (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY assinaturas_insert ON assinaturas FOR INSERT
    WITH CHECK (true);

-- ---- especies: catálogo global (empresa_id NULL) visível a todos + espécies
-- customizadas visíveis apenas ao tenant dono.
ALTER TABLE especies ENABLE ROW LEVEL SECURITY;
ALTER TABLE especies FORCE ROW LEVEL SECURITY;

CREATE POLICY especies_select ON especies FOR SELECT
    USING (empresa_id IS NULL OR empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');
CREATE POLICY especies_modify ON especies FOR ALL
    USING (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true')
    WITH CHECK (empresa_id = current_setting('app.tenant_id', true)::uuid
        OR current_setting('app.bypass_tenant_check', true) = 'true');

-- ---- Tabelas estritamente tenant-scoped (empresa_id NOT NULL, sem exceção de bootstrap).
DO $$
DECLARE
    tabela TEXT;
BEGIN
    FOREACH tabela IN ARRAY ARRAY[
        'tanques', 'tanque_fotos', 'lotes', 'registros_alimentacao',
        'registros_qualidade_agua', 'registros_crescimento', 'registros_mortalidade',
        'fornecedores', 'clientes', 'estoque_itens', 'estoque_movimentacoes',
        'lancamentos_financeiros', 'alertas', 'indice_saude_tanque'
    ]
    LOOP
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', tabela);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', tabela);
        EXECUTE format(
            'CREATE POLICY %I ON %I FOR ALL '
            || 'USING (empresa_id = current_setting(''app.tenant_id'', true)::uuid '
            || 'OR current_setting(''app.bypass_tenant_check'', true) = ''true'') '
            || 'WITH CHECK (empresa_id = current_setting(''app.tenant_id'', true)::uuid '
            || 'OR current_setting(''app.bypass_tenant_check'', true) = ''true'')',
            tabela || '_tenant_isolation', tabela
        );
    END LOOP;
END $$;
