-- \i create.sql

-- @todo Add north-east, north-west etc for diagonal movements.
CREATE TYPE direction_type AS ENUM ('north', 'east', 'south', 'west');

CREATE TYPE tile_type AS ENUM ('land', 'water');

-- Tile connectors specify a connection type between two edges of adjacent
-- tiles, e.g. a tile with a riverlet on the south edge may only connect to
-- another tile with a riverlet on its adjacent north edge.
CREATE TABLE IF NOT EXISTS tile_connectors(
       id SERIAL PRIMARY KEY,
       code VARCHAR(32) NOT NULL UNIQUE,
       description VARCHAR(128));

-- @todo Move images into separate table.
CREATE TABLE IF NOT EXISTS tiles(
       id SERIAL PRIMARY KEY,
       tile_type tile_type NOT NULL,
       is_traverseable BOOLEAN NOT NULL,
       -- @todo If null then connects to anything (e.g. an overlay of a small
       -- pebble has no edges).
       -- There is a potential connector for each side of the tile.
       north_connector_id INTEGER REFERENCES tile_connectors,
       east_connector_id INTEGER REFERENCES tile_connectors,
       south_connector_id INTEGER REFERENCES tile_connectors,
       west_connector_id INTEGER REFERENCES tile_connectors,              
       code VARCHAR(32) NOT NULL UNIQUE,
       image BYTEA,
       description VARCHAR(128));

CREATE TYPE avatar_type AS ENUM ('user');

-- An avatar is a composite image or sprite of a user or a game creature.
-- @todo Move avatar images into separate table.
CREATE TABLE IF NOT EXISTS avatars(
       id SERIAL PRIMARY KEY,
       avatar_type avatar_type NOT NULL,
       -- Composite image containing 4 rows x 3 columns.
       -- The first row is frames of the avatar facing south.
       -- The second row is frames of the avatar facing west.
       -- The third row is frames of the avatar facing east.
       -- The fourth row is frames of the avatar facing north.
       --
       -- S1 S2 S3
       -- W1 W2 W3
       -- E1 E2 E3
       -- N1 N2 N3
       image BYTEA,
       description VARCHAR(128));

-- @todo Make table names singular?
-- @todo Change last_x, last_y, last_direction to simply x, y, direction.
CREATE TABLE IF NOT EXISTS users(
       id SERIAL PRIMARY KEY,
       created_time TIMESTAMP NOT NULL,
       last_seen_time TIMESTAMP NOT NULL,
       last_x INTEGER NOT NULL,
       last_y INTEGER NOT NULL,
       last_direction direction_type NOT NULL,
       avatar_name VARCHAR(64) NOT NULL,
       avatar_id INTEGER REFERENCES avatars,
       email VARCHAR(64),
       CHECK (last_seen_time >= created_time));

CREATE INDEX IF NOT EXISTS users_email_index ON users(email);

-- @todo Tokens are like passwords. We should store them hashed using
-- bcrypt or similar.
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

CREATE TABLE IF NOT EXISTS game_map(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       PRIMARY KEY (x, y),
       base_tile_id INTEGER REFERENCES tiles NOT NULL,
       overlay_tile1_id INTEGER REFERENCES tiles,
       overlay_tile2_id INTEGER REFERENCES tiles,
       overlay_tile3_id INTEGER REFERENCES tiles,
       is_traverseable BOOLEAN NOT NULL);

CREATE TABLE IF NOT EXISTS traversals(
       x INTEGER NOT NULL,
       y INTEGER NOT NULL,
       user_id INTEGER NOT NULL REFERENCES users,       
       PRIMARY KEY (x, y, user_id),
       has_seen BOOLEAN NOT NULL,
       has_visited BOOLEAN NOT NULL);
