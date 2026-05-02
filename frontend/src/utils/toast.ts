export interface ToastDetail {
    message: string;
    type: 'success' | 'error';
}

export const showLiquidToast = (message: string, type: 'success' | 'error') => {
    const event = new CustomEvent('show-liquid-toast', {
        detail: { message, type }
    });
    window.dispatchEvent(event);
};