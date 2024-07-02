CREATE TABLE IF NOT EXISTS users(
    id bigint(20) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users(id, email, name, password) VALUES
(1, 'andrio@email.com', 'Andrio', "$2a$12$MK51buovsAkP0SlAuKzQh.ocDLzicIiFASVXCSSnk.o8US8b4Rjie");

CREATE TABLE IF NOT EXISTS todos(
    id bigint(20) PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP,
    status SMALLINT,
    is_deleted BOOLEAN,
    priority INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS todos_users(
    id bigint(20) PRIMARY KEY,
    todo_id bigint(20),
    user_id bigint(20),
    role SMALLINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS activity_logs(
    id bigint(20) PRIMARY KEY,
    todo_id bigint(20),
    user_id bigint(20),
    action SMALLINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users_activity_logs(
    todo_id bigint(20),
    user_id bigint(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags(
    id bigint(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS todos_tags(
    id bigint(20) PRIMARY KEY,
    todo_id VARCHAR(255),
    tag_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);