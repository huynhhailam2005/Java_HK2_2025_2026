import apiClient from './apiClient';
import type { ApiResponse, AdminUserResponse, GroupDto } from '../types/api';

export const adminApi = {
    getUsers: () => apiClient.get<ApiResponse<AdminUserResponse[]>>('/api/admin/users'),
    getUserById: (id: number) => apiClient.get<ApiResponse>(`/api/admin/users/${id}`),
    updateUser: (id: number, body: any) => apiClient.put<ApiResponse>(`/api/admin/users/${id}`, body),
    deleteUser: (id: number) => apiClient.delete<ApiResponse>(`/api/admin/users/${id}`),

    getGroups: () => apiClient.get<ApiResponse<GroupDto[]>>('/api/groups'),
    getGroupById: (id: number) => apiClient.get<ApiResponse<GroupDto>>(`/api/groups/${id}`),
    createGroup: (body: any) => apiClient.post<ApiResponse>('/api/groups', body),
    updateGroup: (id: number, body: any) => apiClient.put<ApiResponse>(`/api/groups/${id}`, body),
    deleteGroup: (id: number) => apiClient.delete<ApiResponse>(`/api/groups/${id}`),
    updateGroupLecturer: (id: number, lecturerId: number) =>
        apiClient.patch<ApiResponse>(`/api/admin/groups/${id}/lecturer`, { lecturerId }),
};