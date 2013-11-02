-- Create tables for example app (h2)

create table programmer (
  id bigserial not null primary key,
  name varchar(64) not null,
  favorite_number bigint not null,
  company_id bigint,
  created_timestamp timestamp not null,
  updated_timestamp timestamp,
  deleted_timestamp timestamp
);
    
create table company (
  id bigserial not null primary key,
  name varchar(64) not null,
  url varchar(128),
  created_at timestamp not null,
  updated_at timestamp,
  deleted_at timestamp
);
    
create table skill (
  id bigserial not null primary key,
  name varchar(64) not null
);
    
create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);

alter table programmer add foreign key(company_id) references company(id);
alter table programmer_skill add foreign key(skill_id) references skill(id);
alter table programmer_skill add foreign key(programmer_id) references programmer(id);

