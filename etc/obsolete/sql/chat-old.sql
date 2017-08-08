---------------------------------------- TABLE chat_user_credit_limit_log

CREATE SEQUENCE chat_user_credit_limit_log_id_seq;

CREATE TABLE chat_user_credit_limit_log (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_credit_limit_log_id_seq'),
	chat_user_id int NOT NULL REFERENCES chat_user,
	timestamp timestamp with time zone NOT NULL,
	old_credit_limit int NOT NULL,
	new_credit_limit int NOT NULL
);

---------------------------------------- TABLE chat_user_spend

CREATE SEQUENCE chat_user_spend_id_seq;

CREATE TABLE chat_user_spend (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_spend_id_seq'),
	chat_user_id int NOT NULL REFERENCES chat_user,
	date date NOT NULL,
	UNIQUE (chat_user_id, date),

	user_message_count int NOT NULL DEFAULT 0,
	user_message_charge int NOT NULL DEFAULT 0,
	monitor_message_count int NOT NULL DEFAULT 0,
	monitor_message_charge int NOT NULL DEFAULT 0,
	text_profile_count int NOT NULL DEFAULT 0,
	text_profile_charge int NOT NULL DEFAULT 0,
	image_profile_count int NOT NULL DEFAULT 0,
	image_profile_charge int NOT NULL DEFAULT 0
);

---------------------------------------- TABLE chat_user_image

CREATE SEQUENCE chat_user_image_id_seq;

CREATE TABLE chat_user_image (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_image_id_seq'),
	chat_user_id int NOT NULL,
	type text NOT NULL CHECK (text IN ('photo', 'video')),
	index int NOT NULL,
	media_id int NOT NULL,
	timestamp timestamp with time zone NOT NULL,
	message_id int,
	user_id int,
	status int NOT NULL,
	moderation_timestamp timestamp with time zone
);

CREATE UNIQUE INDEX chat_user_image_index
ON chat_user_image (chat_user_id, type, index);

---------------------------------------- TABLE chat_block

CREATE SEQUENCE chat_block_id_seq;

CREATE TABLE chat_block (
	id int PRIMARY KEY DEFAULT nextval ('chat_block_id_seq'),
	chat_user_id int NOT NULL REFERENCES chat_user,
	blocked_chat_user_id int NOT NULL REFERENCES chat_user,
	UNIQUE (chat_user_id, blocked_chat_user_id),
	timestamp timestamp with time zone NOT NULL DEFAULT now ()
);

---------------------------------------- TABLE chat_ad_template

CREATE SEQUENCE chat_ad_template_id_seq;

CREATE TABLE chat_ad_template (
	id int PRIMARY KEY DEFAULT nextval ('chat_ad_template_id_seq'),
	chat_id int NOT NULL,
	generic_text_id int NOT NULL,
	gay_male_text_id int NOT NULL,
	gay_female_text_id int NOT NULL
);

---------------------------------------- TABLE chat_intro

CREATE SEQUENCE chat_intro_id_seq;

CREATE TABLE chat_intro (
	id int PRIMARY KEY DEFAULT nextval ('chat_intro_id_seq'),
	from_user_id int NOT NULL REFERENCES chat_user,
	to_user_id int NOT NULL REFERENCES chat_user,
	timestamp timestamp with time zone NOT NULL DEFAULT now (),
	thread_id int NOT NULL REFERENCES message,
	text text NOT NULL
);

---------------------------------------- TABLE chat_hint

CREATE SEQUENCE chat_hint_id_seq;

CREATE TABLE chat_hint (
	id int PRIMARY KEY DEFAULT nextval ('chat_hint_id_seq'),
	chat_id int NOT NULL REFERENCES chat,
	send_if_online boolean NOT NULL DEFAULT true,
	send_if_offline boolean NOT NULL DEFAULT true,
	text text NOT NULL DEFAULT ''
);

---------------------------------------- TABLE chat_hint_log

CREATE SEQUENCE chat_hint_log_id_seq;

