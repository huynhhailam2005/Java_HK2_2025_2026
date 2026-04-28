// src/types/api.ts
export interface ApiResponse<T = unknown> {
    success: boolean;
    message: string;
    data: T;
} // [cite: 25]

export interface AdminUserResponse {
    id: number;
    username: string;
    email: string;
    role: 'ADMIN' | 'LECTURER' | 'STUDENT';
    studentCode?: string;
    lecturerCode?: string;
    jiraAccountId?: string;
    githubUsername?: string;
} // [cite: 25, 26, 27]

export interface GroupMemberDto {
    id: number;
    student: { id: number; username: string; studentCode: string };
    role: 'LEADER' | 'MEMBER';
} // [cite: 29]

export interface GroupDto {
    id: number;
    groupName: string;
    groupCode?: string;
    jiraUrl?: string;
    jiraProjectKey?: string;
    jiraAdminEmail?: string;
    jiraApiToken?: string;
    githubRepoUrl?: string;
    githubAccessToken?: string;
    lecturer?: { id: number; username: string; lecturerCode: string };
    groupMembers?: GroupMemberDto[];
} // [cite: 27, 28, 29]

export interface IssueDto {
    id: number;
    title: string;
    description?: string;
    issueType: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
    status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
    issueCode?: string;
    syncStatus?: string;
    parentId?: number;
    deadline?: string;
    assignedTo?: GroupMemberDto;
    group?: { id: number; groupName: string };
} // [cite: 30, 31, 32]