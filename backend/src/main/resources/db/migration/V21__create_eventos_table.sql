CREATE TABLE eventos (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITH TIME ZONE,
    concluido BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_eventos_empresa_id ON eventos(empresa_id);
CREATE INDEX idx_eventos_data_inicio ON eventos(data_inicio);
