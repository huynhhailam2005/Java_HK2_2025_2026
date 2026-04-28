import axios from 'axios';

// Khai báo thẳng ở đây để cắt đứt lỗi Circular Import làm crash Vite
const TOKEN_KEY = 'token';

const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: tự động gắn Bearer token
apiClient.interceptors.request.use((config) => {
    const url = config.url || '';
    // Bỏ qua gắn token cho các api đăng nhập/đăng ký
    if (url.startsWith('/api/auth/')) return config;

    const token = localStorage.getItem(TOKEN_KEY);
    if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Response interceptor: tự logout khi token hết hạn (lỗi 401)
apiClient.interceptors.response.use(
    (res) => res,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem('user');

            // Đá về login nếu đang không ở trang login (tránh reload liên tục)
            if (window.location.pathname !== '/login' && window.location.pathname !== '/') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;