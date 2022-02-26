-- \i create.sql

CREATE TABLE IF NOT EXISTS users(
       id SERIAL PRIMARY KEY,
       created_time TIMESTAMP NOT NULL,
       last_seen_time TIMESTAMP NOT NULL,
       last_x INTEGER NOT NULL,
       last_y INTEGER NOT NULL,
       avatar_name VARCHAR(64) NOT NULL,
       email VARCHAR(64),
       CHECK (last_seen_time >= created_time));

CREATE INDEX IF NOT EXISTS users_email_index ON users(email);

CREATE TABLE IF NOT EXISTS tokens(
       id SERIAL PRIMARY KEY,
       created_time TIMESTAMP NOT NULL,
       token VARCHAR(64) NOT NULL,
       user_id INTEGER REFERENCES users);

CREATE INDEX IF NOT EXISTS tokens_token_index ON tokens(token);

CREATE TABLE IF NOT EXISTS items(
       id SERIAL PRIMARY KEY,
       item_name VARCHAR(32) NOT NULL UNIQUE,
       description VARCHAR(256));

CREATE TABLE IF NOT EXISTS inventory(
       id SERIAL PRIMARY KEY,
       item_id INTEGER REFERENCES items NOT NULL,
       user_id INTEGER REFERENCES users NOT NULL,
       item_count INTEGER NOT NULL);

CREATE TYPE tile_enum AS ENUM ('land', 'water');

-- @todo Move tile images into separate table.
CREATE TABLE IF NOT EXISTS tiles(
       id SERIAL PRIMARY KEY,
       tile_type tile_enum NOT NULL,
       is_traverseable BOOLEAN NOT NULL,
       code VARCHAR(4) NOT NULL UNIQUE,
       image BYTEA,
       description VARCHAR(128));

CREATE TABLE IF NOT EXISTS game_map(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       PRIMARY KEY (x, y),
       base_tile_id INTEGER REFERENCES tiles NOT NULL,
       overlay_tile1_id INTEGER REFERENCES tiles,
       overlay_tile2_id INTEGER REFERENCES tiles,
       overlay_tile3_id INTEGER REFERENCES tiles,
       is_traverseable BOOLEAN NOT NULL);

CREATE TABLE IF NOT EXISTS user_traversals(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       user_id INTEGER NOT NULL REFERENCES users,       
       PRIMARY KEY (x, y, user_id),
       has_seen BOOLEAN NOT NULL,
       has_visited BOOLEAN NOT NULL);
