CREATE OR REPLACE FUNCTION nowz (
) RETURNS text AS $$

	SELECT to_char (
		now () at time zone 'UTC',
		'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"');

$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION isoz (
	timestamp with time zone
) RETURNS text AS $$

	SELECT to_char (
		$1 at time zone 'UTC',
		'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"');

$$ LANGUAGE SQL;
