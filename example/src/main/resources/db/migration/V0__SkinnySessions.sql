create table skinny_sessions (
  id bigserial not null primary key,
  created_at timestamp not null,
  expire_at timestamp not null
);

create table servlet_sessions (
  jsession_id varchar(32) not null primary key,
  skinny_session_id bigint not null,
  foreign key(skinny_session_id) references skinny_sessions(id)
);

create table skinny_session_attributes (
  skinny_session_id bigint not null,
  attribute_name varchar(128) not null,
  attribute_value varbinary(10485760),
  foreign key(skinny_session_id) references skinny_sessions(id)
);

alter table skinny_session_attributes add constraint
  skinny_session_attributes_unique_idx
  unique(skinny_session_id, attribute_name);
