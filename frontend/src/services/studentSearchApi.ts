import apiClient from './apiClient';
import type { ApiResponse } from '../types/api';

export interface StudentSearchResult {
    id: number;
    username: string;
    studentCode: string;
    email: string;
    githubUsername: string | null;
    jiraAccountId: string | null;
}

export const studentSearchApi = {
    search: (q: string) =>
        apiClient.get<ApiResponse<StudentSearchResult[]>>(`/api/students/search?q=${encodeURIComponent(q)}`),
    getAll: () =>
        apiClient.get<ApiResponse<StudentSearchResult[]>>('/api/students/all'),
};
