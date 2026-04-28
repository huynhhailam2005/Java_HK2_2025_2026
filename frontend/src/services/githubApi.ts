import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const githubApi = {
    // Lưu link Repo và đồng bộ lịch sử commit về hệ thống
    syncRepo: (groupId: number, repoUrl: string) =>
        apiClient.post<ApiResponse>(`/api/github/sync/${groupId}`, { repoUrl }),

    // Lấy danh sách commit đã lưu của nhóm
    getCommits: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/github/commits/${groupId}`)
};