DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    student_id  VARCHAR(20)  UNIQUE
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    rating INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    student_id VARCHAR(50),
    lecturer_id VARCHAR(50)
);

drop table feedback;

CREATE TABLE IF NOT EXISTS feedbacks (
    id VARCHAR(50) PRIMARY KEY,
    content TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    reviewer_id VARCHAR(50) NOT NULL, -- ID của Giảng viên
    group_id VARCHAR(50) NOT NULL,    -- ID của Nhóm bị đánh giá
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
