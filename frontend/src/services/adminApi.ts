import axios from 'axios';

const API_URL = 'http://localhost:8080/api/admin';

// Lấy token từ localStorage (vì admin đã đăng nhập)
const getHeader = () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return {
        headers: { Authorization: `Bearer ${user.token}` }
    };
};

export const adminApi = {
    // Quản lý User
    getUsers: async () => {
        const response = await axios.get(`${API_URL}/users`, getHeader());
        return response.data;
    },
    updateUserRole: async (userId: number, role: string) => {
        const response = await axios.put(`${API_URL}/users/${userId}/role`, { role }, getHeader());
        return response.data;
    },

    // Quản lý Group
    getGroups: async () => {
        const response = await axios.get(`${API_URL}/groups`, getHeader());
        return response.data;
    },
    updateGroup: async (groupId: number, data: any) => {
        const response = await axios.put(`${API_URL}/groups/${groupId}`, data, getHeader());
        return response.data;
    }
};