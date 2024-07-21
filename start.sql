CREATE TABLE IF NOT EXISTS users(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users(id, email, name, password) VALUES
(1, 'andrio@email.com', 'Andrio', "$2a$12$VzVdmAZWmRZCRDKCxFisG.zhm9xzcL9Br84dXDG2y7TccV3DYO2IK"),
(2, 'dodo@email.com', 'Dodo', "$2a$12$VzVdmAZWmRZCRDKCxFisG.zhm9xzcL9Br84dXDG2y7TccV3DYO2IK"),
(3, 'dudu@email.com', 'Dudu', "$2a$12$VzVdmAZWmRZCRDKCxFisG.zhm9xzcL9Br84dXDG2y7TccV3DYO2IK");

CREATE TABLE IF NOT EXISTS todos(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    status SMALLINT DEFAULT 0 COMMENT '0: Not started, 1: In progress, 2: Completed',
    is_deleted BOOLEAN DEFAULT 0,
    priority INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todos(id, uuid, name, description, due_date, status, is_deleted, priority) VALUES
(1, 'fcdc097f-e7b3-4d00-a6e4-14ec313e3931', 'Todo 1', 'Description 1', '2021-12-13 00:00:00', 0, false, 1),
(2, '3ac29c71-450a-4bf2-bded-5a6e8e16db90', 'Todo 2', 'Description 2', '2021-12-11 00:00:00', 0, false, 2),
(3, '86600e3c-d3ac-4102-a29c-1e9b63385283', 'Todo 3', 'Description 3', '2021-12-15 00:00:00', 1, false, 3),
(4, 'd9fb7a46-8e95-4088-9a98-11a6b362dc34', 'Todo 4', 'Description 4', '2021-12-12 00:00:00', 2, false, 4),
(5, '16de87f0-aad8-4a71-8597-dea604e06322', 'Todo 5', 'Description 5', '2021-12-13 00:00:00', 1, false, 5),
(6, '16de87f0-aad8-4a71-8597-dea604e06322', 'Dodo\'s Todo 1', 'Description 1 owned by Dodo', '2021-12-12 00:00:00', 1, false, 1);

CREATE TABLE IF NOT EXISTS todos_users(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    todo_id bigint(20),
    user_id bigint(20),
    role SMALLINT DEFAULT 0 COMMENT '0: Owner, 1: Collaborator',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todos_users(id, todo_id, user_id, role) VALUES
(1, 1, 1, 0),
(2, 2, 1, 0),
(3, 3, 1, 0),
(4, 4, 1, 0),
(5, 5, 1, 0),
(6, 6, 1, 1),
(7, 6, 2, 0);

CREATE TABLE IF NOT EXISTS activity_logs(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    todo_id bigint(20),
    user_id bigint(20),
    `action` VARCHAR(255),
    `full_text` TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users_activity_logs(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    activity_logs_id bigint(20),
    user_id bigint(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS todos_tags(
    id bigint(20) PRIMARY KEY AUTO_INCREMENT,
    todo_id VARCHAR(255),
    tag_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);