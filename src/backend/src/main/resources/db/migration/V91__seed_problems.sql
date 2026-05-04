BEGIN;

INSERT INTO problems (
  id,
  slug,
  title,
  statement_md,
  input_spec_md,
  output_spec_md,
  constraints_md,
  hints_md,
  difficulty,
  author_id,
  solution_template,
  language_compilation_config
)
VALUES
  (
    '2f510332-4f9d-4e8a-8875-36f0f6d28c21',
    'two-sum',
    'Two Sum',
    'Given an array of integers and a target, return the indices of the two numbers that add up to target.',
    'First line: n. Second line: n integers. Third line: target.',
    'Two zero-based indices separated by a space.',
    '2 <= n <= 100000',
    'Use a hash map to track complements.',
    'easy'::problem_difficulty,
    NULL,
    NULL,
    '{"python":"# Write your solution here"}'::jsonb
  ),
  (
    '685e0ebf-8932-4f66-b47f-c0e6abbd6909',
    'valid-parentheses',
    'Valid Parentheses',
    'Determine if the input string has valid pairs of parentheses, brackets, and braces.',
    'Single line string s.',
    'Print true or false.',
    '1 <= s.length <= 100000',
    'A stack is the intended approach.',
    'easy'::problem_difficulty,
    NULL,
    NULL,
    '{"javascript":"// Write your solution here"}'::jsonb
  ),
  (
    '7228c8cf-2d6e-4892-ba04-56fa97f95fbf',
    'coin-change',
    'Coin Change',
    'Given coin denominations and an amount, return the fewest number of coins needed to make that amount.',
    'First line: n. Second line: n coin values. Third line: amount.',
    'Print the minimum number of coins, or -1 if impossible.',
    '1 <= n <= 100, amount <= 10000',
    'Bottom-up dynamic programming works well.',
    'medium'::problem_difficulty,
    NULL,
    NULL,
    '{"java":"// Write your solution here"}'::jsonb
  ),
  (
    'f94349d6-4be3-4325-8ed5-b9a194df6028',
    'shortest-path-grid',
    'Shortest Path in Grid',
    'Given a matrix with blocked and free cells, find the shortest path from top-left to bottom-right.',
    'First line: rows cols. Next rows lines: grid values (0 free, 1 blocked).',
    'Print the shortest distance, or -1.',
    'rows, cols <= 500',
    'Breadth-first search with queue.',
    'hard'::problem_difficulty,
    NULL,
    NULL,
    '{"cpp":"// Write your solution here"}'::jsonb
  )
ON CONFLICT (slug) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, is_default)
SELECT p.id, l.id, CASE WHEN l.code = 'python' THEN TRUE ELSE FALSE END
FROM problems p
JOIN languages l ON l.code IN ('python', 'javascript', 'java', 'cpp')
WHERE p.slug = 'two-sum'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, is_default)
SELECT p.id, l.id, CASE WHEN l.code = 'javascript' THEN TRUE ELSE FALSE END
FROM problems p
JOIN languages l ON l.code IN ('python', 'javascript', 'java')
WHERE p.slug = 'valid-parentheses'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, is_default)
SELECT p.id, l.id, CASE WHEN l.code = 'java' THEN TRUE ELSE FALSE END
FROM problems p
JOIN languages l ON l.code IN ('python', 'java', 'cpp')
WHERE p.slug = 'coin-change'
ON CONFLICT (problem_id, language_id) DO NOTHING;

INSERT INTO problem_languages (problem_id, language_id, is_default)
SELECT p.id, l.id, CASE WHEN l.code = 'cpp' THEN TRUE ELSE FALSE END
FROM problems p
JOIN languages l ON l.code IN ('cpp', 'java')
WHERE p.slug = 'shortest-path-grid'
ON CONFLICT (problem_id, language_id) DO NOTHING;

ALTER TABLE test_cases
  ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample)
SELECT p.id, '4\n2 7 11 15\n9\n', '0 1\n', TRUE
FROM problems p
WHERE p.slug = 'two-sum'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id);

INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample)
SELECT p.id, '()[]{}', 'true\n', TRUE
FROM problems p
WHERE p.slug = 'valid-parentheses'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id);

INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample)
SELECT p.id, '3\n1 2 5\n11\n', '3\n', TRUE
FROM problems p
WHERE p.slug = 'coin-change'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id);

INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample)
SELECT p.id, '3 3\n0 0 0\n1 1 0\n0 0 0\n', '4\n', TRUE
FROM problems p
WHERE p.slug = 'shortest-path-grid'
  AND NOT EXISTS (SELECT 1 FROM test_cases tc WHERE tc.problem_id = p.id);

COMMIT;
