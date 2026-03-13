BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS idx_problem_languages_single_default
  ON problem_languages (problem_id)
  WHERE is_default;

CREATE INDEX IF NOT EXISTS idx_problems_browse
  ON problems (status, difficulty, published_at DESC)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_test_cases_problem_sample
  ON test_cases (problem_id, is_sample);

CREATE INDEX IF NOT EXISTS idx_resolution_user_status
  ON user_problem_resolution_status (user_id, status);

CREATE INDEX IF NOT EXISTS idx_submissions_user_recent
  ON submissions (user_id, submitted_at DESC);

CREATE INDEX IF NOT EXISTS idx_submissions_problem_status
  ON submissions (problem_id, status, submitted_at DESC);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_active
  ON refresh_tokens (user_id, expires_at DESC)
  WHERE revoked_at IS NULL;

COMMIT;
