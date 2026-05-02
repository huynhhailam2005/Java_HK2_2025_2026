import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const jiraApi = {
    syncGroup: (groupId: number, jiraGroupName: string) =>
        apiClient.post<ApiResponse>(`/api/groups/${groupId}/sync-jira`, { jiraGroupName }),

    pullIssues: (groupId: number, projectKey: string) =>
        apiClient.post<ApiResponse>('/api/issues/sync-jira', { groupId, projectKey }),

    pushIssueToJira: (issueId: number) =>
        apiClient.post<ApiResponse>(`/api/issues/${issueId}/push-create`)
};