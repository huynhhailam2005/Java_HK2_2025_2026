import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const jiraApi = {
    // 1. Đồng bộ (Tạo) Project Jira từ Group của SRPM
    syncGroupToJira: (groupId: number) =>
        apiClient.post<ApiResponse>(`/api/jira/sync/group/${groupId}`),

    // 2. Đồng bộ Issue (Công việc) của một nhóm lên Jira
    syncIssuesToJira: (groupId: number) =>
        apiClient.post<ApiResponse>(`/api/jira/sync/issues/push/${groupId}`),

    // 3. Kéo Issue từ Jira về SRPM (Nếu cần)
    pullIssuesFromJira: (groupId: number) =>
        apiClient.post<ApiResponse>(`/api/jira/sync/issues/pull/${groupId}`)
};