BEGIN;

CREATE TABLE IF NOT EXISTS tags (
  id BIGSERIAL PRIMARY KEY,
  slug VARCHAR(60) NOT NULL UNIQUE,
  name VARCHAR(80) NOT NULL UNIQUE,
  CONSTRAINT tags_slug_format_ck CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

CREATE TABLE IF NOT EXISTS problem_tags (
  problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
  tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
  PRIMARY KEY (problem_id, tag_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  ip_address VARCHAR(255),
  user_agent TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMIT;
