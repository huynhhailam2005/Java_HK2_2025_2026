import { motion, AnimatePresence } from 'framer-motion';
import { X, Calendar, User, FileText, Tag, Clock } from 'lucide-react';

interface IssueDetailModalProps {
    isOpen: boolean;
    onClose: () => void;
    issue: any;
}

const IssueDetailModal = ({ isOpen, onClose, issue }: IssueDetailModalProps) => {
    if (!issue) return null;

    const formatDate = (dateString: string) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString('vi-VN');
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto"
                    onClick={onClose}
                >
                    <motion.div
                        initial={{ scale: 0.9, opacity: 0, y: 20 }}
                        animate={{ scale: 1, opacity: 1, y: 0 }}
                        exit={{ scale: 0.9, opacity: 0, y: 20 }}
                        transition={{ type: 'spring', stiffness: 300, damping: 25 }}
                        onClick={(e) => e.stopPropagation()}
                        className="bg-[#0f172a] border border-white/10 rounded-2xl p-6 w-full max-w-2xl shadow-2xl max-h-[85vh] overflow-y-auto my-auto"
                    >
                        {/* Header cố định */}
                        <div className="p-6 border-b border-white/10 flex justify-between items-center bg-white/[0.02] shrink-0">
                            <div className="flex items-center gap-3">
                                <div className={`px-2.5 py-1 rounded-lg text-[10px] font-black border uppercase tracking-widest ${
                                    issue.type === 'EPIC' ? 'border-purple-500/30 text-purple-400 bg-purple-500/10' :
                                        issue.type === 'SUB_TASK' ? 'border-cyan-500/30 text-cyan-400 bg-cyan-500/10' : 'border-blue-500/30 text-blue-400 bg-blue-500/10'
                                }`}>
                                    {issue.type}
                                </div>
                                <h2 className="text-white font-black uppercase text-sm tracking-widest">Chi tiết Issue</h2>
                            </div>
                            <button
                                onClick={onClose}
                                className="p-2 text-slate-400 hover:text-white hover:bg-white/10 rounded-full transition-all"
                            >
                                <X size={24} />
                            </button>
                        </div>

                        {/* Nội dung có thể cuộn */}
                        <div className="flex-1 overflow-y-auto p-8 custom-scrollbar space-y-8">
                            <div>
                                <h3 className="text-3xl font-black text-white mb-3 leading-tight">{issue.title}</h3>
                                <div className="flex items-center gap-4">
                                    <span className="flex items-center gap-1.5 bg-white/5 px-3 py-1 rounded-md border border-white/5 text-xs font-mono text-blue-400">
                                        <Tag size={14} />
                                        {issue.issueCode || `#${issue.issueId}`}
                                    </span>
                                    <span className={`px-3 py-1 rounded-md border text-[10px] font-black uppercase ${
                                        issue.status === 'DONE' ? 'border-emerald-500/30 text-emerald-400 bg-emerald-500/5' :
                                            issue.status === 'IN_PROGRESS' ? 'border-blue-500/30 text-blue-400 bg-blue-500/5' : 'border-slate-500/30 text-slate-400 bg-white/5'
                                    }`}>
                                        {issue.status}
                                    </span>
                                </div>
                            </div>

                            {/* Mô tả */}
                            {issue.description && (
                                <div className="space-y-3">
                                    <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-2">
                                        <FileText size={14} /> Description
                                    </label>
                                    <div className="bg-black/20 border border-white/5 rounded-2xl p-5 text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">
                                        {issue.description}
                                    </div>
                                </div>
                            )}

                            {/* Grid thông tin */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div className="space-y-3">
                                    <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-2">
                                        <User size={14} /> Assignee
                                    </label>
                                    <div className="flex items-center gap-3 bg-white/5 border border-white/10 rounded-2xl p-4">
                                        <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-xs uppercase shadow-lg shadow-blue-900/20">
                                            {issue.assignedTo?.charAt(0) || '?'}
                                        </div>
                                        <span className="text-white font-bold text-sm">{issue.assignedTo || 'Unassigned'}</span>
                                    </div>
                                </div>

                                <div className="space-y-3">
                                    <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest flex items-center gap-2">
                                        <Calendar size={14} /> Deadline
                                    </label>
                                    <div className="bg-white/5 border border-white/10 rounded-2xl p-4 text-white font-mono text-sm">
                                        {issue.deadline ? formatDate(issue.deadline) : 'No Deadline'}
                                    </div>
                                </div>
                            </div>

                            {/* Footer timeline */}
                            <div className="pt-6 border-t border-white/5 flex flex-wrap gap-x-8 gap-y-4">
                                <div className="flex items-center gap-2 text-[10px] font-bold text-slate-600 uppercase tracking-wider">
                                    <Clock size={12} /> Created: {formatDate(issue.createdAt)}
                                </div>
                            </div>
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};

export default IssueDetailModal;