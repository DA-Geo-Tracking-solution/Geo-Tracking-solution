-- docker exec -it cassandra cqlsh -f /path/to/create_tables.cql

-- Create Keyspace
CREATE KEYSPACE IF NOT EXISTS geo_tracking_keyspace
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

-- Switch to Keyspace
USE geo_tracking_keyspace;

-- Create GPS Data Table
CREATE TABLE IF NOT EXISTS gps_data (
    user_email TEXT,
    timestamp TIMESTAMP,
    latitude FLOAT,
    longitude FLOAT,
    PRIMARY KEY (user_email, timestamp)
) WITH CLUSTERING ORDER BY (timestamp ASC);

-- Table: User by Squad
CREATE TABLE IF NOT EXISTS users_by_squad (
    squad_id uuid,
    user_email TEXT,
    PRIMARY KEY (squad_id, user_email)
);

-- Table: Messages by Chat
CREATE TABLE IF NOT EXISTS messages_by_chat (
    chat_id UUID,
    message_id UUID,
    author_email TEXT,
    content TEXT,
    timestamp TIMESTAMP,
    PRIMARY KEY (chat_id, timestamp, message_id)
) WITH CLUSTERING ORDER BY (timestamp ASC);

-- Table: Chats by User
CREATE TABLE IF NOT EXISTS chats_by_user (
    user_email TEXT,
    chat_id UUID,
    chat_name TEXT,
    PRIMARY KEY (user_email, chat_id)
);

-- Table: Users by Chat
CREATE TABLE IF NOT EXISTS users_by_chat (
    chat_id UUID,
    user_email TEXT,
    PRIMARY KEY (chat_id, user_email)
);


-- Insert into GPS Data Table
INSERT INTO gps_data (user_email, timestamp, latitude, longitude)
VALUES ('felix@example.com', '2024-12-01 10:00:00', 40.7128, -74.0060);

INSERT INTO gps_data (user_email, timestamp, latitude, longitude)
VALUES ('felix@example.com', '2024-12-01 10:05:00', 40.7138, -74.0070);

INSERT INTO gps_data (user_email, timestamp, latitude, longitude)
VALUES ('felix@example.com', '2024-12-01 10:10:00', 40.7148, -74.0080);

INSERT INTO gps_data (user_email, timestamp, latitude, longitude)
VALUES ('adrian@example', '2023-12-01 10:00:00', 34.0522, -118.2437);

INSERT INTO gps_data (user_email, timestamp, latitude, longitude)
VALUES ('adrian@example', '2024-12-01 10:05:00', 34.0532, -118.2447);


-- Insert into User by Squad Table
INSERT INTO users_by_squad (squad_id, user_email)
VALUES (11111111-1111-1111-1111-111111111111, 'felix@example.com');

INSERT INTO users_by_squad (squad_id, user_email)
VALUES (11111111-1111-1111-1111-111111111111, 'adrian@example.com');

INSERT INTO users_by_squad (squad_id, user_email)
VALUES (22222222-2222-2222-2222-222222222222, 'jonas@example.com');

INSERT INTO users_by_squad (squad_id, user_email)
VALUES (33333333-3333-3333-3333-333333333333, 'benjamin@example.com');



-- Insert into Messages by Chat Table
INSERT INTO messages_by_chat (chat_id, message_id, author_email, content, timestamp)
VALUES (11111111-1111-1111-1111-111111111111, uuid(), 'felix@example.com', 'Hello, how are you?', '2024-12-01 10:00:00');

INSERT INTO messages_by_chat (chat_id, message_id, author_email, content, timestamp)
VALUES (11111111-1111-1111-1111-111111111111, uuid(), 'adrian@example.com', 'I am good, thanks! And you?', '2024-12-01 10:05:00');

INSERT INTO messages_by_chat (chat_id, message_id, author_email, content, timestamp)
VALUES (11111111-1111-1111-1111-111111111111, uuid(), 'felix@example.com', 'I am doing well, thanks for asking!', '2024-12-01 10:10:00');


-- Insert into Chats by User Table
INSERT INTO chats_by_user (user_email, chat_id, chat_name)
VALUES ('felix@example.com', 11111111-1111-1111-1111-111111111111, 'Group Chat A');

INSERT INTO chats_by_user (user_email, chat_id, chat_name)
VALUES ('adrian@example.com', 11111111-1111-1111-1111-111111111111, 'Group Chat A');

INSERT INTO chats_by_user (user_email, chat_id, chat_name)
VALUES ('felix@example.com', 22222222-2222-2222-2222-222222222222, 'Group Chat B');

INSERT INTO chats_by_user (user_email, chat_id, chat_name)
VALUES ('benjamin@example.com', 33333333-3333-3333-3333-333333333333, 'Group Chat B');


-- Insert into Users by Chat Table
INSERT INTO users_by_chat (chat_id, user_email)
VALUES (11111111-1111-1111-1111-111111111111, 'felix@example.com');

INSERT INTO users_by_chat (chat_id, user_email)
VALUES (11111111-1111-1111-1111-111111111111, 'adrian@example.com');

INSERT INTO users_by_chat (chat_id, user_email)
VALUES (22222222-2222-2222-2222-222222222222, 'felix@example.com');

INSERT INTO users_by_chat (chat_id, user_email)
VALUES (33333333-3333-3333-3333-333333333333, 'benjamin@example.com');


