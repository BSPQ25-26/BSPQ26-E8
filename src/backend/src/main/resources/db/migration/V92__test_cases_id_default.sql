BEGIN;

ALTER TABLE test_cases
  ALTER COLUMN id SET DEFAULT gen_random_uuid();

COMMIT;