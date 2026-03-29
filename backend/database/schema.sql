DROP TABLE IF EXISTS submission CASCADE;
DROP TABLE IF EXISTS group_students CASCADE;
DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS lecturers CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE admins (
    id VARCHAR(50) PRIMARY KEY,
    CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE lecturers (
    id VARCHAR(50) PRIMARY KEY,
    CONSTRAINT fk_lecturer_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE students (
    id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(20) UNIQUE,
    CONSTRAINT fk_student_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE submission (
    id SERIAL PRIMARY KEY,
    file_url TEXT,
    note TEXT,
    student_id BIGINT,
    project_id BIGINT
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

CREATE TABLE groups (
    id          VARCHAR(36)  PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    lecturer_id VARCHAR(50)  NOT NULL,
    CONSTRAINT fk_group_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id) ON DELETE RESTRICT
);

CREATE TABLE group_students (
    group_id   VARCHAR(36) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, student_id),
    CONSTRAINT fk_group_students_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_students_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);