CREATE TABLE chat_hint_log (
	id int PRIMARY KEY DEFAULT nextval ('chat_hint_log_id_seq'),
	hint_id int NOT NULL REFERENCES chat_hint,
	user_id int NOT NULL REFERENCES chat_user,
	UNIQUE (hint_id, user_id),
	timestamp timestamp with time zone NOT NULL DEFAULT now ()
);

---------------------------------------- VIEW chat_user_contacts_view

CREATE VIEW chat_user_contacts_view (
	my_user_id,
	his_user_id,
	last_info_from,
	last_message_from,
	last_info_to,
	last_message_to)

AS SELECT
	my_user.id,
	his_user.id,
	contact_from.last_info,
	contact_from.last_message,
	contact_to.last_info,
	contact_to.last_message

FROM chat_user AS my_user

INNER JOIN chat_user AS his_user
	ON my_user.chat_id = his_user.chat_id
		AND his_user.online

LEFT JOIN chat_user_contact AS contact_from
	ON contact_from.from_user_id = his_user.id
		AND contact_from.to_user_id = my_user.id

LEFT JOIN chat_user_contact AS contact_to
	ON contact_to.to_user_id = his_user.id
		AND contact_to.from_user_id = my_user.id;

---------------------------------------- TABLE chat_system_template

CREATE SEQUENCE chat_system_template_id_seq;

CREATE TABLE chat_system_template (
	id int PRIMARY KEY DEFAULT nextval ('chat_system_template_id_seq'),
	chat_id int NOT NULL,

);

------------------------------------------------------------ TABLE chat_user_info

CREATE SEQUENCE chat_user_info_id_seq;

CREATE TABLE chat_user_info (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_info_id_seq'),
	chat_user_id int,
	timestamp timestamp with time zone NOT NULL,
	text_id int NOT NULL,
	edited_text_id int,
	user_id int,
	status int NOT NULL,
	moderation_timestamp timestamp with time zone,
	thread_id int,
	edit_reason int
);

CREATE INDEX chat_user_info_chat_user_id ON chat_user_info (chat_user_id);

------------------------------------------------------------ TABLE chat_user_name

CREATE SEQUENCE chat_user_name_id_seq;

CREATE TABLE chat_user_name (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_name_id_seq'),
	chat_user_id int,
	name text,
	edited_name text,
	timestamp timestamp with time zone NOT NULL,
	user_id int,
	status int NOT NULL,
	moderation_timestamp timestamp with time zone,
	thread_id int,
	edit_reason int
);

CREATE INDEX chat_user_name_chat_user_id ON chat_user_info (chat_user_id);

------------------------------------------------------------ TABLE chat_approval_regexp

CREATE SEQUENCE chat_approval_regexp_id_seq;

CREATE TABLE chat_approval_regexp (
	id int PRIMARY KEY DEFAULT nextval ('chat_approval_regexp_id_seq'),
	chat_id int NOT NULL,
	regexp text NOT NULL,
	auto bool NOT NULL
);

------------------------------------------------------------ TABLE chat_user_credit

CREATE SEQUENCE chat_user_credit_id_seq;

CREATE TABLE chat_user_credit (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_credit_id_seq'),
	chat_user_id int NOT NULL,
	amount int NOT NULL,
	timestamp timestamp with time zone NOT NULL DEFAULT now (),
	user_id int NOT NULL,
	gift bool NOT NULL,
	details text NOT NULL
);

------------------------------------------------------------ TABLE chat_user_date_log

CREATE SEQUENCE chat_user_date_log_id_seq;

CREATE TABLE chat_user_date_log (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_date_id_seq'),
	chat_user_id int NOT NULL,
	user_id int,
	message_id int,
	date_mode int,
	radius int NOT NULL,
	start_hour int NOT NULL,
	end_hour int NOT NULL,
	daily_max int NOT NULL
);

------------------------------------------------------------ TABLE chat_user_dob_failure

CREATE SEQUENCE chat_user_dob_failure_id_seq;

