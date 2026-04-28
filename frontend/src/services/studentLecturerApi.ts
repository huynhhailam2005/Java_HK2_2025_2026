import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface StudentRequest {
    username?: string;
    password?: string;
    email?: string;
    studentCode?: string;
    jiraAccountId?: string;
    githubUsername?: string;
}

export interface LecturerRequest {
    username?: string;
    password?: string;
    email?: string;
    lecturerCode?: string;
}

export const studentApi = {
    create: (body: StudentRequest) => apiClient.post<ApiResponse>('/api/admin/students', body),
    update: (id: number, body: StudentRequest) => apiClient.put<ApiResponse>(`/api/admin/students/${id}`, body),
};

export const lecturerApi = {
    create: (body: LecturerRequest) => apiClient.post<ApiResponse>('/api/admin/lecturers', body),
    update: (id: number, body: LecturerRequest) => apiClient.put<ApiResponse>(`/api/admin/lecturers/${id}`, body),
};