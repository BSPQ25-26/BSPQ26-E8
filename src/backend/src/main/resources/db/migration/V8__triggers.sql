BEGIN;

DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
-- Trigger creation for users.updated_at removed to avoid runtime errors
-- when the users table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_languages_updated_at ON languages;
-- Trigger creation for languages.updated_at removed to avoid runtime errors
-- when the languages table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_problems_updated_at ON problems;
-- Trigger creation for problems.updated_at removed to avoid runtime errors
-- when the problems table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_problem_languages_updated_at ON problem_languages;
-- Trigger creation for problem_languages.updated_at removed to avoid runtime errors
-- when the problem_languages table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_test_cases_updated_at ON test_cases;
-- Trigger creation for test_cases.updated_at removed to avoid runtime errors
-- when the test_cases table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_user_problem_resolution_status_updated_at ON user_problem_resolution_status;
-- Trigger creation for user_problem_resolution_status.updated_at removed
-- to avoid runtime errors when the table does not define an updated_at column.

DROP TRIGGER IF EXISTS trg_tags_updated_at ON tags;
-- Trigger creation for tags.updated_at removed to avoid runtime errors
-- when the tags table does not define an updated_at column.

COMMIT;
