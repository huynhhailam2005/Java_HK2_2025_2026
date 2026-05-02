-- Drop all tables
DROP TABLE IF EXISTS submissions CASCADE;
DROP TABLE IF EXISTS issues CASCADE;
DROP TABLE IF EXISTS group_members CASCADE;
DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS lecturers CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- Users
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_role VARCHAR(31) NOT NULL CHECK (user_role IN ('ADMIN', 'LECTURER', 'STUDENT'))
);


-- Admins
CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    admin_code VARCHAR(20) UNIQUE NOT NULL
);


-- Lecturers
CREATE TABLE lecturers (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    lecturer_code VARCHAR(20) UNIQUE NOT NULL
);


-- Students
CREATE TABLE students (
    user_id BIGINT PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    jira_account_id VARCHAR(255) UNIQUE,
    github_username VARCHAR(255) UNIQUE
);


-- Groups
CREATE TABLE groups (
    group_id BIGSERIAL PRIMARY KEY,
    group_code VARCHAR(20) UNIQUE NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    lecturer_id BIGINT NOT NULL REFERENCES lecturers(user_id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    jira_url VARCHAR(500),
    jira_project_key VARCHAR(50),
    jira_api_token VARCHAR(255),
    jira_admin_email VARCHAR(255),
    github_repo_url VARCHAR(500),
    github_access_token VARCHAR(255)
);


-- Group Members
CREATE TABLE group_members (
    group_member_id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    group_member_role VARCHAR(50) NOT NULL DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_group_student UNIQUE (group_id, student_id)
);


-- Issues
CREATE TABLE issues (
    issue_id BIGSERIAL PRIMARY KEY,
    issue_code VARCHAR(20) UNIQUE,
    group_id BIGINT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
    assigned_to_member_id BIGINT REFERENCES group_members(group_member_id) ON DELETE SET NULL,
    parent_id BIGINT REFERENCES issues(issue_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    deadline TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    issue_type VARCHAR(50) NOT NULL DEFAULT 'TASK' CHECK (issue_type IN ('TASK', 'BUG', 'STORY', 'SUB_TASK', 'EPIC')),
    sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (sync_status IN ('PENDING', 'SYNCED', 'ERROR')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_epic_no_parent CHECK (issue_type != 'EPIC' OR parent_id IS NULL),
    CONSTRAINT chk_subtask_has_parent CHECK (issue_type != 'SUB_TASK' OR parent_id IS NOT NULL)
);


-- Submissions
CREATE TABLE submissions (
    submission_id BIGSERIAL PRIMARY KEY,
    submission_code VARCHAR(20) UNIQUE NOT NULL,
    issue_id BIGINT NOT NULL REFERENCES issues(issue_id) ON DELETE CASCADE,
    submitted_by_member_id BIGINT NOT NULL REFERENCES group_members(group_member_id) ON DELETE CASCADE,
    content TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
