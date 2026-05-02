import apiClient from './apiClient';
import type { ApiResponse, IssueDto } from '../types/api';

export const issueApi = {
    getIssuesByGroup: (groupId: number) =>
        apiClient.get<ApiResponse<IssueDto[]>>(`/api/issues/group/${groupId}`),

    createIssue: (data: any) =>
        apiClient.post<ApiResponse>('/api/issues', data),

    pushToJira: (issueId: number) =>
        apiClient.post<ApiResponse>(`/api/issues/${issueId}/push-create`),

    updateStatus: (issueId: number, status: string) =>
        apiClient.patch<ApiResponse>(`/api/issues/${issueId}/status`, { status }),

    syncFromJira: (groupId: number, projectKey: string) =>
        apiClient.post<ApiResponse>('/api/issues/sync-jira', { groupId, projectKey }),

    getMyAssigned: () =>
        apiClient.get<ApiResponse>('/api/issues/my-assigned'),

    updateIssue: (issueId: number, data: {
        title?: string;
        description?: string;
        deadline?: string | null;
        assignedToMemberId?: number | null;
        parentId?: number | null;
        status?: string;
    }) => apiClient.put<ApiResponse>(`/api/issues/${issueId}`, data),

    pushUpdateToJira: (issueId: number) =>
        apiClient.put<ApiResponse>(`/api/issues/${issueId}/push-update`),

    submitIssue: (issueId: number, content: string) =>
        apiClient.post<ApiResponse>('/api/submissions', null, {
            params: { issueId, content }
        }),

    checkSubmission: (issueId: number) =>
        apiClient.get<ApiResponse<{ submitted: boolean }>>(`/api/submissions/check/${issueId}`),
};