import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const groupApi = {
    // 1. Lấy danh sách thành viên của 1 nhóm
    getMembers: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/groups/${groupId}/members`),

    // 2. Thêm sinh viên vào nhóm
    addMember: (groupId: number, studentId: number) =>
        apiClient.post<ApiResponse>(`/api/groups/${groupId}/members/${studentId}`),

    // 3. Xoá sinh viên khỏi nhóm
    removeMember: (groupId: number, memberId: number) =>
        apiClient.delete<ApiResponse>(`/api/groups/${groupId}/members/${memberId}`),

    // 4. Phân công Nhóm trưởng (Dựa theo TeamLeaderController)
    assignLeader: (groupId: number, memberId: number) =>
        apiClient.post<ApiResponse>(`/api/groups/${groupId}/leader/${memberId}`)
};