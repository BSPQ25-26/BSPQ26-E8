BEGIN;

INSERT INTO languages (code, name, compile_cmd, run_cmd, version)
VALUES
  ('python', 'Python 3', NULL, 'python3 main.py', '3.12'),
  ('javascript', 'Node.js', NULL, 'node main.js', '22'),
  ('java', 'Java', 'javac Main.java', 'java Main', '21'),
  ('cpp', 'C++', 'g++ -std=c++20 -O2 -o main main.cpp', './main', 'GCC 13')
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
