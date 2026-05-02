import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Lock, Eye, EyeOff, ShieldCheck, KeyRound } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { showLiquidToast } from '../utils/toast';

interface ResetPasswordModalProps {
    isOpen: boolean;
    onClose: () => void;
    userId: number;
    userName: string;
    userEmail: string;
}

const ResetPasswordModal = ({ isOpen, onClose, userId, userName, userEmail }: ResetPasswordModalProps) => {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [loading, setLoading] = useState(false);

    const resetForm = () => {
        setNewPassword('');
        setConfirmPassword('');
        setShowNew(false);
        setShowConfirm(false);
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

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

        setLoading(true);
        try {
            // 🔥 SỬA TẠI ĐÂY: Gửi kèm username và email cũ để BE thỏa mãn điều kiện @NotBlank
            const res = await adminApi.updateUser(userId, {
                username: userName,
                email: userEmail,
                password: newPassword.trim()
            });

            if (res.data.success) {
                showLiquidToast('Đặt lại mật khẩu thành công!', 'success');
                handleClose();
            }
        } catch (err: any) {
            const message = err?.response?.data?.message || 'Đặt lại mật khẩu thất bại!';
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
                    className="fixed inset-0 z-[200] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto"
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
                        <div className="flex items-center justify-between mb-6">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 bg-amber-500/20 rounded-xl flex items-center justify-center">
                                    <KeyRound size={20} className="text-amber-400" />
                                </div>
                                <div>
                                    <h2 className="text-white font-bold text-lg">Đặt lại mật khẩu</h2>
                                    <p className="text-slate-500 text-[10px] font-bold uppercase tracking-wider">
                                        Tài khoản: <span className="text-amber-400">{userName}</span>
                                    </p>
                                </div>
                            </div>
                            <button onClick={handleClose} className="p-2 hover:bg-white/10 rounded-lg transition text-slate-400 hover:text-white">
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="space-y-3">
                            <div>
                                <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 block">Mật khẩu mới</label>
                                <div className="relative">
                                    <Lock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                                    <input
                                        type={showNew ? 'text' : 'password'}
                                        value={newPassword}
                                        onChange={(e) => setNewPassword(e.target.value)}
                                        placeholder="Tối thiểu 6 ký tự"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-9 pr-9 text-white text-sm focus:outline-none focus:border-amber-500/50"
                                    />
                                    <button type="button" onClick={() => setShowNew(!showNew)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500">
                                        {showNew ? <EyeOff size={14} /> : <Eye size={14} />}
                                    </button>
                                </div>
                            </div>

                            <div>
                                <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 block">Xác nhận mật khẩu</label>
                                <div className="relative">
                                    <ShieldCheck size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                                    <input
                                        type={showConfirm ? 'text' : 'password'}
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        placeholder="Nhập lại mật khẩu mới"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl py-2.5 pl-9 pr-9 text-white text-sm focus:outline-none focus:border-amber-500/50"
                                    />
                                    <button type="button" onClick={() => setShowConfirm(!showConfirm)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500">
                                        {showConfirm ? <EyeOff size={14} /> : <Eye size={14} />}
                                    </button>
                                </div>
                            </div>

                            <div className="flex gap-3 pt-2">
                                <button type="button" onClick={handleClose} className="flex-1 py-2.5 rounded-xl text-sm font-bold text-slate-400 bg-white/5 border border-white/10">Huỷ</button>
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white bg-amber-600 hover:bg-amber-500 disabled:opacity-50 flex items-center justify-center gap-2"
                                >
                                    {loading ? 'Đang xử lý...' : 'Xác nhận'}
                                </button>
                            </div>
                        </form>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default ResetPasswordModal;