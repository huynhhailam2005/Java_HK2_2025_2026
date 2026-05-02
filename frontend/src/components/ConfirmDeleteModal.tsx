import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, Trash2, Loader2 } from 'lucide-react';

interface ConfirmDeleteModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    loading?: boolean;
}

export default function ConfirmDeleteModal({ isOpen, onClose, onConfirm, title, loading }: ConfirmDeleteModalProps) {
    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="fixed inset-0 bg-black/90 backdrop-blur-md z-[200] flex items-center justify-center p-4"
            >
                <motion.div
                    initial={{ scale: 0.9, y: 20, rotateX: 10 }}
                    animate={{ scale: 1, y: 0, rotateX: 0 }}
                    exit={{ scale: 0.9, opacity: 0 }}
                    className="bg-[#0f172a] border border-red-500/30 rounded-[2rem] p-8 w-full max-w-md shadow-[0_0_50px_rgba(239,68,68,0.2)] relative overflow-hidden text-center"
                >
                    {/* Background Glow */}
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 w-40 h-40 bg-red-500/10 blur-[60px] pointer-events-none"></div>

                    <div className="relative z-10">
                        {/* Icon cảnh báo rung nhẹ */}
                        <motion.div
                            animate={{ rotate: [0, -5, 5, -5, 5, 0] }}
                            transition={{ repeat: Infinity, duration: 2, repeatDelay: 3 }}
                            className="w-20 h-20 bg-red-500/20 text-red-500 rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-lg shadow-red-500/20"
                        >
                            <AlertTriangle size={40} />
                        </motion.div>

                        <h2 className="text-2xl font-black text-white mb-3">Xác nhận xóa nhóm?</h2>
                        <p className="text-slate-400 text-sm leading-relaxed mb-8">
                            Bạn đang yêu cầu xóa vĩnh viễn nhóm <span className="text-red-400 font-bold">"{title}"</span>.
                            Mọi dữ liệu Issues, lịch sử GitHub và cấu hình Jira sẽ biến mất mãi mãi.
                        </p>

                        <div className="grid grid-cols-2 gap-4">
                            <button
                                onClick={onClose}
                                disabled={loading}
                                className="bg-white/5 hover:bg-white/10 text-white font-bold py-4 rounded-2xl transition-all border border-white/5 active:scale-95 disabled:opacity-50"
                            >
                                Hủy bỏ
                            </button>
                            <button
                                onClick={onConfirm}
                                disabled={loading}
                                className="bg-red-600 hover:bg-red-500 text-white font-bold py-4 rounded-2xl shadow-xl shadow-red-900/40 transition-all flex items-center justify-center gap-2 active:scale-95 disabled:opacity-50"
                            >
                                {loading ? <Loader2 className="animate-spin" size={20} /> : <Trash2 size={20} />}
                                {loading ? 'Đang xóa...' : 'Xác nhận xóa'}
                            </button>
                        </div>
                    </div>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}