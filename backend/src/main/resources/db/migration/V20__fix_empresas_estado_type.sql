-- Corrige tipo da coluna estado: CHAR(2) → VARCHAR(2) para alinhar com a entidade JPA
ALTER TABLE empresas ALTER COLUMN estado TYPE VARCHAR(2);
