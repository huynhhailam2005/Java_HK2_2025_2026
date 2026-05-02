import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Save, FolderPlus, Search, CheckCircle2, Loader2 } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { showLiquidToast } from '../utils/toast';

export default function CreateGroupModal({ isOpen, onClose, onSuccess }: any) {
    const [loading, setLoading] = useState(false);
    const [lecturers, setLecturers] = useState<any[]>([]);
    const [formData, setFormData] = useState({
        groupCode: '',
        groupName: '',
        lecturerId: 0
    });
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (isOpen) {
            loadLecturers();
            setFormData({ groupCode: '', groupName: '', lecturerId: 0 });
            setSearchTerm('');
        }
    }, [isOpen]);

    const loadLecturers = async () => {
        try {
            const res = await adminApi.getUsers();
            if (res.data.success) {
                setLecturers(res.data.data.filter((u: any) => u.role === 'LECTURER'));
            }
        } catch (err) {
            console.error(err);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (formData.lecturerId === 0) {
            return showLiquidToast('Vui lòng chọn một giảng viên hướng dẫn!', 'error');
        }

        setLoading(true);
        try {
            const res = await adminApi.createGroup(formData);
            if (res.data.success) {
                showLiquidToast('Khởi tạo nhóm đồ án thành công!', 'success');
                onSuccess();
                onClose();
            }
        } catch (error: any) {
            const msg = error.response?.data?.message || 'Lỗi khi tạo nhóm';
            showLiquidToast(msg, 'error');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    const filteredLecturers = lecturers.filter(l =>
        l.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
        l.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <AnimatePresence>
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="fixed inset-0 bg-black/85 backdrop-blur-xl z-100 flex items-center justify-center p-4"
            >
                <motion.div
                    initial={{ scale: 0.9, y: 30 }}
                    animate={{ scale: 1, y: 0 }}
                    className="bg-[#0a0f1e] border border-white/10 rounded-[3rem] p-10 w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-[0_0_80px_rgba(37,99,235,0.2)] relative"
                >
                    {/* Header */}
                    <div className="flex justify-between items-start mb-10">
                        <div className="flex items-center gap-5">
                            <div className="p-4 bg-gradient-to-br from-blue-600 to-blue-400 rounded-3xl text-white shadow-lg shadow-blue-500/30">
                                <FolderPlus size={32}/>
                            </div>
                            <div>
                                <h2 className="text-3xl font-black text-white tracking-tighter">Tạo Nhóm Đồ Án</h2>
                                <p className="text-slate-500 text-sm font-medium">Thiết lập thông tin cơ bản và người hướng dẫn</p>
                            </div>
                        </div>
                        <button onClick={onClose} className="p-2 text-slate-500 hover:text-white hover:bg-white/5 rounded-full transition-all">
                            <X size={28}/>
                        </button>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-10">
                        {/* Group Info Section */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="space-y-2">
                                <label className="text-[11px] font-black text-blue-400 uppercase tracking-widest ml-1">Mã định danh</label>
                                <input
                                    required
                                    className="w-full bg-white/[0.03] border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50 transition-all font-mono"
                                    placeholder="Ví dụ: G2024-01"
                                    value={formData.groupCode}
                                    onChange={e => setFormData({...formData, groupCode: e.target.value})}
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-[11px] font-black text-blue-400 uppercase tracking-widest ml-1">Tên Đề Tài</label>
                                <input
                                    required
                                    className="w-full bg-white/[0.03] border border-white/10 rounded-2xl p-4 text-white outline-none focus:ring-2 focus:ring-blue-500/50 transition-all"
                                    placeholder="Nhập tên đồ án..."
                                    value={formData.groupName}
                                    onChange={e => setFormData({...formData, groupName: e.target.value})}
                                />
                            </div>
                        </div>

                        {/* Lecturer Selection Section - ĐÃ LÀM ĐẸP TẠI ĐÂY */}
                        <div className="space-y-4">
                            <div className="flex justify-between items-end px-1">
                                <label className="text-[11px] font-black text-emerald-400 uppercase tracking-widest">Giảng viên hướng dẫn</label>
                                <span className="text-[10px] text-slate-500 font-bold uppercase tracking-tighter">Bắt buộc</span>
                            </div>

                            <div className="relative group">
                                <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-blue-400 transition-colors" size={20}/>
                                <input
                                    className="w-full bg-black/40 border border-white/10 rounded-2xl py-4 pl-14 pr-4 text-white text-sm outline-none focus:border-blue-500/50 transition-all shadow-inner"
                                    placeholder="Tìm theo tên hoặc email giảng viên..."
                                    value={searchTerm}
                                    onChange={e => setSearchTerm(e.target.value)}
                                />
                            </div>

                            <div className="grid grid-cols-1 gap-3 max-h-64 overflow-y-auto custom-scrollbar pr-2 pt-1">
                                {filteredLecturers.length > 0 ? filteredLecturers.map(l => (
                                    <motion.div
                                        whileHover={{ scale: 1.01 }}
                                        whileTap={{ scale: 0.99 }}
                                        key={l.id}
                                        onClick={() => setFormData({...formData, lecturerId: l.id})}
                                        className={`group flex items-center justify-between p-4 rounded-[1.5rem] cursor-pointer transition-all border ${
                                            formData.lecturerId === l.id
                                                ? 'bg-blue-600/10 border-blue-500/50 shadow-[0_0_20px_rgba(37,99,235,0.1)]'
                                                : 'bg-white/[0.02] border-transparent hover:bg-white/[0.05] hover:border-white/10'
                                        }`}
                                    >
                                        <div className="flex items-center gap-4">
                                            <div className={`w-12 h-12 rounded-2xl flex items-center justify-center font-black transition-all ${
                                                formData.lecturerId === l.id ? 'bg-blue-500 text-white' : 'bg-white/5 text-slate-400 group-hover:bg-white/10'
                                            }`}>
                                                {l.username.charAt(0).toUpperCase()}
                                            </div>
                                            <div>
                                                <div className="text-white font-bold text-sm flex items-center gap-2">
                                                    {l.username}
                                                    {formData.lecturerId === l.id && <span className="text-[9px] bg-blue-500 text-white px-2 py-0.5 rounded-full uppercase tracking-tighter">Đã chọn</span>}
                                                </div>
                                                <div className="text-[11px] text-slate-500 font-mono mt-0.5">{l.email}</div>
                                            </div>
                                        </div>
                                        <div className={`w-8 h-8 rounded-full flex items-center justify-center transition-all ${
                                            formData.lecturerId === l.id ? 'bg-blue-500 text-white' : 'bg-white/5 text-slate-700'
                                        }`}>
                                            <CheckCircle2 size={18} />
                                        </div>
                                    </motion.div>
                                )) : (
                                    <div className="text-center py-10 text-slate-600 italic text-sm">Không tìm thấy giảng viên nào...</div>
                                )}
                            </div>
                        </div>

                        {/* Footer Action */}
                        <div className="pt-4">
                            <button
                                disabled={loading}
                                type="submit"
                                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white p-5 rounded-3xl font-black shadow-2xl shadow-blue-500/30 transition-all flex justify-center items-center gap-3 active:scale-[0.98] disabled:opacity-50"
                            >
                                {loading ? <Loader2 className="animate-spin" size={24}/> : <Save size={24}/>}
                                {loading ? 'ĐANG KHỞI TẠO HỆ THỐNG...' : 'XÁC NHẬN TẠO NHÓM MỚI'}
                            </button>
                            <p className="text-center text-[10px] text-slate-600 mt-6 uppercase tracking-[0.3em] font-bold">SRPM Management System v2.0</p>
                        </div>
                    </form>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}