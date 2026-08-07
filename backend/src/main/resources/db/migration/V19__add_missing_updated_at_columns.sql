-- Toda entidade Java estende BaseEntity, que mapeia createdAt E updatedAt
-- (@CreationTimestamp/@UpdateTimestamp) incondicionalmente. As tabelas abaixo
-- foram desenhadas como "log"/evento (só created_at) mas isso diverge do
-- contrato de BaseEntity e quebraria `spring.jpa.hibernate.ddl-auto: validate`
-- na subida da aplicação. Padronizamos adicionando updated_at a todas.
DO $$
DECLARE
    tabela TEXT;
BEGIN
    FOREACH tabela IN ARRAY ARRAY[
        'refresh_tokens', 'login_logs', 'password_reset_tokens', 'email_confirmation_tokens',
        'tanque_fotos', 'registros_alimentacao', 'registros_qualidade_agua',
        'registros_crescimento', 'registros_mortalidade', 'estoque_movimentacoes',
        'alertas', 'indice_saude_tanque'
    ]
    LOOP
        EXECUTE format(
            'ALTER TABLE %I ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now()',
            tabela
        );
    END LOOP;
END $$;
