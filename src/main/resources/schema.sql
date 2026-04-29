-- Bootstraps the PG schemas before Hibernate creates the tables.
-- ddl-auto: create-drop generates tables; it does not generate schemas.
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS territory;
CREATE SCHEMA IF NOT EXISTS geo;
CREATE SCHEMA IF NOT EXISTS ride;
CREATE SCHEMA IF NOT EXISTS matching;
