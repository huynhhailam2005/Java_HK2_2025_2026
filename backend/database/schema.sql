DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    student_id  VARCHAR(20)  UNIQUE
);

DROP TABLE IF EXISTS topics CASCADE;

CREATE TABLE topics (
    id          VARCHAR(36)  PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    lecturer_id VARCHAR(36),
    student_id  VARCHAR(36),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);