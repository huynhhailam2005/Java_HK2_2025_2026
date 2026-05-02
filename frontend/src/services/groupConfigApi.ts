import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface GroupConfigData {
    id: number;
    groupCode: string;
    groupName: string;
    lecturerId: number;
    jiraUrl?: string;
    jiraProjectKey?: string;
    jiraApiToken?: string;
    jiraAdminEmail?: string;
    githubRepoUrl?: string;
    githubAccessToken?: string;
}

export interface UpdateGroupConfigRequest {
    groupName?: string;
    lecturerId?: number;
    jiraUrl?: string;
    jiraProjectKey?: string;
    jiraApiToken?: string;
    jiraAdminEmail?: string;
    githubRepoUrl?: string;
    githubAccessToken?: string;
}

export const groupConfigApi = {
    getGroupConfig: (groupId: number) =>
        apiClient.get<ApiResponse<GroupConfigData>>(`/api/admin/groups/${groupId}`),

    updateGroupConfig: (groupId: number, data: UpdateGroupConfigRequest) =>
        apiClient.put<ApiResponse<GroupConfigData>>(`/api/admin/groups/${groupId}`, data),
};

