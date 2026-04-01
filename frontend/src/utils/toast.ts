// Định nghĩa kiểu dữ liệu cho Toast (Fix lỗi Unexpected any)
export interface ToastDetail {
    message: string;
    type: 'success' | 'error';
}

// Hàm hú Toast dùng chung cho toàn bộ App
export const showLiquidToast = (message: string, type: 'success' | 'error') => {
    const event = new CustomEvent('show-liquid-toast', {
        detail: { message, type }
    });
    window.dispatchEvent(event);
};