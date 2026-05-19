BEGIN;

UPDATE test_cases
SET
  input_data = replace(input_data, chr(92) || 'n', chr(10)),
  expected_output = replace(expected_output, chr(92) || 'n', chr(10))
WHERE input_data LIKE '%' || chr(92) || 'n' || '%'
   OR expected_output LIKE '%' || chr(92) || 'n' || '%';

COMMIT;
