import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Search, UserCheck, GraduationCap, Loader2 } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { showLiquidToast } from '../utils/toast';

export default function GroupLecturerModal({ isOpen, onClose, group, onSuccess }: any) {
    const [lecturers, setLecturers] = useState<any[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (isOpen) {
            loadLecturers();
            setSearchTerm('');
        }
    }, [isOpen]);

    const loadLecturers = async () => {
        setLoading(true);
        try {
            const res = await adminApi.getUsers();
            if (res.data.success) {
                const allUsers = res.data.data;
                setLecturers(allUsers.filter((u: any) => u.role === 'LECTURER'));
            }
        } catch (err) {
            showLiquidToast('Không thể kết nối danh sách giảng viên', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleSelectLecturer = async (lecturerId: number, lecturerName: string) => {
        if (group?.lecturer === lecturerName) {
            onClose();
            return;
        }

        setSubmitting(true);
        try {
            const res = await adminApi.updateGroupLecturer(group.id, lecturerId);

            if (res.data.success) {
                showLiquidToast(`Đã gán thầy/cô ${lecturerName} hướng dẫn nhóm`, 'success');
                onSuccess();
                onClose();
            }
        } catch (err: any) {
            console.error("Lỗi đổi giảng viên:", err.response?.data);
            const msg = err.response?.data?.message || 'Lỗi xác thực dữ liệu từ Backend';
            showLiquidToast(msg, 'error');
        } finally {
            setSubmitting(false);
        }
    };

    if (!isOpen) return null;

    const filtered = lecturers.filter(l =>
        l.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        l.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <AnimatePresence>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 bg-black/80 backdrop-blur-md z-100 flex items-center justify-center p-4">
                <motion.div initial={{ scale: 0.95, y: 20 }} animate={{ scale: 1, y: 0 }} className="bg-[#0f172a] border border-white/10 rounded-[2.5rem] p-8 w-full max-w-lg shadow-2xl relative overflow-hidden">

                    {/* Header Modal */}
                    <div className="flex justify-between items-center mb-8">
                        <div className="flex items-center gap-3">
                            <div className="p-3 bg-emerald-500/20 rounded-2xl text-emerald-400">
                                <GraduationCap size={24}/>
                            </div>
                            <div>
                                <h2 className="text-xl font-bold text-white leading-none">Điều chỉnh hướng dẫn</h2>
                                <p className="text-slate-500 text-xs mt-1">Đề tài: {group?.name}</p>
                            </div>
                        </div>
                        <button onClick={onClose} className="p-2 text-slate-400 hover:text-white transition-colors"><X size={24}/></button>
                    </div>

                    {/* Thanh tìm kiếm */}
                    <div className="relative mb-6">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" size={18}/>
                        <input
                            className="w-full bg-black/40 border border-white/10 rounded-2xl py-4 pl-12 pr-4 text-white outline-none focus:ring-2 focus:ring-emerald-500/50 transition-all text-sm"
                            placeholder="Gõ tên hoặc email giảng viên..."
                            value={searchTerm}
                            onChange={e => setSearchTerm(e.target.value)}
                        />
                    </div>

                    {/* Danh sách giảng viên */}
                    <div className="max-h-[350px] overflow-y-auto space-y-2 pr-2 custom-scrollbar">
                        {loading ? (
                            <div className="text-center py-10 text-slate-500 animate-pulse italic text-sm">Đang truy xuất danh sách...</div>
                        ) : filtered.length === 0 ? (
                            <div className="text-center py-10 text-slate-600 italic text-sm">Không tìm thấy giảng viên nào.</div>
                        ) : filtered.map(l => (
                            <div
                                key={l.id}
                                onClick={() => !submitting && handleSelectLecturer(l.id, l.username)}
                                className={`group flex items-center justify-between p-4 rounded-2xl cursor-pointer transition-all border ${group?.lecturer === l.username ? 'bg-emerald-500/20 border-emerald-500/50' : 'bg-white/[0.03] border-transparent hover:border-white/10 hover:bg-white/[0.05]'}`}
                            >
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center font-bold text-slate-400 group-hover:text-emerald-400 transition-colors">
                                        {l.username.charAt(0).toUpperCase()}
                                    </div>
                                    <div>
                                        <div className="text-white font-bold text-sm">{l.username}</div>
                                        <div className="text-[10px] text-slate-500 font-mono">{l.email}</div>
                                    </div>
                                </div>

                                {submitting ? (
                                    <Loader2 size={18} className="animate-spin text-slate-500" />
                                ) : group?.lecturer === l.username ? (
                                    <div className="flex items-center gap-2 text-emerald-400">
                                        <span className="text-[10px] font-black uppercase tracking-tighter">Hiện tại</span>
                                        <UserCheck size={20} />
                                    </div>
                                ) : (
                                    <div className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all">
                                        <UserCheck size={16} className="text-slate-400" />
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>

                    <div className="mt-8 pt-4 border-t border-white/5 text-center">
                        <p className="text-[9px] text-slate-600 uppercase font-bold tracking-[0.2em]">Hệ thống quản lý nghiên cứu khoa học SRPM</p>
                    </div>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}