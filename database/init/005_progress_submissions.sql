BEGIN;

CREATE TABLE IF NOT EXISTS user_problem_resolution_status (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
  status resolution_status NOT NULL DEFAULT 'not_started',
  CONSTRAINT user_problem_resolution_status_unique UNIQUE (user_id, problem_id),
);

CREATE TABLE IF NOT EXISTS submissions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
  language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE RESTRICT,
  source_code TEXT NOT NULL,
  status submission_status NOT NULL DEFAULT 'queued',
  verdict_message TEXT,
  runtime_ms INTEGER,
  memory_mb INTEGER,
  testcases_passed INTEGER NOT NULL DEFAULT 0,
  testcases_total INTEGER,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  evaluated_at TIMESTAMPTZ,
  CONSTRAINT submissions_runtime_ck CHECK (runtime_ms IS NULL OR runtime_ms > 0),
  CONSTRAINT submissions_memory_ck CHECK (memory_mb IS NULL OR memory_mb > 0),
  CONSTRAINT submissions_passed_ck CHECK (testcases_passed >= 0),
  CONSTRAINT submissions_total_ck CHECK (testcases_total IS NULL OR testcases_total >= 0)
);

COMMIT;
