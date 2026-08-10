-- A venda passou a ser unitária, sem vínculo com lote/tanque — remove a coluna.
DROP INDEX IF EXISTS idx_vendas_lote;
ALTER TABLE vendas DROP COLUMN lote_id;