CREATE TABLE chat_user_dob_failure (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_dob_failure_id_seq'),
	chat_user_id int NOT NULL,
	message_id int NOT NULL,
	timestamp timestamp with time zone NOT NULL DEFAULT now (),
	failing_text_id int NOT NULL
);

------------------------------------------------------------ TABLE chat_user_bill_log

CREATE SEQUENCE chat_user_bill_log_id_seq;

CREATE TABLE chat_user_bill_log (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_bill_log_id_seq'),
	chat_user_id int NOT NULL REFERENCES chat_user,
	timestamp timestamp with time zone NOT NULL DEFAULT now (),
	user_id int NOT NULL REFERENCES conuser
);

CREATE INDEX chat_user_bill_log_chat_user_id ON chat_user_bill_log (chat_user_id, timestamp);

---------------------------------------- FUNCTION chat_profile_field

CREATE SEQUENCE chat_profile_field_id_seq;

CREATE TABLE chat_profile_field (
	id int PRIMARY KEY DEFAULT nextval ('chat_profile_field_id_seq'),
	chat_id int NOT NULL REFERENCES chat,
	code text NOT NULL,
	UNIQUE (chat_id, code)
);

---------------------------------------- FUNCTION chat_profile_field_value

CREATE SEQUENCE chat_profile_field_value_id_seq;

CREATE TABLE chat_profile_field_value (
	id int PRIMARY KEY DEFAULT nextval ('chat_profile_field_value_id_seq'),
	chat_profile_field_id int NOT NULL REFERENCES chat_profile_field,
	code text NOT NULL,
	UNIQUE (chat_profile_field_id, code)
);

INSERT INTO chat_profile_field (chat_id, code) SELECT id, 'looking_for_meet' FROM chat;
INSERT INTO chat_profile_field (chat_id, code) SELECT id, 'looking_for_casual_fun' FROM chat;
INSERT INTO chat_profile_field (chat_id, code) SELECT id, 'looking_for_relationship' FROM chat;
INSERT INTO chat_profile_field (chat_id, code) SELECT id, 'looking_for_friendship' FROM chat;
INSERT INTO chat_profile_field (chat_id, code) SELECT id, 'looking_for_no_strings' FROM chat;

INSERT INTO chat_profile_field_value (chat_profile_field_id, code) SELECT id, 'true' FROM chat_profile_field;
INSERT INTO chat_profile_field_value (chat_profile_field_id, code) SELECT id, 'false' FROM chat_profile_field;

---------------------------------------- FUNCTION chat_user_profile_field

CREATE SEQUENCE chat_user_profile_field_id_seq;

CREATE TABLE chat_user_profile_field (
	id int PRIMARY KEY DEFAULT nextval ('chat_user_profile_field_id_seq'),
	chat_user_id int NOT NULL REFERENCES chat_user,
	chat_profile_field_id int NOT NULL REFERENCES chat_profile_field,
	UNIQUE (chat_user_id, chat_profile_field_id),
	chat_profile_field_value_id int NOT NULL REFERENCES chat_profile_field_value
);

------------------------------------------------------------ INSERT object_type

SELECT object_type_insert ('chat', 'Chat service', 'txt2.chat.db.Chat');

SELECT container_insert ('chat', 'Chat services');

SELECT object_type_insert (
	'chat_keyword',
	'Chat keyword',
	'chat',
	2);

---------------------------------------- INSERT rechandler

SELECT rechandler_insert ('chat', 'txt2.chat.daemon.ChatReceivedHandler');

---------------------------------------- INSERT service_type

SELECT service_type_insert ('chat', 'default', 'User to user chat service');
SELECT service_type_insert ('chat', 'chat', 'User to user chat');
SELECT service_type_insert ('chat', 'monitor', 'User to monitor chat');
SELECT service_type_insert ('chat', 'help', 'User to help chat');
SELECT service_type_insert ('chat', 'ad', 'Automated ads');
SELECT service_type_insert ('chat', 'online_text', 'Online profiles (text)');
SELECT service_type_insert ('chat', 'online_image', 'Online profile (image)');
SELECT service_type_insert ('chat', 'online_video', 'Online profile (video)');
SELECT service_type_insert ('chat', 'date_text', 'Dating profile (text)');
SELECT service_type_insert ('chat', 'date_image', 'Dating profile (image)');
SELECT service_type_insert ('chat', 'date_video', 'Dating profile (video)');
SELECT service_type_insert ('chat', 'info_site', 'Online profiles via WAP');

