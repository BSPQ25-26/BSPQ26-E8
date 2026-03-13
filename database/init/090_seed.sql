BEGIN;

INSERT INTO languages (code, name, compile_cmd, run_cmd)
VALUES
  ('python', 'Python 3', NULL, 'python3 main.py'),
  ('javascript', 'Node.js', NULL, 'node main.js'),
  ('java', 'Java', 'javac Main.java', 'java Main'),
  ('cpp', 'C++', 'g++ -std=c++20 -O2 -o main main.cpp', './main')
ON CONFLICT (code) DO NOTHING;

INSERT INTO tags (slug, name)
VALUES
  ('array', 'Array'),
  ('string', 'String'),
  ('hash-table', 'Hash Table'),
  ('dynamic-programming', 'Dynamic Programming'),
  ('graph', 'Graph')
ON CONFLICT (slug) DO NOTHING;

COMMIT;
