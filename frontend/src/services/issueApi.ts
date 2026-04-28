import apiClient from './apiClient';
import type { ApiResponse, IssueDto } from '../types/api';

export const issueApi = {
    // 1. Lấy danh sách issue của 1 nhóm
    getIssuesByGroup: (groupId: number) =>
        apiClient.get<ApiResponse<IssueDto[]>>(`/api/issues/group/${groupId}`),

    // 2. Tạo mới một Issue (Chỉ Team Leader được làm)
    createIssue: (body: any) =>
        apiClient.post<ApiResponse>('/api/issues', body),

    // 3. Cập nhật trạng thái Issue (TODO, IN_PROGRESS, DONE)
    updateIssueStatus: (issueId: number, status: string) =>
        apiClient.put<ApiResponse>(`/api/issues/${issueId}/status`, { status }),

    // 4. Phân công Issue cho một thành viên
    assignIssue: (issueId: number, memberId: number) =>
        apiClient.put<ApiResponse>(`/api/issues/${issueId}/assignee/${memberId}`),
};