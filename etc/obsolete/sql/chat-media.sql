---------------------------------------------------------- TABLE chat_info_site

CREATE SEQUENCE chat_info_site_id_seq;

CREATE TABLE chat_info_site (

	id int PRIMARY KEY DEFAULT nextval ('chat_info_site_id_seq'),

	chat_user_id int NOT NULL REFERENCES chat_user,

	create_time timestamp with time zone NOT NULL,
	expire_time timestamp with time zone NOT NULL,

	first_view_time timestamp with time zone,
	last_view_time timestamp with time zone,
	num_views int NOT NULL DEFAULT 0,

	last_expired_time timestamp with time zone,
	num_expired int NOT NULL DEFAULT 0,

	token text NOT NULL
);

CREATE TABLE chat_info_site_user (

	chat_info_site_id int NOT NULL REFERENCES chat_info_site,
	index int NOT NULL,

	PRIMARY KEY (chat_info_site_id, index),

	chat_user_id int NOT NULL REFERENCES chat_user

);
