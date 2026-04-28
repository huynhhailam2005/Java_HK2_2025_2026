import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, UserPlus } from 'lucide-react';
import { studentApi, lecturerApi } from '../../services/studentLecturerApi';
import { showLiquidToast } from '../../utils/toast';

interface CreateUserModalProps {
    isOpen: boolean;
    onClose: () => void;
    role: 'STUDENT' | 'LECTURER';
    onSuccess: () => void;
}

export default function CreateUserModal({ isOpen, onClose, role, onSuccess }: CreateUserModalProps) {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        code: '',
        jiraAccountId: '',
        githubUsername: ''
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (formData.password.length < 6) {
            showLiquidToast('Mật khẩu phải chứa ít nhất 6 ký tự!', 'error');
            return;
        }

        setLoading(true);
        try {
            if (role === 'STUDENT') {
                await studentApi.create({
                    username: formData.username,
                    email: formData.email,
                    password: formData.password,
                    studentCode: formData.code,
                    jiraAccountId: formData.jiraAccountId || undefined,
                    githubUsername: formData.githubUsername || undefined
                });
            } else {
                await lecturerApi.create({
                    username: formData.username,
                    email: formData.email,
                    password: formData.password,
                    lecturerCode: formData.code
                });
            }
            showLiquidToast('Tạo tài khoản thành công!', 'success');
            setFormData({ username: '', email: '', password: '', code: '', jiraAccountId: '', githubUsername: '' });
            onSuccess();
            onClose();
        } catch (error: unknown) {
            // Fix chuẩn Type để ESLint không chửi "Unexpected any"
            const err = error as { response?: { data?: { message?: string, errors?: Record<string, string> } } };
            let errorMsg = err.response?.data?.message || 'Lỗi hệ thống khi tạo tài khoản (Hãy kiểm tra log Backend)!';

            if (err.response?.data?.errors) {
                const errObj = err.response.data.errors;
                if (errObj.password) errorMsg = errObj.password;
                else if (errObj.username) errorMsg = errObj.username;
                else if (errObj.email) errorMsg = errObj.email;
                else errorMsg = Object.values(errObj)[0];
            } else if (errorMsg.includes('could not execute statement')) {
                errorMsg = 'Dữ liệu bị trùng lặp (Tên đăng nhập hoặc Email đã tồn tại)!';
            }

            showLiquidToast(errorMsg, 'error');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 z-100 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="bg-[#0f172a] border border-white/10 rounded-3xl w-full max-w-lg overflow-hidden shadow-2xl">
                    <div className="p-6 border-b border-white/10 flex justify-between items-center bg-white/5">
                        <h2 className="text-xl font-black text-white flex items-center gap-2">
                            <UserPlus className="w-5 h-5 text-blue-400" />
                            TẠO {role === 'STUDENT' ? 'SINH VIÊN' : 'GIẢNG VIÊN'} MỚI
                        </h2>
                        <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors"><X /></button>
                    </div>

                    <form onSubmit={handleSubmit} className="p-6 space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Tên đăng nhập</label>
                                <input required type="text" value={formData.username} onChange={(e) => setFormData({...formData, username: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                            </div>
                            <div className="space-y-1">
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Mật khẩu</label>
                                <input required minLength={6} type="password" value={formData.password} onChange={(e) => setFormData({...formData, password: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                            </div>
                        </div>

                        <div className="space-y-1">
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Email</label>
                            <input required type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <div className="space-y-1">
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Mã {role === 'STUDENT' ? 'Sinh viên' : 'Giảng viên'}</label>
                            <input required type="text" value={formData.code} onChange={(e) => setFormData({...formData, code: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                        </div>

                        {role === 'STUDENT' && (
                            <div className="grid grid-cols-2 gap-4 pt-2 border-t border-white/5">
                                <div className="space-y-1">
                                    <label className="text-xs font-bold text-slate-400 ml-1 uppercase">GitHub</label>
                                    <input type="text" value={formData.githubUsername} onChange={(e) => setFormData({...formData, githubUsername: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Jira ID</label>
                                    <input type="text" value={formData.jiraAccountId} onChange={(e) => setFormData({...formData, jiraAccountId: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" />
                                </div>
                            </div>
                        )}

                        <button disabled={loading} type="submit" className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-xl transition-colors mt-4">
                            {loading ? 'Đang xử lý...' : 'Xác nhận tạo'}
                        </button>
                    </form>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}