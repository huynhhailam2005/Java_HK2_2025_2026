import apiClient from './apiClient';
import type { ApiResponse, GitHubMemberMappingDto } from '../types/api';

export const githubApi = {
    getMemberMappings: (groupId: number) =>
        apiClient.get<ApiResponse<GitHubMemberMappingDto[]>>(`/api/github/groups/${groupId}/members`),

    getTeamCommits: (groupId: number) =>
        apiClient.get<ApiResponse>(`/api/github/groups/${groupId}/team-commits-summary`),

    getTeamCommitHistory: (groupId: number, days?: number) =>
        apiClient.get<ApiResponse>(`/api/github/groups/${groupId}/team-commit-history${days ? `?days=${days}` : ''}`),

    getMemberCommits: (groupId: number, memberId: number) =>
        apiClient.get<ApiResponse>(`/api/github/groups/${groupId}/members/${memberId}/commits`)
};