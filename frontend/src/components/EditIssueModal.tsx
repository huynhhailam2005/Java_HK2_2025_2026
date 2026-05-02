import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Save, Calendar, User, FileText, Tag, ListTodo } from 'lucide-react';
import { issueApi } from '../services/issueApi';
import { groupApi } from '../services/groupApi';
import { showLiquidToast } from '../utils/toast';

interface EditIssueModalProps {
    isOpen: boolean;
    onClose: () => void;
    issue: any;
    groupId: number;
    onSuccess: () => void;
    hideAssignee?: boolean;
}

const STATUS_OPTIONS = [
    { value: 'TODO', label: 'To Do', color: 'text-slate-400 bg-slate-500/10 border-slate-500/20' },
    { value: 'IN_PROGRESS', label: 'In Progress', color: 'text-blue-400 bg-blue-500/10 border-blue-500/20' },
    { value: 'DONE', label: 'Done', color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20' },
];

export default function EditIssueModal({ isOpen, onClose, issue, groupId, onSuccess, hideAssignee }: EditIssueModalProps) {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        deadline: '',
        assignedToMemberId: '',
        status: 'TODO',
    });
    const [members, setMembers] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);

    useEffect(() => {
        if (isOpen && issue) {
            setFetching(true);

            if (hideAssignee) {
                setFormData({
                    title: issue.title || '',
                    description: issue.description || '',
                    deadline: issue.deadline ? issue.deadline.split('T')[0] : '',
                    assignedToMemberId: '',
                    status: issue.status || 'TODO',
                });
                setFetching(false);
            } else {
                // Load members cho dropdown assignee
                groupApi.getMembers(groupId)
                    .then(res => {
                        if (res.data.success) {
                            const memberList = (res.data.data as any[]) || [];
                            setMembers(memberList);

                            // Tìm member ID từ username hiện tại
                            let currentMemberId = '';
                            if (issue.assignedTo && issue.assignedTo !== 'Chưa gán') {
                                const matched = memberList.find(
                                    (m: any) => m.studentUsername === issue.assignedTo
                                );
                                if (matched) currentMemberId = String(matched.id);
                            }

                            setFormData({
                                title: issue.title || '',
                                description: issue.description || '',
                                deadline: issue.deadline ? issue.deadline.split('T')[0] : '',
                                assignedToMemberId: currentMemberId,
                                status: issue.status || 'TODO',
                            });
                        }
                    })
                    .catch(() => {})
                    .finally(() => setFetching(false));
            }
        }
    }, [isOpen, issue, groupId, hideAssignee]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!formData.title.trim()) {
            showLiquidToast('Tiêu đề không được để trống!', 'error');
            return;
        }

        setLoading(true);
        try {
            const payload: any = {
                title: formData.title.trim(),
                description: formData.description,
                status: formData.status,
            };

            // Deadline: gửi null nếu rỗng, gửi ISO nếu có
            if (formData.deadline) {
                payload.deadline = formData.deadline + 'T00:00:00';
            } else {
                payload.deadline = '';
            }

            // Assignee: chỉ gửi nếu không hide
            if (!hideAssignee) {
                if (formData.assignedToMemberId) {
                    payload.assignedToMemberId = parseInt(formData.assignedToMemberId);
                } else {
                    // Chọn "Chưa gán" → clear assignee
                    payload.assignedToMemberId = null;
                    payload.clearAssignee = true;
                }
            }

            const res = await issueApi.updateIssue(issue.issueId, payload);
            if (res.data.success) {
                // 🔥 Auto push lên Jira sau khi cập nhật local thành công
                try {
                    if (issue.issueCode) {
                        // Đã có Jira key → push update
                        await issueApi.pushUpdateToJira(issue.issueId);
                    } else {
                        // Chưa có Jira key → push create
                        await issueApi.pushToJira(issue.issueId);
                    }
                } catch (pushErr: any) {
                    const msg = pushErr?.response?.data?.message || 'Lỗi push Jira';
                    showLiquidToast(`Issue đã cập nhật local, nhưng push Jira thất bại: ${msg}`, 'error');
                }

                showLiquidToast('Cập nhật Issue thành công!', 'success');
                onSuccess();
                onClose();
            } else {
                showLiquidToast(res.data.message || 'Lỗi cập nhật Issue!', 'error');
            }
        } catch (error: any) {
            const msg = error?.response?.data?.message || 'Lỗi hệ thống khi cập nhật Issue!';
            showLiquidToast(msg, 'error');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen || !issue) return null;

    return (
        <AnimatePresence>
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
                {/* Header */}
                <div className="p-6 border-b border-white/10 flex justify-between items-center bg-white/[0.02] shrink-0">
                    <div className="flex items-center gap-3">
                        <div className={`px-2.5 py-1 rounded-lg text-[10px] font-black border uppercase tracking-widest ${
                            issue.type === 'EPIC' ? 'border-purple-500/30 text-purple-400 bg-purple-500/10' :
                                issue.type === 'SUB_TASK' ? 'border-cyan-500/30 text-cyan-400 bg-cyan-500/10' : 'border-blue-500/30 text-blue-400 bg-blue-500/10'
                        }`}>
                            {issue.type}
                        </div>
                        <h2 className="text-white font-black uppercase text-sm tracking-widest">SỬA ISSUE</h2>
                    </div>
                    <button onClick={onClose} className="p-2 text-slate-400 hover:text-white hover:bg-white/10 rounded-full transition-all">
                        <X size={24} />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-8 custom-scrollbar space-y-6">
                    {/* Title */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                            <FileText size={12} /> Tiêu đề
                        </label>
                        <input
                            required
                            type="text"
                            value={formData.title}
                            onChange={(e) => setFormData({...formData, title: e.target.value})}
                            className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white font-bold text-lg outline-none focus:ring-2 focus:ring-blue-500/50"
                            placeholder="Nhập tiêu đề Issue..."
                        />
                    </div>

                    {/* Description */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                            <FileText size={12} /> Mô tả
                        </label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({...formData, description: e.target.value})}
                            rows={4}
                            className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white text-sm outline-none focus:ring-2 focus:ring-blue-500/50 resize-none"
                            placeholder="Nhập mô tả Issue..."
                        />
                    </div>

                    {/* Grid: Deadline + Status */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Deadline */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                <Calendar size={12} /> Deadline
                            </label>
                            <input
                                type="date"
                                value={formData.deadline}
                                onChange={(e) => setFormData({...formData, deadline: e.target.value})}
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white outline-none focus:ring-2 focus:ring-blue-500/50"
                            />
                        </div>

                        {/* Status */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                <ListTodo size={12} /> Trạng thái
                            </label>
                            <select
                                value={formData.status}
                                onChange={(e) => setFormData({...formData, status: e.target.value})}
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white outline-none focus:ring-2 focus:ring-blue-500/50 appearance-none cursor-pointer"
                            >
                                {STATUS_OPTIONS.map(opt => (
                                    <option key={opt.value} value={opt.value} className="bg-slate-900">
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    {/* Assignee - ẩn nếu hideAssignee=true */}
                    {!hideAssignee && (
                        <div className="space-y-2">
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                <User size={12} /> Người thực hiện
                            </label>
                            <select
                                value={formData.assignedToMemberId}
                                onChange={(e) => setFormData({...formData, assignedToMemberId: e.target.value})}
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white outline-none focus:ring-2 focus:ring-blue-500/50 appearance-none cursor-pointer"
                            >
                                <option value="" className="bg-slate-900">— Chưa gán —</option>
                                {fetching ? (
                                    <option disabled className="bg-slate-900">Đang tải...</option>
                                ) : (
                                    members.map((m: any) => (
                                        <option key={m.id} value={m.id} className="bg-slate-900">
                                            {m.studentUsername || `Member #${m.id}`}
                                            {m.memberRole === 'TEAM_LEADER' ? ' (Leader)' : ''}
                                        </option>
                                    ))
                                )}
                            </select>
                        </div>
                    )}

                    {/* Info code */}
                    <div className="flex items-center gap-3 text-[11px] text-slate-500 font-mono">
                        <Tag size={12} />
                        {issue.issueCode ? `Jira: ${issue.issueCode}` : `#${issue.issueId}`}
                    </div>

                    {/* Submit button */}
                    <button
                        disabled={loading}
                        type="submit"
                        className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800/50 disabled:cursor-not-allowed text-white font-black py-3.5 rounded-xl transition-all flex items-center justify-center gap-2 uppercase tracking-wider text-sm"
                    >
                        <Save size={18} />
                        {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
                    </button>
                </form>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}
