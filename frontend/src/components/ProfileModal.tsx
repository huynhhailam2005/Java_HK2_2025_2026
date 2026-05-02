import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, User, Save, Edit3, Mail, Hash, Github, AtSign } from 'lucide-react';
import { userApi } from '../services/userApi';
import { showLiquidToast } from '../utils/toast';

interface ProfileModalProps {
    isOpen: boolean;
    onClose: () => void;
    userId: number;
}

const ProfileModal = ({ isOpen, onClose, userId }: ProfileModalProps) => {
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [editing, setEditing] = useState(false);
    const [profile, setProfile] = useState<any>(null);
    const [loadError, setLoadError] = useState(false);

    // Form fields
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [studentCode, setStudentCode] = useState('');
    const [lecturerCode, setLecturerCode] = useState('');
    const [jiraAccountId, setJiraAccountId] = useState('');
    const [githubUsername, setGithubUsername] = useState('');

    const getLocalUser = () => {
        try {
            const data = localStorage.getItem('user');
            return data ? JSON.parse(data) : null;
        } catch { return null; }
    };

    useEffect(() => {
        if (isOpen && userId) {
            loadProfile();
        }
    }, [isOpen, userId]);

    const loadProfile = async () => {
        setLoading(true);
        setLoadError(false);

        const localUser = getLocalUser();
        const baseData = {
            id: localUser?.id || userId,
            username: localUser?.username || '',
            email: localUser?.email || '',
            role: localUser?.role || 'STUDENT',
            studentCode: '',
            lecturerCode: '',
            jiraAccountId: '',
            githubUsername: '',
        };

        setProfile(baseData);
        setUsername(baseData.username);
        setEmail(baseData.email);
        setStudentCode('');
        setLecturerCode('');
        setJiraAccountId('');
        setGithubUsername('');

        try {
            const res = await userApi.getProfile(userId);
            if (res.data.success) {
                const data = res.data.data;
                const merged = { ...baseData, ...data };
                setProfile(merged);
                setUsername(merged.username || '');
                setEmail(merged.email || '');
                setStudentCode(merged.studentCode || '');
                setLecturerCode(merged.lecturerCode || '');
                setJiraAccountId(merged.jiraAccountId || '');
                setGithubUsername(merged.githubUsername || '');
            }
        } catch (err: any) {
            setLoadError(true);
        } finally {
            setLoading(false);
        }
    };

    const resetForm = () => {
        setEditing(false);
        if (profile) {
            setUsername(profile.username || '');
            setEmail(profile.email || '');
            setStudentCode(profile.studentCode || '');
            setLecturerCode(profile.lecturerCode || '');
            setJiraAccountId(profile.jiraAccountId || '');
            setGithubUsername(profile.githubUsername || '');
        }
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    const handleSave = async () => {
        if (!username.trim()) {
            showLiquidToast('Username không được để trống', 'error');
            return;
        }
        if (!email.trim()) {
            showLiquidToast('Email không được để trống', 'error');
            return;
        }

        setSaving(true);
        try {
            if (profile?.role === 'LECTURER') {
                const res = await userApi.updateLecturerProfile(userId, {
                    username: username.trim(),
                    email: email.trim(),
                    lecturerId: lecturerCode.trim() || undefined,
                });
                if (res.data.success) {
                    showLiquidToast('Cập nhật thông tin thành công!', 'success');
                    updateLocalUser(username.trim(), email.trim());
                    setEditing(false);
                    loadProfile();
                } else {
                    showLiquidToast(res.data.message || 'Cập nhật thất bại', 'error');
                }
            } else if (profile?.role === 'STUDENT') {
                const data: any = {
                    username: username.trim(),
                    email: email.trim(),
                    studentId: studentCode.trim() || undefined,
                };
                if (jiraAccountId.trim()) data.jiraAccountId = jiraAccountId.trim();
                if (githubUsername.trim()) data.githubUsername = githubUsername.trim();

                const res = await userApi.updateStudentProfile(userId, data);
                if (res.data.success) {
                    showLiquidToast('Cập nhật thông tin thành công!', 'success');
                    updateLocalUser(username.trim(), email.trim());
                    setEditing(false);
                    loadProfile();
                } else {
                    showLiquidToast(res.data.message || 'Cập nhật thất bại', 'error');
                }
            } else {
                const res = await userApi.updateProfile(userId, {
                    username: username.trim(),
                    email: email.trim(),
                });
                if (res.data.success) {
                    showLiquidToast('Cập nhật thông tin thành công!', 'success');
                    updateLocalUser(username.trim(), email.trim());
                    setEditing(false);
                    loadProfile();
                } else {
                    showLiquidToast(res.data.message || 'Cập nhật thất bại', 'error');
                }
            }
        } catch (err: any) {
            const msg = err?.response?.data?.message || 'Cập nhật thất bại';
            showLiquidToast(msg, 'error');
        } finally {
            setSaving(false);
        }
    };

    const updateLocalUser = (newUsername: string, newEmail: string) => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            try {
                const parsed = JSON.parse(storedUser);
                parsed.username = newUsername;
                parsed.email = newEmail;
                localStorage.setItem('user', JSON.stringify(parsed));
            } catch { /* ignore */ }
        }
    };

    const isStudent = profile?.role === 'STUDENT';
    const isLecturer = profile?.role === 'LECTURER';

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
                        className="bg-[#151b2b] border border-white/10 rounded-2xl p-6 w-full max-w-lg shadow-2xl max-h-[85vh] overflow-y-auto my-auto"
                    >
                        {/* Header */}
                        <div className="flex items-center justify-between mb-6">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 bg-blue-600/20 rounded-xl flex items-center justify-center">
                                    <User size={20} className="text-blue-400" />
                                </div>
                                <div>
                                    <h2 className="text-white font-bold text-lg">Thông tin cá nhân</h2>
                                    <p className="text-slate-500 text-[10px] font-bold uppercase tracking-wider">
                                        {isStudent ? 'Sinh viên' : isLecturer ? 'Giảng viên' : 'Quản trị viên'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                {!editing && (
                                    <button
                                        onClick={() => setEditing(true)}
                                        className="p-2 bg-amber-600/20 text-amber-400 rounded-xl hover:bg-amber-600 hover:text-white transition-all"
                                        title="Chỉnh sửa"
                                    >
                                        <Edit3 size={16} />
                                    </button>
                                )}
                                <button
                                    onClick={handleClose}
                                    className="p-2 hover:bg-white/10 rounded-lg transition text-slate-400 hover:text-white"
                                >
                                    <X size={20} />
                                </button>
                            </div>
                        </div>

                        {loading ? (
                            <div className="py-16 text-center">
                                <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto" />
                                <p className="text-slate-500 text-sm mt-4">Đang tải...</p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {/* Avatar */}
                                <div className="flex justify-center mb-4">
                                    <div className="h-20 w-20 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 flex items-center justify-center text-3xl font-black border-2 border-white/20 shadow-xl">
                                        {profile?.username ? profile.username.charAt(0).toUpperCase() : 'U'}
                                    </div>
                                </div>

                                {/* Username */}
                                <div>
                                    <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                        <User size={12} /> Tên đăng nhập
                                    </label>
                                    {editing ? (
                                        <input
                                            type="text"
                                            value={username}
                                            onChange={(e) => setUsername(e.target.value)}
                                            className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm focus:outline-none focus:border-blue-500/50 transition-all"
                                        />
                                    ) : (
                                        <p className="text-white font-semibold">{profile?.username}</p>
                                    )}
                                </div>

                                {/* Email */}
                                <div>
                                    <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                        <Mail size={12} /> Email
                                    </label>
                                    {editing ? (
                                        <input
                                            type="email"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm focus:outline-none focus:border-blue-500/50 transition-all"
                                        />
                                    ) : (
                                        <p className="text-white">{profile?.email}</p>
                                    )}
                                </div>

                                {/* Student Code / Lecturer Code */}
                                {isStudent && (
                                    <div>
                                        <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                            <Hash size={12} /> Mã sinh viên
                                        </label>
                                        {editing ? (
                                            <input
                                                type="text"
                                                value={studentCode}
                                                onChange={(e) => setStudentCode(e.target.value)}
                                                placeholder="Nhập mã sinh viên"
                                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
                                            />
                                        ) : (
                                            <p className="text-white font-mono">{profile?.studentCode || '—'}</p>
                                        )}
                                    </div>
                                )}
                                {isLecturer && (
                                    <div>
                                        <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                            <Hash size={12} /> Mã giảng viên
                                        </label>
                                        {editing ? (
                                            <input
                                                type="text"
                                                value={lecturerCode}
                                                onChange={(e) => setLecturerCode(e.target.value)}
                                                placeholder="Nhập mã giảng viên"
                                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
                                            />
                                        ) : (
                                            <p className="text-white font-mono">{profile?.lecturerCode || '—'}</p>
                                        )}
                                    </div>
                                )}

                                {/* Jira Account ID - chỉ Student */}
                                {isStudent && (
                                    <div>
                                        <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                            <AtSign size={12} /> Jira Account ID
                                        </label>
                                        {editing ? (
                                            <input
                                                type="text"
                                                value={jiraAccountId}
                                                onChange={(e) => setJiraAccountId(e.target.value)}
                                                placeholder="Nhập Jira Account ID"
                                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
                                            />
                                        ) : (
                                            <p className="text-white font-mono text-sm">{profile?.jiraAccountId || '—'}</p>
                                        )}
                                    </div>
                                )}

                                {/* GitHub Username - chỉ Student */}
                                {isStudent && (
                                    <div>
                                        <label className="text-slate-400 text-[10px] font-bold uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                                            <Github size={12} /> GitHub Username
                                        </label>
                                        {editing ? (
                                            <input
                                                type="text"
                                                value={githubUsername}
                                                onChange={(e) => setGithubUsername(e.target.value)}
                                                placeholder="Nhập GitHub username"
                                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm placeholder:text-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
                                            />
                                        ) : (
                                            <p className="text-white font-mono text-sm">{profile?.githubUsername || '—'}</p>
                                        )}
                                    </div>
                                )}

                                {/* Thông báo nếu API lỗi */}
                                {loadError && !editing && (
                                    <div className="bg-amber-600/10 border border-amber-500/30 rounded-xl p-3 text-center">
                                        <p className="text-amber-400 text-[10px] font-bold">
                                            ⚠ Không thể tải thông tin chi tiết. Vui lòng thử lại.
                                        </p>
                                    </div>
                                )}

                                {/* Buttons */}
                                {editing && (
                                    <div className="flex gap-3 pt-2 border-t border-white/10 mt-6">
                                        <button
                                            onClick={resetForm}
                                            className="flex-1 py-2.5 rounded-xl text-sm font-bold text-slate-400 bg-white/5 border border-white/10 hover:bg-white/10 transition-all active:scale-95"
                                        >
                                            Huỷ
                                        </button>
                                        <button
                                            onClick={handleSave}
                                            disabled={saving}
                                            className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white bg-blue-600 hover:bg-blue-500 transition-all active:scale-95 shadow-lg shadow-blue-600/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                                        >
                                            {saving ? (
                                                <>
                                                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                                                    </svg>
                                                    Đang lưu...
                                                </>
                                            ) : (
                                                <>
                                                    <Save size={16} /> Lưu thay đổi
                                                </>
                                            )}
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default ProfileModal;
