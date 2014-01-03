-- For H2 Database
create table snake_case_key_examples (
  id bigserial not null primary key,
  first_name varchar(512) not null,
  luckey_number int not null,
  created_at timestamp not null,
  updated_at timestamp
)
