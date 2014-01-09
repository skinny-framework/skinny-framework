-- For H2 Database
create table comments (
  id bigserial not null primary key,
  author varchar(512) not null,
  text varchar(1024) not null)
