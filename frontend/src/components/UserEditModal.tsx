import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Save, User, Hash, Loader2, KeyRound, Trello, Github } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { showLiquidToast } from '../utils/toast';
import ResetPasswordModal from './ResetPasswordModal';

interface UserEditModalProps {
    userId: number;
    userRole: 'STUDENT' | 'LECTURER';
    isOpen: boolean;
    onClose: () => void;
    onSave: () => void;
}

export default function UserEditModal({ userId, userRole, isOpen, onClose, onSave }: UserEditModalProps) {
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [isResetPasswordOpen, setIsResetPasswordOpen] = useState(false);

    const [formData, setFormData] = useState({
        username: '',
        email: '',
        studentCode: '',
        jiraAccountId: '',
        githubUsername: '',
        lecturerCode: ''
    });

    useEffect(() => {
        if (isOpen && userId) {
            loadUserData();
        }
    }, [isOpen, userId]);

    const loadUserData = async () => {
        setLoading(true);
        try {
            const res = await adminApi.getUserById(userId);
            if (res.data.success) {
                const u: any = res.data.data;
                setFormData({
                    username: u.username || '',
                    email: u.email || '',
                    studentCode: u.studentCode || '',
                    jiraAccountId: u.jiraAccountId || '',
                    githubUsername: u.githubUsername || '',
                    lecturerCode: u.lecturerCode || ''
                });
            }
        } catch (error) {
            showLiquidToast('Loi tai ho so nguoi dung', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            const payload: any = {
                username: formData.username,
                email: formData.email,
            };

            if (userRole === 'STUDENT') {
                payload.studentCode = formData.studentCode;
                payload.jiraAccountId = formData.jiraAccountId;
                payload.githubUsername = formData.githubUsername;
            } else {
                payload.lecturerCode = formData.lecturerCode;
            }

            const res = await adminApi.updateUser(userId, payload);
            if (res.data.success) {
                showLiquidToast('Cap nhat thanh cong!', 'success');
                onSave();
                onClose();
            }
        } catch (error: any) {
            showLiquidToast(error.response?.data?.message || 'Loi luu du lieu', 'error');
        } finally {
            setSaving(false);
        }
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4"
                >
                    <motion.div
                        initial={{ scale: 0.95, y: 20 }}
                        animate={{ scale: 1, y: 0 }}
                        exit={{ scale: 0.95, y: 20 }}
                        className="bg-[#0f172a] border border-white/10 rounded-[2.5rem] p-8 w-full max-w-2xl shadow-2xl overflow-y-auto max-h-[90vh]"
                    >
                        <div className="flex justify-between items-center mb-8 border-b border-white/5 pb-6">
                            <div className="flex items-center gap-4">
                                <div className="p-3 bg-blue-500/20 rounded-2xl text-blue-400"><User size={24}/></div>
                                <h2 className="text-2xl font-black text-white">Chỉnh Sửa {userRole}</h2>
                            </div>
                            <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors"><X size={24}/></button>
                        </div>

                        {loading ? (
                            <div className="text-center py-20 animate-pulse text-slate-500 font-bold">ĐANG TRUY XUẤT...</div>
                        ) : (
                            <form onSubmit={handleSubmit} className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1">Username</label>
                                        <input required className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50" value={formData.username} onChange={e => setFormData({...formData, username: e.target.value})}/>
                                    </div>
                                    <div className="space-y-2">
                                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1">Email</label>
                                        <input required type="email" className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50" value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})}/>
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <label className="text-[10px] font-black text-blue-400 uppercase tracking-widest ml-1">{userRole === 'STUDENT' ? 'Mã Sinh Viên' : 'Mã Giảng Viên'}</label>
                                    <div className="relative">
                                        <Hash className="absolute left-4 top-1/2 -translate-y-1/2 text-blue-400" size={18}/>
                                        <input required className="w-full bg-blue-500/5 border border-blue-500/20 rounded-2xl py-4 pl-12 pr-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50 font-mono" value={userRole === 'STUDENT' ? formData.studentCode : formData.lecturerCode} onChange={e => userRole === 'STUDENT' ? setFormData({...formData, studentCode: e.target.value}) : setFormData({...formData, lecturerCode: e.target.value})}/>
                                    </div>
                                </div>

                                {userRole === 'STUDENT' && (
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 flex items-center gap-2"><Trello size={12}/> Jira Account ID</label>
                                            <input className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50 font-mono text-sm" placeholder="71202:8e68..." value={formData.jiraAccountId} onChange={e => setFormData({...formData, jiraAccountId: e.target.value})}/>
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 flex items-center gap-2"><Github size={12}/> GitHub</label>
                                            <input className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50" placeholder="Username" value={formData.githubUsername} onChange={e => setFormData({...formData, githubUsername: e.target.value})}/>
                                        </div>
                                    </div>
                                )}

                                <div className="border-t border-white/5 pt-6">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <label className="text-[10px] font-black text-amber-400 uppercase tracking-widest ml-1">Mật khẩu</label>
                                            <p className="text-xs text-slate-500 ml-1 mt-1">Thiết lập mật khẩu mới cho người dùng</p>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setIsResetPasswordOpen(true)}
                                            className="flex items-center gap-2 px-5 py-2.5 bg-amber-500/10 border border-amber-500/30 text-amber-400 hover:bg-amber-500/20 rounded-xl text-sm font-bold transition-all"
                                        >
                                            <KeyRound size={16}/>
                                            Đặt lại mật khẩu
                                        </button>
                                    </div>
                                </div>

                                <button disabled={saving} type="submit" className="w-full bg-blue-600 hover:bg-blue-500 text-white p-5 rounded-[1.5rem] font-black shadow-xl shadow-blue-900/20 transition-all flex justify-center items-center gap-3 disabled:opacity-50">
                                    {saving ? <Loader2 className="animate-spin" size={24}/> : <Save size={24}/>}
                                    {saving ? 'ĐANG LƯU...' : 'LƯU THAY ĐỔI'}
                                </button>
                            </form>
                        )}

                        <ResetPasswordModal
                            isOpen={isResetPasswordOpen}
                            onClose={() => setIsResetPasswordOpen(false)}
                            userId={userId}
                            userName={formData.username}
                            userEmail={formData.email}
                        />
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
}




