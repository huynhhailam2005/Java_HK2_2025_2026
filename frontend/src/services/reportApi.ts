import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface MemberProgress {
    studentId: number;
    username: string;
    fullName: string;
    completedTasks: number;
    totalTasks: number;
    commitCount: number;
    contributionPercentage: number;
}

export interface GroupProgressReport {
    groupId: number;
    groupName: string;
    overallProgress: number;
    memberProgress: MemberProgress[];
    lastSyncJira: string;
    lastSyncGitHub: string;
}

export const reportApi = {
    // Lấy báo cáo chi tiết của một nhóm
    getGroupReport: (groupId: number) =>
        apiClient.get<ApiResponse<GroupProgressReport>>(`/api/reports/group/${groupId}`),

    // Xuất file báo cáo (PDF/Excel) - Backend sẽ trả về blob
    exportReport: (groupId: number, format: 'pdf' | 'excel') =>
        apiClient.get(`/api/reports/group/${groupId}/export?format=${format}`, { responseType: 'blob' })
};