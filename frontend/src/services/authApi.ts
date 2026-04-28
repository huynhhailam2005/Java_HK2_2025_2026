import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

// Định nghĩa key lưu token
export const AUTH_TOKEN_KEY = 'token';

// Type chuẩn
export interface LoginPayload {
    username?: string;
    password?: string;
}

export interface RegisterPayload {
    username?: string;
    password?: string;
    email?: string;
    role?: string;
    studentCode?: string;
    lecturerCode?: string;
}

// Hàm bắt lỗi
export const getApiErrorMessage = (error: unknown): string => {
    const err = error as { response?: { data?: { message?: string } } };
    return err.response?.data?.message || 'Đã có lỗi xảy ra từ hệ thống!';
};

// 1. Export lẻ tẻ (để fix lỗi import { login })
export const login = async (credentials: LoginPayload) => {
    const response = await apiClient.post<ApiResponse>('/api/auth/login', credentials);
    return response.data;
};

export const register = async (data: RegisterPayload) => {
    const response = await apiClient.post<ApiResponse>('/api/auth/register', data);
    return response.data;
};

// 2. Export nguyên cục object (để ai gọi authApi.login vẫn chạy)
export const authApi = {
    login,
    register
};