---------------------------------------- INSERT command_type

SELECT command_type_insert ('chat', 'main', 'Main shortcode command');

SELECT command_type_insert ('chat', 'chat', 'User-to-user chat (magic!)');
SELECT command_type_insert ('chat', 'magic', 'Magic proxy (magic!)');

SELECT command_type_insert ('chat', 'name', 'Change name');
SELECT command_type_insert ('chat', 'help', 'Contact help');
SELECT command_type_insert ('chat', 'block', 'Block a user');
SELECT command_type_insert ('chat', 'logoff', 'Log off');
SELECT command_type_insert ('chat', 'block_all', 'Block all messages');
SELECT command_type_insert ('chat', 'set_photo', 'Set my photo');
SELECT command_type_insert ('chat', 'video_get', 'Get video for user');
SELECT command_type_insert ('chat', 'video_set', 'Set own video');

SELECT command_type_insert ('chat', 'join_info', 'Join and/or change info');
SELECT command_type_insert ('chat', 'join_age', 'Confirm age');
SELECT command_type_insert ('chat', 'join_location', 'Choose location');
SELECT command_type_insert ('chat', 'join_prefs', 'Set orientation and gender preferences');
SELECT command_type_insert ('chat', 'join_next', 'Get more users online');
SELECT command_type_insert ('chat', 'join_dob', 'Join and set DOB');
SELECT command_type_insert ('chat', 'join_charges', 'Join and confirm charges');
SELECT command_type_insert ('chat', 'join_pics', 'Join and send pics');
SELECT command_type_insert ('chat', 'join_videos', 'Join and send videos');

SELECT command_type_insert ('chat', 'chat_gay_male', 'Join and set prefs as gay male');
SELECT command_type_insert ('chat', 'chat_gay_female', 'Join and set prefs as gay female');
SELECT command_type_insert ('chat', 'chat_straight_male', 'Join and set prefs as straight male');
SELECT command_type_insert ('chat', 'chat_straight_female', 'Join and set prefs as straight female');
SELECT command_type_insert ('chat', 'chat_bi_male', 'Join and set prefs as bi male');
SELECT command_type_insert ('chat', 'chat_bi_female', 'Join and set prefs as bi female');

SELECT command_type_insert ('chat', 'date_join_text', 'Join text dating');
SELECT command_type_insert ('chat', 'date_join_photo', 'Join photo dating');
SELECT command_type_insert ('chat', 'date_join_videos', 'Join video dating');
SELECT command_type_insert ('chat', 'date_upgrade', 'Upgrade to photo dating');

SELECT command_type_insert ('chat_affiliate', 'date_set_photo', 'Join dating and set photo');

SELECT command_type_insert ('chat_scheme' 'chat_scheme', 'Main shortcode command');
SELECT command_type_insert ('chat_scheme', 'photo_set', 'Set own photo');

SELECT command_type_insert ('chat_scheme', 'chat_info', 'Join chat and set info');
SELECT command_type_insert ('chat_scheme', 'chat_dob', 'Join chat and set date of birth');
SELECT command_type_insert ('chat_scheme', 'chat_charges', 'Join chat and confirm charges');
SELECT command_type_insert ('chat_scheme', 'chat_location', 'Join chat and set location');
SELECT command_type_insert ('chat_scheme', 'chat_gender', 'Join chat and set gender');
SELECT command_type_insert ('chat_scheme', 'chat_gender_other', 'Join chat and set preferred gender');
SELECT command_type_insert ('chat_scheme', 'chat_next', 'Join chat and receive more users');
SELECT command_type_insert ('chat_scheme', 'chat_pics', 'Join chat and receive more user photos');

