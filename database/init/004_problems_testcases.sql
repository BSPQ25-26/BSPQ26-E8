BEGIN;

CREATE TABLE IF NOT EXISTS problems (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug VARCHAR(120) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  statement_md TEXT NOT NULL,
  input_spec_md TEXT NOT NULL DEFAULT '',
  output_spec_md TEXT NOT NULL DEFAULT '',
  constraints_md TEXT NOT NULL DEFAULT '',
  hints_md TEXT,
  difficulty problem_difficulty NOT NULL,
  author_id UUID REFERENCES users(id) ON DELETE SET NULL,
  solution_template TEXT,
  language_compilation_config JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT problems_slug_format_ck CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  CONSTRAINT problems_lang_cfg_obj_ck CHECK (jsonb_typeof(language_compilation_config) = 'object')
);

CREATE TABLE IF NOT EXISTS problem_languages (
  problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
  language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE RESTRICT,
  compilation_args TEXT,
  execution_args TEXT,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (problem_id, language_id)
);

CREATE TABLE IF NOT EXISTS test_cases (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
  input_data TEXT NOT NULL,
  expected_output TEXT NOT NULL,
  is_sample BOOLEAN NOT NULL DEFAULT FALSE
);

COMMIT;