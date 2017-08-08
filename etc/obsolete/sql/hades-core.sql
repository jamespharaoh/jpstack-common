
---------------------------------------- slice apn

INSERT INTO slice (code, description) VALUES ('apn', 'APN');

---------------------------------------- default user james

INSERT INTO conuser (slice_id, username, password, active, fullname)
VALUES (slice_id ('apn'), 'james', 'RgOBe8hQeUwTPbxd03YaeIdw4qc=', 't', 'James Pharaoh');

---------------------------------------- default user privs

INSERT INTO user_priv (priv_id, user_id, can, can_grant) VALUES (
	(SELECT id FROM priv WHERE parent_object_type_id = 0 AND parent_object_id = 0 AND my_code = 'manage'),
	(SELECT conuserid FROM conuser WHERE slice_id = slice_id ('apn') AND username = 'james'),
	't', 't'
);