SELECT command_type_insert ('chat_scheme', 'date_dob', 'Join dating and set date of birth');
SELECT command_type_insert ('chat_scheme', 'date_charges', 'Join dating and confirm charges');
SELECT command_type_insert ('chat_scheme', 'date_location', 'Join dating and set location');
SELECT command_type_insert ('chat_scheme', 'date_gender', 'Join dating and set gender');
SELECT command_type_insert ('chat_scheme', 'date_gender_other', 'Join dating and set other gender');
SELECT command_type_insert ('chat_scheme', 'date_info', 'Join dating and set info');
SELECT command_type_insert ('chat_scheme', 'date_stop', 'Stop dating');

---------------------------------------- INSERT priv_group, chat

SELECT priv_group_insert (
	'chat',
	'Chat services',
	'Full access to this chat service and all stats and settings.');

SELECT priv_group_insert (
	'chat',
	'view',
	'View chat service',
	'View summary for and basic details about this chat service.',
	true);

SELECT priv_group_insert (
	'chat_help',
	'Chat services, answer help requests',
	'Answer help requests from chat users.');

SELECT priv_group_insert (
	'chat_history',
	'Chat services, view message history',
	'View and search full user-to-user message histories.');

SELECT priv_group_insert (
	'chat_number',
	'Chat services, view mobile numbers',
	'View users'' mobile numbers on various screens.');

SELECT priv_group_insert (
	'chat_stats',
	'Chat services, view statistics',
	'View systems statistics, including affiliate statistics.');

SELECT priv_group_insert (
	'chat_supervisor',
	'Chat services, supervisor',
	'View console user activity.');

SELECT priv_group_insert (
	'chat',
	'monitor',
	'Chat services, manage monitors',
	'View add and update monitors for the given chat service.',
	true);

SELECT priv_group_insert (
	'chat',
	'credit_update',
	'View and update users credit',
	'View and administrate users credit and credit mode, including crediting and debiting.',
	true);

SELECT priv_group_insert (
	'chat',
	'user_create',
	'Create new users',
	'Create new users (monitors)',
	true);

SELECT priv_group_insert (
	'chat',
	'user_view',
	'Search and view users',
	'Search through and view (most) information for all users',
	true);

SELECT priv_group_insert (
	'chat',
	'user_admin',
	'Administrate users',
	'Update users info, bring them on and off line etc...',
	true);

SELECT priv_group_insert (
	'chat',
	'approve',
	'Approvals',
	'Approve or reject users mesasges, names, infos, photos etc.',
	true);

SELECT priv_group_insert (
	'chat',
	'gay_male',
	'Chat as gay male',
	'Chat to users as a gay male.',
	true);

SELECT priv_group_insert (
	'chat',
	'gay_female',
	'Chat as gay female',
	'Chat to users as a gay female.',
	true);

SELECT priv_group_insert (
	'chat',
	'straight_male',
	'Chat as straight male',
	'Chat to users as a straight male.',
	true);

SELECT priv_group_insert (
	'chat',
	'straight_female',
	'Chat services, chat as straight female',
	'Chat to users as a straight female',
	true);

SELECT priv_type_insert (
	'chat',
	'unknown',
	'Chat services, chat as unknown',
	'Chat to users where gender/orient is unknown',
	true);

SELECT priv_type_insert (
	'chat',
	'affiliate_create',
	'Create affiliates in all schemes',
	'',
	true);

---------------------------------------- INSERT priv_group, chat

SELECT priv_group_insert ('chat_affiliate', 'view', 'View chat affiliate',
	'', true);

SELECT priv_group_insert ('chat_affiliate', 'user_view', 'View chat users belonging to affiliate',
	'', true);

SELECT priv_group_insert ('chat_affiliate', 'messages', 'View messages relating to affiliate',
	'', true);

SELECT priv_group_insert ('chat_affiliate', 'stats', 'View stats relating to affiliate',
	'', true);

---------------------------------------- INSERT menu

