import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface GradeInput {
    studentId: number;
    score: number;
    feedback: string;
}

export const gradingApi = {
    // Gửi bảng điểm của một nhóm xuống Backend lưu lại
    submitGrades: (groupId: number, grades: GradeInput[]) =>
        apiClient.post<ApiResponse>(`/api/grading/group/${groupId}`, { grades }),

    // Lấy điểm đã chấm (nếu có) để hiển thị lại
    getGrades: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/grading/group/${groupId}`)
};