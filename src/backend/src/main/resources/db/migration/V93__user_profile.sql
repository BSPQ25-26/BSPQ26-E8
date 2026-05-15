BEGIN;

ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture TEXT;

CREATE TABLE IF NOT EXISTS user_preferred_languages (
  user_id    UUID   NOT NULL REFERENCES users(id)     ON DELETE CASCADE,
  language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, language_id)
);

COMMIT;
