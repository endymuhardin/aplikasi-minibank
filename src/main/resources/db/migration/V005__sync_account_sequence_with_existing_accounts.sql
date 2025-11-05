-- Sinkronkan sequence ACCOUNT_NUMBER dengan data accounts yang sudah ada
UPDATE sequence_numbers sn
SET last_number = GREATEST(
  sn.last_number,
  COALESCE((
    SELECT MAX( (regexp_replace(a.account_number, '^[A-Za-z]+', '') )::bigint )
    FROM accounts a
    WHERE a.account_number LIKE COALESCE(sn.prefix, '') || '%'
  ), sn.last_number)
)
WHERE sn.sequence_name = 'ACCOUNT_NUMBER';
