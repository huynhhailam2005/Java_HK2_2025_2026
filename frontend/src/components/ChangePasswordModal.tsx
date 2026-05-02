import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Lock, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { userApi } from '../services/userApi';
import { showLiquidToast } from '../utils/toast';

interface ChangePasswordModalProps {
    isOpen: boolean;
    onClose: () => void;
    userId: number;
}

const ChangePasswordModal = ({ isOpen, onClose, userId }: ChangePasswordModalProps) => {
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [loading, setLoading] = useState(false);

    const resetForm = () => {
        setOldPassword('');
        setNewPassword('');
        setConfirmPassword('');
        setShowOld(false);
        setShowNew(false);
        setShowConfirm(false);
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!oldPassword.trim()) {
            showLiquidToast('Vui lòng nhập mật khẩu cũ', 'error');
            return;
        }
        if (!newPassword.trim()) {
            showLiquidToast('Vui lòng nhập mật khẩu mới', 'error');
            return;
        }
        if (newPassword.length < 6) {
            showLiquidToast('Mật khẩu mới phải tối thiểu 6 ký tự', 'error');
            return;
        }
        if (newPassword !== confirmPassword) {
            showLiquidToast('Mật khẩu xác nhận không khớp', 'error');
            return;
        }
        if (oldPassword === newPassword) {
            showLiquidToast('Mật khẩu mới không được trùng với mật khẩu cũ', 'error');
            return;
        }

        setLoading(true);
        try {
            const res = await userApi.changePassword(userId, {
                oldPassword: oldPassword.trim(),
                newPassword: newPassword.trim(),
                confirmPassword: confirmPassword.trim(),
            });

            if (res.data.success) {
                showLiquidToast('Đổi mật khẩu thành công!', 'success');
                handleClose();
            }
        } catch (err: any) {
            const message = err?.response?.data?.message || 'Đổi mật khẩu thất bại!';
            showLiquidToast(message, 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto"
                    onClick={handleClose}
                >
                    <motion.div
                        initial={{ scale: 0.9, opacity: 0, y: 20 }}
                        animate={{ scale: 1, opacity: 1, y: 0 }}
                        exit={{ scale: 0.9, opacity: 0, y: 20 }}
                        transition={{ type: 'spring', stiffness: 300, damping: 25 }}
                        onClick={(e) => e.stopPropagation()}
                        className="bg-[#151b2b] border border-white/10 rounded-2xl p-6 w-full max-w-md shadow-2xl max-h-[85vh] overflow-y-auto my-auto"
                    >
                        {/* Header */}
                        <div className="flex items-center justify-between mb-6">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 bg-blue-600/20 rounded-xl flex items-center justify-center">
                                    <Lock size={20} className="text-blue-400" />
                                </div>
                                <div>
                                    <h2 className="text-white font-bold text-lg">Đổi mật khẩu</h2>
                                    <p className="text-slate-500 text-[10px] font-bold uppercase tracking-wider">
                                        Bảo mật tài khoản
                                    </p>
                                </div>
                            </div>
                            <button
                                onClick={handleClose}
                                className="p-2 hover:bg-white/10 rounded-lg transition text-slate-400 hover:text-white"
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {/* Form */}
                        <form onSubmit={handleSubmit} className="space-y-3">
                            {/* Mật khẩu cũ */}
                            <div>
                                <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 block">
                                    Mật khẩu cũ
                                </label>
                                <div className="relative">
                                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                                    <input
                                        type={showOld ? 'text' : 'password'}
                                        value={oldPassword}
                                        onChange={(e) => setOldPassword(e.target.value)}
                                        placeholder="Nhập mật khẩu hiện tại"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-9 pr-9 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/20 transition-all"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowOld(!showOld)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition"
                                    >
                                        {showOld ? <EyeOff size={14} /> : <Eye size={14} />}
                                    </button>
                                </div>
                            </div>

                            {/* Mật khẩu mới */}
                            <div>
                                <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 block">
                                    Mật khẩu mới
                                </label>
                                <div className="relative">
                                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                                    <input
                                        type={showNew ? 'text' : 'password'}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        placeholder="Tối thiểu 6 ký tự"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-9 pr-9 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/20 transition-all"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowNew(!showNew)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition"
                                    >
                                        {showNew ? <EyeOff size={14} /> : <Eye size={14} />}
                                    </button>
                                </div>
                            </div>

                            {/* Xác nhận mật khẩu */}
                            <div>
                                <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 block">
                                    Xác nhận mật khẩu mới
                                </label>
                                <div className="relative">
                                    <ShieldCheck size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                                    <input
                                        type={showConfirm ? 'text' : 'password'}
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        placeholder="Nhập lại mật khẩu mới"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-9 pr-9 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 focus:ring-1 focus:ring-blue-500/20 transition-all"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowConfirm(!showConfirm)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition"
                                    >
                                        {showConfirm ? <EyeOff size={14} /> : <Eye size={14} />}
                                    </button>
                                </div>
                            </div>

                            {/* Buttons */}
                            <div className="flex gap-3">
                                <button
                                    type="button"
                                    onClick={handleClose}
                                    className="flex-1 py-2.5 rounded-xl text-sm font-bold text-slate-400 bg-white/5 border border-white/10 hover:bg-white/10 transition-all active:scale-95"
                                >
                                    Huỷ
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white bg-blue-600 hover:bg-blue-500 transition-all active:scale-95 shadow-lg shadow-blue-600/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                                >
                                    {loading ? (
                                        <>
                                            <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                                            </svg>
                                            Đang xử lý...
                                        </>
                                    ) : (
                                        <>
                                            <Lock size={16} /> Đổi mật khẩu
                                        </>
                                    )}
                                </button>
                            </div>
                        </form>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default ChangePasswordModal;
