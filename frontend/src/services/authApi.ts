import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export const AUTH_TOKEN_KEY = 'token';

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

export interface UserPayload {
    id: number;
    username: string;
    email: string;
    role: 'ADMIN' | 'LECTURER' | 'STUDENT';
}

export interface LoginResponseData {
    user: UserPayload;
    token: string;
}

export const getApiErrorMessage = (error: unknown, defaultMsg?: string): string => {
    const err = error as { response?: { data?: { message?: string } } };
    return err.response?.data?.message || defaultMsg || 'Đã có lỗi xảy ra từ hệ thống!';
};

export const login = async (credentials: LoginPayload) => {
    const response = await apiClient.post<ApiResponse>('/api/auth/login', credentials);
    return response.data;
};

export const register = async (data: RegisterPayload) => {
    const response = await apiClient.post<ApiResponse>('/api/auth/register', data);
    return response.data;
};

export const authApi = {
    login,
    register
};