ALTER TABLE registros_mortalidade ADD COLUMN causa VARCHAR(30);

UPDATE registros_mortalidade SET causa = 'MORTE';

ALTER TABLE registros_mortalidade ALTER COLUMN causa SET NOT NULL;
ALTER TABLE registros_mortalidade DROP COLUMN motivo;
