import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const groupApi = {
    getById: (id: number) =>
        apiClient.get<ApiResponse>(`/api/groups/${id}`),

    getMembers: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/groups/${groupId}/members`),

    addMember: (groupId: number, studentId: number) =>
        apiClient.post<ApiResponse>(`/api/groups/${groupId}/members/${studentId}`),
    removeMember: (groupId: number, memberId: number) =>
        apiClient.delete<ApiResponse>(`/api/groups/${groupId}/members/${memberId}`),

    assignTeamLeader: (groupId: number, studentId: number) =>
        apiClient.post<ApiResponse>(`/api/groups/${groupId}/team-leader`, { studentId }),

    changeTeamLeader: (groupId: number, studentId: number) =>
        apiClient.put<ApiResponse>(`/api/groups/${groupId}/team-leader`, { studentId }),

    getTeamLeader: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/groups/${groupId}/team-leader`),

    removeTeamLeader: (groupId: number) =>
        apiClient.delete<ApiResponse>(`/api/groups/${groupId}/team-leader`),
};