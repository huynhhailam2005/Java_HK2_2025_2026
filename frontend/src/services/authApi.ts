import axios from 'axios';

export type AuthApiResponse = {
    success: boolean;
    message: string;
    data?: unknown;
};

export type LoginPayload = {
    username: string;
    password: string;
};

export type RegisterPayload = {
    username: string;
    password: string;
    email: string;
    role: string;
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
    baseURL: apiBaseUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

export async function login(payload: LoginPayload): Promise<AuthApiResponse> {
    const response = await apiClient.post<AuthApiResponse>('/api/auth/login', payload);
    return response.data;
}

export async function register(payload: RegisterPayload): Promise<AuthApiResponse> {
    const response = await apiClient.post<AuthApiResponse>('/api/auth/register', payload);
    return response.data;
}

export function getApiErrorMessage(error: unknown, fallback = 'Khong the ket noi den server'): string {
    if (axios.isAxiosError(error)) {
        const message = error.response?.data?.message;
        if (typeof message === 'string' && message.trim().length > 0) {
            return message;
        }
    }
    return fallback;
}

