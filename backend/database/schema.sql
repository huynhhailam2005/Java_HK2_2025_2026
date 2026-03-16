DROP TABLE IF EXISTS submission CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    student_id VARCHAR(20) UNIQUE
);

CREATE TABLE submission (
    id SERIAL PRIMARY KEY,
    file_url TEXT,
    note TEXT,
    student_id BIGINT,
    project_id BIGINT
);