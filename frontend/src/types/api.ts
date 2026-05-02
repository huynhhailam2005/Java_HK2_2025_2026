// src/types/api.ts
export interface ApiResponse<T = unknown> {
    success: boolean;
    message: string;
    data: T;
}

export interface AdminUserResponse {
    id: number;
    username: string;
    email: string;
    role: 'ADMIN' | 'LECTURER' | 'STUDENT';
    studentCode?: string;
    lecturerCode?: string;
    jiraAccountId?: string;
    githubUsername?: string;
}

export interface GitHubMemberMappingDto {
    groupMemberId: number;
    studentId: number;
    studentCode: string;
    studentUsername: string;
    githubUsername: string;
    groupId: number;
    groupCode: string;
    groupName: string;
    githubRepoUrl: string;
    isMapped: boolean;
    mappingStatus: string;
}

export interface GroupMemberDto {
    id: number;
    student: { id: number; username: string; studentCode: string };
    role: 'LEADER' | 'MEMBER';
}

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
}

export interface IssueDto {
    issueId: number;
    issueCode: string;
    title: string;
    description?: string;
    type: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
    status: 'TODO' | 'IN_PROGRESS' | 'DONE';
    assignedTo: string;
    deadline?: string;
    parentId?: number | null;
    parentCode?: string;
    parentTitle?: string;
    createdAt?: string;
    updatedAt?: string;
    submitted?: boolean;
}

export interface IssueTreeNode extends IssueDto {
    children: IssueTreeNode[];
}


export const buildIssueTree = (flatIssues: IssueDto[]): IssueTreeNode[] => {
    if (!flatIssues || flatIssues.length === 0) return [];
    const map: Record<number, IssueTreeNode> = {};
    const roots: IssueTreeNode[] = [];

    flatIssues.forEach(issue => {
        map[issue.issueId] = { ...issue, children: [] };
    });

    flatIssues.forEach(issue => {
        const node = map[issue.issueId];
        if (issue.parentId && map[issue.parentId]) {
            map[issue.parentId].children.push(node);
        } else {
            roots.push(node);
        }
    });
    return roots;
};