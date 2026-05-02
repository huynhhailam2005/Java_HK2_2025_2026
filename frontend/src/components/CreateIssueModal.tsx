import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckSquare } from 'lucide-react';
import { issueApi } from '../services/issueApi';
import { groupApi } from '../services/groupApi';
import { showLiquidToast } from '../utils/toast';

interface CreateIssueModalProps {
    isOpen: boolean;
    onClose: () => void;
    groupId?: number;
    onSuccess?: () => void;
    initialParentId?: number | null;
    initialIssueType?: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
}

interface GroupMember {
    id: number;
    username: string;
    studentCode?: string;
}

interface Issue {
    issueId: number;
    title: string;
    issueCode?: string;
    type: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
}

const ISSUE_TYPES = ['EPIC', 'TASK', 'STORY', 'BUG', 'SUB_TASK'] as const;

const CreateIssueModal = ({ isOpen, onClose, groupId, onSuccess, initialParentId, initialIssueType }: CreateIssueModalProps) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        issueType: 'TASK' as typeof ISSUE_TYPES[number],
        deadline: '',
        parentId: '',
        assignedToMemberId: ''
    });

    const [members, setMembers] = useState<GroupMember[]>([]);
    const [issues, setIssues] = useState<Issue[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setFormData(prev => ({
                ...prev,
                issueType: initialIssueType || 'TASK',
                parentId: initialParentId ? initialParentId.toString() : ''
            }));
        }
    }, [isOpen, initialParentId, initialIssueType]);

    useEffect(() => {
        if (isOpen && groupId) {
            Promise.all([
                groupApi.getById(groupId),
                issueApi.getIssuesByGroup(groupId)
            ])
                .then(([groupRes, issuesRes]) => {
                    const groupData = groupRes.data.data as any;
                    const transformedMembers = (groupData?.members || []).map((member: any) => ({
                        id: member.groupMemberId || member.id, // Dùng groupMemberId để gán Issue
                        username: member.username,
                        studentCode: member.studentCode
                    }));
                    setMembers(transformedMembers);
                    const issuesData = issuesRes.data.data as any;
                    setIssues(Array.isArray(issuesData) ? issuesData : []);
                })
                .catch(err => console.error("Lỗi lấy dữ liệu:", err));
        }
    }, [isOpen, groupId]);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!groupId) {
            showLiquidToast('Lỗi: Không xác định được Nhóm!', 'error');
            return;
        }

        if (!formData.title.trim()) {
            showLiquidToast('Vui lòng nhập tiêu đề', 'error');
            return;
        }

        // Validate logic phân cấp
        if (formData.issueType === 'EPIC' && formData.parentId) {
            showLiquidToast('Epic không thể có issue cha', 'error');
            return;
        }

        if (formData.issueType === 'SUB_TASK' && !formData.parentId) {
            showLiquidToast('Sub-task bắt buộc phải chọn issue cha', 'error');
            return;
        }

        setLoading(true);
        try {
            // Convert deadline from "2026-05-10" to "2026-05-10T00:00:00" for LocalDateTime
            const deadline = formData.deadline ? `${formData.deadline}T00:00:00` : null;

            const request = {
                groupId,
                title: formData.title.trim(),
                description: formData.description.trim(),
                issueType: formData.issueType,
                deadline: deadline,
                parentId: formData.parentId ? parseInt(formData.parentId) : null,
                assignedToMemberId: formData.assignedToMemberId ? parseInt(formData.assignedToMemberId) : null,
            };

            const response = await issueApi.createIssue(request);

            if (response.data.success) {
                const createData = response.data.data as any;
                const issueId = createData?.issueId;

                // 🔥 Auto push lên Jira sau khi tạo local thành công
                if (issueId) {
                    try {
                        const pushRes = await issueApi.pushToJira(issueId);
                        if (pushRes.data.success) {
                            const pushData = pushRes.data.data as any;
                            const jiraCode = pushData?.issueCode || 'OK';
                            showLiquidToast(`Tạo Issue thành công! Jira: ${jiraCode}`, 'success');
                        } else {
                            showLiquidToast('Tạo local OK, nhưng push lên Jira thất bại!', 'error');
                        }
                    } catch (pushErr: any) {
                        const msg = pushErr?.response?.data?.message || 'Lỗi push Jira';
                        showLiquidToast(`Issue đã tạo local, nhưng push Jira thất bại: ${msg}`, 'error');
                    }
                } else {
                    showLiquidToast('Tạo Issue thành công!', 'success');
                }

                // Reset form
                setFormData({ title: '', description: '', issueType: 'TASK', deadline: '', parentId: '', assignedToMemberId: '' });
                if (onSuccess) onSuccess();
                onClose();
            }
        } catch (error: any) {
            showLiquidToast(error.response?.data?.message || 'Lỗi khi tạo Issue!', 'error');
        } finally {
            setLoading(false);
        }
    };

    const isTypeDisabled = (type: typeof ISSUE_TYPES[number]) => {
        if (!initialParentId) {
            if (type === 'SUB_TASK') return true;
            return false;
        }

        if (type === 'EPIC') return true;

        if (initialIssueType === 'SUB_TASK' && type !== 'SUB_TASK') return true;

        return false;
    };

    const selectedParent = formData.parentId ?
        issues.find(i => i.issueId === parseInt(formData.parentId)) : null;

    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 z-100 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
                <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="bg-[#0f172a] border border-white/10 rounded-3xl w-full max-w-2xl overflow-hidden shadow-2xl max-h-[90vh] overflow-y-auto">
                    <div className="sticky top-0 p-6 border-b border-white/10 flex justify-between items-center bg-white/5 backdrop-blur z-20">
                        <h2 className="text-xl font-black text-white flex items-center gap-2">
                            <CheckSquare className="w-5 h-5 text-blue-400" />
                            TẠO {formData.issueType} {formData.parentId ? 'CON' : 'MỚI'}
                        </h2>
                        <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors"><X /></button>
                    </div>

                    <form onSubmit={handleSubmit} className="p-6 space-y-5">
                        {/* Parent Info - Hiển thị nếu đang tạo con */}
                        {selectedParent && (
                            <div className="bg-blue-500/10 border border-blue-500/20 p-4 rounded-2xl">
                                <p className="text-[10px] font-black text-blue-400 uppercase tracking-widest mb-1">Issue Cha</p>
                                <p className="text-white font-bold text-sm">{selectedParent.issueCode}: {selectedParent.title}</p>
                            </div>
                        )}

                        <div>
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase tracking-widest block mb-2">Tiêu đề <span className="text-red-500">*</span></label>
                            <input
                                required
                                type="text"
                                value={formData.title}
                                onChange={(e) => setFormData({...formData, title: e.target.value})}
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500 placeholder-slate-600"
                                placeholder="Nhập tiêu đề issue..."
                            />
                        </div>

                        <div>
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase tracking-widest block mb-2">Loại Issue <span className="text-red-500">*</span></label>
                            <div className="grid grid-cols-5 gap-2">
                                {ISSUE_TYPES.map((type) => (
                                    <button
                                        key={type}
                                        type="button"
                                        disabled={isTypeDisabled(type)}
                                        onClick={() => setFormData({
                                            ...formData,
                                            issueType: type,
                                            // Xoá parent nếu chuyển sang Epic (Epic không có cha)
                                            parentId: type === 'EPIC' ? '' : formData.parentId
                                        })}
                                        className={`px-3 py-2 rounded-lg text-[10px] font-black uppercase transition-all border ${
                                            formData.issueType === type
                                                ? 'bg-blue-600 text-white border-blue-500'
                                                : 'bg-white/5 text-slate-500 border-white/10 hover:border-white/20 disabled:opacity-20 disabled:cursor-not-allowed'
                                        }`}
                                    >
                                        {type}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Description */}
                        <div>
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase tracking-widest block mb-2">Mô tả chi tiết</label>
                            <textarea
                                rows={3}
                                value={formData.description}
                                onChange={(e) => setFormData({...formData, description: e.target.value})}
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500 placeholder-slate-600"
                                placeholder="Mô tả nội dung công việc..."
                            />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase tracking-widest block mb-2">Deadline</label>
                                <input
                                    type="date"
                                    value={formData.deadline}
                                    onChange={(e) => setFormData({...formData, deadline: e.target.value})}
                                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500"
                                />
                            </div>
                            <div>
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase tracking-widest block mb-2">Người thực hiện</label>
                                <select
                                    value={formData.assignedToMemberId}
                                    onChange={(e) => setFormData({...formData, assignedToMemberId: e.target.value})}
                                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500"
                                >
                                    <option value="" className="bg-[#0f172a]">Chưa gán</option>
                                    {members.map(m => (
                                        <option key={m.id} value={m.id} className="bg-[#0f172a]">{m.username}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="flex gap-3 pt-4 border-t border-white/10">
                            <button type="button" onClick={onClose} className="flex-1 px-4 py-4 bg-white/5 hover:bg-white/10 text-slate-400 rounded-2xl font-black uppercase text-[10px] tracking-[0.2em] transition-all">Hủy</button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 px-4 py-4 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white rounded-2xl font-black uppercase text-[10px] tracking-[0.2em] transition-all shadow-lg shadow-blue-600/20"
                            >
                                {loading ? 'Đang xử lý...' : 'Xác nhận tạo'}
                            </button>
                        </div>
                    </form>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
};

export default CreateIssueModal;