SELECT menu_insert ('facility', 'chat', 'Chat services', '/chat/chat_list');

---------------------------------------- INSERT queue_type

SELECT queue_type_insert (
	'chat',
	'chat_help',
	'Chat services, user help requests');

SELECT queue_type_insert (
	'chat',
	'chat_gay_male',
	'Chat services, gay male');

SELECT queue_type_insert (
	'chat',
	'chat_gay_female',
	'Chat services, gay female';

SELECT queue_type_insert (
	'chat',
	'chat_straight_male',
	'Chat services, straight male');

SELECT queue_type_insert (
	'chat',
	'chat_straight_female',
	'Chat services, straight female');

SELECT queue_type_insert (
	'chat',
	'unknown',
	'Chat services, unknown gender/orient');

SELECT queue_type_insert (
	'chat',
	'chat_gay_male',
	'Chat services, gay male');

SELECT queue_type_insert ('chat', 'chat_gay_female', 'Chat services, gay female');
SELECT queue_type_insert ('chat', 'chat_straight_male', 'Chat services, straight male');
SELECT queue_type_insert ('chat', 'chat_straight_female', 'Chat services, straight female');
SELECT queue_type_insert ('chat', 'unknown', 'Chat services, unknown gender/orient');

SELECT queue_type_insert ('chat', 'chat_gay_male_alarm', 'Chat services, gay male alarm');
SELECT queue_type_insert ('chat', 'chat_gay_female_alarm', 'Chat services, gay female alarm');
SELECT queue_type_insert ('chat', 'chat_straight_male_alarm', 'Chat services, straight male alarm');
SELECT queue_type_insert ('chat', 'chat_straight_female_alarm', 'Chat services, straight female alarm');
SELECT queue_type_insert ('chat', 'chat_unknown_alarm', 'Chat services, unknown gender/orient alarm');

SELECT queue_type_insert ('chat', 'chat_user', 'Chat services, approve user info');
SELECT queue_type_insert ('chat', 'chat_message', 'Chat services, approve message');

---------------------------------------- INSERT affiliate_type


---------------------------------------- INSERT console_path

SELECT console_path_insert ('/chat', 'txt2.chat.console.ChatPathHandler');

------------------------------------------------------------ INSERT event_type (chat_scheme)

SELECT event_type_insert ('chat_scheme_code',
	'%0 changed code for chat scheme %1 from %2 to %3');

SELECT event_type_insert ('chat_scheme_description',
	'%0 set description for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_rb_bill_route',
	'%0 set RB bill route for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_rb_bill_route_null',
	'%0 unset RB bill route for chat scheme %1');

SELECT event_type_insert ('chat_scheme_rb_free_router',
	'%0 set RB free router for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_rb_free_router_null',
	'%0 unset RB free route for chat scheme %1');

SELECT event_type_insert ('chat_scheme_rb_number',
	'%0 set RB number for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_magic_router',
	'%0 set magic router for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_magic_router_null',
	'%0 unset magic router for chat scheme %1');

SELECT event_type_insert ('chat_scheme_mms_route',
	'%0 set MMS route for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_mms_route_null',
	'%0 unset MMS route for chat scheme %1');

SELECT event_type_insert ('chat_scheme_mms_free_router',
	'%0 set MMS free router for chat scheme %1 to %2');

SELECT event_type_insert ('chat_scheme_mms_free_router_null',
	'%0 unset MMS free router for chat scheme %1');

SELECT event_type_insert ('chat_scheme_mms_free_number',
	'%0 set MMS number for chat scheme %1 to %2');

SELECT event_type_insert ('chat_user_dob',
	'%0 set dob for chat user %1 to %2');

---------------------------------------- INSERT token_domain

SELECT token_domain_insert ('gaytxt_user', 'GayTxt user IDs');

SELECT token_domain_entry_type_insert ('gaytxt_user', 'normal', 'Regular GayTxt user',
	(SELECT id FROM object_type WHERE code = 'chat'),
	(SELECT id FROM chat WHERE code = 'uk'));
