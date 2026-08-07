-- Extensões necessárias para geração de UUID no banco (defesa adicional, além do
-- Hibernate gerar os IDs client-side).
CREATE EXTENSION IF NOT EXISTS pgcrypto;
