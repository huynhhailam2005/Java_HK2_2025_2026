import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckSquare } from 'lucide-react';
import { issueApi } from '../../services/issueApi';
import { groupApi } from '../../services/groupApi';
import { showLiquidToast } from '../../utils/toast';

interface CreateIssueModalProps {
    isOpen: boolean;
    onClose: () => void;
    groupId?: number;
    onSuccess?: () => void;
}

// Khai báo Interface rõ ràng để xoá sạch any
interface GroupMember {
    student: {
        id: number;
        username: string;
    }
}

const CreateIssueModal = ({ isOpen, onClose, groupId, onSuccess }: CreateIssueModalProps) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        issueType: 'TASK',
        assigneeId: ''
    });

    // Gắn type chuẩn vào State
    const [members, setMembers] = useState<GroupMember[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen && groupId) {
            groupApi.getMembers(groupId)
                .then(res => {
                    const data = res.data.data;
                    // Fix lỗi gán object vào state [] bằng cách check isArray
                    setMembers(Array.isArray(data) ? data : []);
                })
                .catch(err => console.error("Lỗi lấy thành viên:", err));
        }
    }, [isOpen, groupId]);

    // Fix lỗi 'FormEvent' is deprecated bằng cách truyền HTMLFormElement
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!groupId) {
            showLiquidToast('Lỗi: Không xác định được Nhóm!', 'error');
            return;
        }

        setLoading(true);
        try {
            await issueApi.createIssue({
                ...formData,
                groupId: groupId,
                assigneeId: formData.assigneeId ? parseInt(formData.assigneeId) : null
            });
            showLiquidToast('Đã tạo công việc thành công!', 'success');

            setFormData({ title: '', description: '', issueType: 'TASK', assigneeId: '' });
            if (onSuccess) onSuccess();
            onClose();
        } catch (error: unknown) { // Xoá error: any
            const err = error as { response?: { data?: { message?: string } } };
            showLiquidToast(err.response?.data?.message || 'Lỗi khi tạo công việc!', 'error');
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
                            <CheckSquare className="w-5 h-5 text-blue-400" />
                            TẠO CÔNG VIỆC MỚI
                        </h2>
                        <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors"><X /></button>
                    </div>

                    <form onSubmit={handleSubmit} className="p-6 space-y-4">
                        <div className="space-y-1">
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Tiêu đề (Tóm tắt)</label>
                            <input required type="text" value={formData.title} onChange={(e) => setFormData({...formData, title: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" placeholder="VD: Thiết kế Database..." />
                        </div>

                        <div className="space-y-1">
                            <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Mô tả chi tiết</label>
                            <textarea rows={3} value={formData.description} onChange={(e) => setFormData({...formData, description: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white focus:ring-2 focus:ring-blue-500" placeholder="Mô tả công việc cần làm..." />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1">
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Loại công việc</label>
                                <select value={formData.issueType} onChange={(e) => setFormData({...formData, issueType: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500">
                                    <option value="EPIC" className="text-black">Epic (Tính năng lớn)</option>
                                    <option value="TASK" className="text-black">Task (Nhiệm vụ)</option>
                                    <option value="STORY" className="text-black">Story</option>
                                    <option value="BUG" className="text-black">Bug (Lỗi)</option>
                                </select>
                            </div>
                            <div className="space-y-1">
                                <label className="text-xs font-bold text-slate-400 ml-1 uppercase">Phân công cho</label>
                                <select value={formData.assigneeId} onChange={(e) => setFormData({...formData, assigneeId: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:ring-2 focus:ring-blue-500">
                                    <option value="" className="text-black">-- Chưa phân công --</option>
                                    {members.map(m => (
                                        <option key={m.student.id} value={m.student.id} className="text-black">{m.student.username}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <button disabled={loading} type="submit" className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-xl transition-colors mt-4">
                            {loading ? 'Đang tạo...' : 'Tạo Công Việc'}
                        </button>
                    </form>
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
};

export default CreateIssueModal;