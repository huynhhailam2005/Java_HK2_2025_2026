import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface IssueReportItem {
    issueId: number;
    issueCode: string | null;
    title: string;
    description?: string;
    type: string;
    status: string;
    assignedTo: string;
    deadline?: string;
    groupId?: number;
    groupName?: string;
    parentId?: number | null;
    parentCode?: string | null;
    parentTitle?: string | null;
    submitted?: boolean;
    createdAt?: string;
}

export interface CommitItem {
    sha: string;
    message: string;
    author: string;
    date: string;
    url?: string;
}

export interface MemberContribution {
    memberId: number;
    studentCode: string;
    username: string;
    githubUsername: string;
    role: string;
    assignedIssues: number;
    completedIssues: number;
    commitCount: number;
    completionRate: number;
}

export interface GroupProgressReport {
    groupId: number;
    groupName: string;
    groupCode: string;
    githubRepoUrl?: string;
    totalIssues: number;
    completedIssues: number;
    inProgressIssues: number;
    todoIssues: number;
    completedPercentage: string;
    progress: number;
    issuesByStatus: {
        DONE: IssueReportItem[];
        IN_PROGRESS: IssueReportItem[];
        TODO: IssueReportItem[];
    };
    memberContributions: MemberContribution[];
    totalMembers: number;
    commitHistory: CommitItem[];
}

export const reportApi = {
    getGroupReport: (groupId: number) =>
        apiClient.get<ApiResponse<GroupProgressReport>>(`/api/progress/groups/${groupId}`),

    exportReport: (groupId: number, format: 'pdf' | 'excel') =>
        apiClient.get(`/api/reports/group/${groupId}/export?format=${format}`, {
            responseType: 'blob'
        })
};