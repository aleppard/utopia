-- \i create.sql

CREATE TABLE users(
       id SERIAL PRIMARY KEY,
       created_time TIMESTAMP NOT NULL,
       last_seen_time TIMESTAMP NOT NULL,
       last_x INTEGER NOT NULL,
       last_y INTEGER NOT NULL,
       last_z INTEGER NOT NULL,
       avatar_name VARCHAR(64) NOT NULL,
       email VARCHAR(64),
       CHECK (last_seen_time >= created_time));

CREATE INDEX users_email_index ON users(email);

CREATE TABLE tokens(
       id SERIAL PRIMARY KEY,
       created_time TIMESTAMP NOT NULL,
       token VARCHAR(64) NOT NULL,
       user_id INTEGER REFERENCES users);

CREATE INDEX tokens_token_index ON tokens(token);

CREATE TYPE tile_enum AS ENUM ('land', 'water');

CREATE TABLE tiles(
       id SERIAL PRIMARY KEY,
       tile_number INTEGER NOT NULL,
       type_type tile_enum NOT NULL,
       is_traverseable BOOLEAN NOT NULL,
       is_opauqe BOOLEAN NOT NULL,
       description VARCHAR(128));

CREATE TABLE tile_sets(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       z INTEGER NOT NULL,
       PRIMARY KEY (x, y, z),
       tile_ids INTEGER[8][8] NOT NULL);

CREATE TABLE user_traversals(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       z INTEGER NOT NULL,
       user_id INTEGER NOT NULL REFERENCES users,       
       PRIMARY KEY (x, y, z, user_id),
       has_seen BOOLEAN[8][8] NOT NULL,
       has_visited BOOLEAN[8][8] NOT NULL);     
