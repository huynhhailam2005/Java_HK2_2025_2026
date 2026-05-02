import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface UserProfile {
    id: number;
    username: string;
    email: string;
    role: 'ADMIN' | 'LECTURER' | 'STUDENT';
    lecturerCode?: string;
    studentCode?: string;
    jiraAccountId?: string;
    githubUsername?: string;
}

export interface UpdateUserRequest {
    username?: string;
    email?: string;
    password?: string;
}

export interface UpdateLecturerRequest {
    username?: string;
    email?: string;
    password?: string;
    lecturerId?: string;
}

export interface UpdateStudentRequest {
    username?: string;
    email?: string;
    password?: string;
    studentId?: string;
    jiraAccountId?: string;
    githubUsername?: string;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export const userApi = {
    // Get user profile by ID
    getProfile: (userId: number) =>
        apiClient.get<ApiResponse<UserProfile>>(`/api/users/${userId}`),

    // Update user profile (general)
    updateProfile: (userId: number, data: UpdateUserRequest) =>
        apiClient.put<ApiResponse<UserProfile>>(`/api/users/${userId}`, data),

    // Update lecturer profile
    updateLecturerProfile: (userId: number, data: UpdateLecturerRequest) =>
        apiClient.put<ApiResponse<any>>(`/api/users/${userId}/lecturer`, data),

    // Update student profile
    updateStudentProfile: (userId: number, data: UpdateStudentRequest) =>
        apiClient.put<ApiResponse<any>>(`/api/users/${userId}/student`, data),

    // Change password
    changePassword: (userId: number, data: ChangePasswordRequest) =>
        apiClient.put<ApiResponse<any>>(`/api/users/${userId}/password`, data),
};

