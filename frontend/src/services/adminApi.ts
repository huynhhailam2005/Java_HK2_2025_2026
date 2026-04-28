// src/services/adminApi.ts (VIẾT LẠI HOÀN TOÀN) [cite: 35]
import apiClient from './apiClient';
import type { ApiResponse, AdminUserResponse, GroupDto } from '../types/api';

export const adminApi = {
    // ── User ──
    getUsers: () => apiClient.get<ApiResponse<AdminUserResponse[]>>('/api/admin/users'), // [cite: 36]
    getUserById: (id: number) => apiClient.get<ApiResponse>(`/api/admin/users/${id}`),
    updateUser: (id: number, body: Partial<AdminUserResponse> & { password?: string }) =>
        apiClient.put<ApiResponse>(`/api/admin/users/${id}`, body), // [cite: 36, 37]
    deleteUser: (id: number) => apiClient.delete<ApiResponse>(`/api/admin/users/${id}`), // [cite: 37]

    // ── Group ──
    getGroups: () => apiClient.get<ApiResponse<GroupDto[]>>('/api/admin/groups'), // [cite: 37]
    createGroup: (body: Partial<GroupDto> & { lecturerId?: number }) =>
        apiClient.post<ApiResponse>('/api/admin/groups', body), // [cite: 37]
    updateGroup: (id: number, body: Partial<GroupDto> & { lecturerId?: number }) =>
        apiClient.put<ApiResponse>(`/api/admin/groups/${id}`, body), // [cite: 37]
    deleteGroup: (id: number) => apiClient.delete<ApiResponse>(`/api/admin/groups/${id}`), // [cite: 38]
};