import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Layers, Layout, CheckCircle, Bug, FilePlus, Save } from 'lucide-react';
import { showLiquidToast } from '../../utils/toast';

interface CreateIssueModalProps {
    isOpen: boolean;
    onClose: () => void;
}

// Giả lập dữ liệu các Công việc hiện có của Nhóm để làm danh sách chọn Cha
const MOCK_EXISTING_ISSUES = [
    { id: 1, type: 'EPIC', title: 'Xây dựng Backend Spring Boot' },
    { id: 2, type: 'EPIC', title: 'Hoàn thiện Frontend UI/UX' },
    { id: 3, type: 'TASK', title: 'Viết API Đăng nhập' },
    { id: 4, type: 'STORY', title: 'Chức năng Quản lý User cho Admin' },
    { id: 5, type: 'BUG', title: 'Lỗi trắng màn hình khi API tạch' },
];

const CreateIssueModal: React.FC<CreateIssueModalProps> = ({ isOpen, onClose }) => {
    const [issueType, setIssueType] = useState('TASK');
    const [title, setTitle] = useState('');
    const [parentId, setParentId] = useState('');

    // Logic lọc danh sách thẻ Cha dựa trên Loại thẻ đang chọn
    const getAvailableParents = () => {
        if (issueType === 'TASK' || issueType === 'STORY' || issueType === 'BUG') {
            return MOCK_EXISTING_ISSUES.filter(issue => issue.type === 'EPIC');
        }
        if (issueType === 'SUB_TASK') {
            return MOCK_EXISTING_ISSUES.filter(issue => ['TASK', 'STORY', 'BUG'].includes(issue.type));
        }
        return [];
    };

    const availableParents = getAvailableParents();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) {
            showLiquidToast('Vui lòng nhập tên công việc!', 'error');
            return;
        }
        if (issueType !== 'EPIC' && !parentId) {
            showLiquidToast('Vui lòng chọn công việc cha!', 'error');
            return;
        }

        console.log({ issueType, title, parentId });
        showLiquidToast('Tạo công việc thành công!', 'success');
        onClose();
        // Reset form
        setTitle('');
        setIssueType('TASK');
        setParentId('');
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    {/* Lớp kính mờ nền */}
                    <motion.div
                        initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                        onClick={onClose}
                        className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100 flex items-center justify-center p-4"
                    >
                        {/* Nội dung Modal */}
                        <motion.div
                            initial={{ opacity: 0, scale: 0.95, y: 20 }}
                            animate={{ opacity: 1, scale: 1, y: 0 }}
                            exit={{ opacity: 0, scale: 0.95, y: 20 }}
                            onClick={(e) => e.stopPropagation()}
                            className="w-full max-w-2xl bg-[#0f172a]/90 backdrop-blur-3xl border border-white/10 rounded-[40px] shadow-2xl overflow-hidden"
                        >
                            {/* Header */}
                            <div className="bg-white/5 border-b border-white/10 p-6 flex justify-between items-center">
                                <div className="flex items-center gap-3">
                                    <div className="p-3 bg-blue-600/20 text-blue-400 rounded-2xl">
                                        <FilePlus className="w-6 h-6" />
                                    </div>
                                    <h2 className="text-2xl font-black text-white uppercase tracking-tight">Tạo Công Việc Mới</h2>
                                </div>
                                <button onClick={onClose} className="p-2 hover:bg-white/10 rounded-full text-slate-400 transition-colors">
                                    <X className="w-6 h-6" />
                                </button>
                            </div>

                            {/* Form Body */}
                            <form onSubmit={handleSubmit} className="p-8 space-y-6">
                                {/* Dòng 1: Chọn loại Issue */}
                                <div className="space-y-3">
                                    <label className="text-sm font-bold text-slate-400 uppercase tracking-widest ml-2">Loại công việc</label>
                                    <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
                                        {[
                                            { type: 'EPIC', icon: <Layers size={18} />, color: 'text-purple-400', bg: 'bg-purple-400/20', border: 'border-purple-500/50' },
                                            { type: 'STORY', icon: <Layout size={18} />, color: 'text-green-400', bg: 'bg-green-400/20', border: 'border-green-500/50' },
                                            { type: 'TASK', icon: <CheckCircle size={18} />, color: 'text-blue-400', bg: 'bg-blue-400/20', border: 'border-blue-500/50' },
                                            { type: 'BUG', icon: <Bug size={18} />, color: 'text-red-400', bg: 'bg-red-400/20', border: 'border-red-500/50' },
                                            { type: 'SUB_TASK', icon: <FilePlus size={18} />, color: 'text-slate-400', bg: 'bg-slate-400/20', border: 'border-slate-500/50', label: 'SUB-TASK' },
                                        ].map((item) => (
                                            <button
                                                key={item.type}
                                                type="button"
                                                onClick={() => {
                                                    setIssueType(item.type);
                                                    setParentId(''); // Reset parent khi đổi type
                                                }}
                                                className={`flex flex-col items-center justify-center p-3 rounded-2xl border transition-all ${
                                                    issueType === item.type ? `${item.bg} ${item.border} shadow-inner` : 'bg-white/5 border-white/5 hover:bg-white/10 hover:border-white/20'
                                                }`}
                                            >
                                                <div className={issueType === item.type ? item.color : 'text-slate-500'}>
                                                    {item.icon}
                                                </div>
                                                <span className={`text-xs font-bold mt-2 ${issueType === item.type ? 'text-white' : 'text-slate-500'}`}>
                                                    {item.label || item.type}
                                                </span>
                                            </button>
                                        ))}
                                    </div>
                                </div>

                                {/* Dòng 2: Logic Ràng buộc Cha - Con */}
                                <AnimatePresence mode="popLayout">
                                    {issueType !== 'EPIC' && (
                                        <motion.div
                                            initial={{ opacity: 0, height: 0, marginTop: 0 }}
                                            animate={{ opacity: 1, height: 'auto', marginTop: 24 }}
                                            exit={{ opacity: 0, height: 0, marginTop: 0 }}
                                            className="space-y-3 overflow-hidden"
                                        >
                                            <label className="text-sm font-bold text-slate-400 uppercase tracking-widest ml-2 flex items-center gap-2">
                                                Thuộc về
                                                <span className="text-red-500">*</span>
                                            </label>
                                            <select
                                                value={parentId}
                                                onChange={(e) => setParentId(e.target.value)}
                                                className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            >
                                                <option value="" className="text-black">
                                                    {issueType === 'SUB_TASK' ? '-- Chọn công việc cha --' : '-- Chọn Epic chứa công việc này --'}
                                                </option>
                                                {availableParents.map(parent => (
                                                    <option key={parent.id} value={parent.id} className="text-black">
                                                        [{parent.type}] {parent.title}
                                                    </option>
                                                ))}
                                            </select>
                                        </motion.div>
                                    )}
                                </AnimatePresence>

                                {/* Dòng 3: Tên công việc */}
                                <div className="space-y-3">
                                    <label className="text-sm font-bold text-slate-400 uppercase tracking-widest ml-2 flex items-center gap-2">
                                        Tên công việc <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                        placeholder="Nhập tóm tắt công việc (VD: Thiết kế màn hình Login)"
                                        className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder:text-slate-600"
                                    />
                                </div>

                                {/* Submit Button */}
                                <div className="pt-4">
                                    <button
                                        type="submit"
                                        className="w-full bg-linear-to-r from-blue-600 to-cyan-500 text-white p-5 rounded-3xl font-black shadow-xl hover:-translate-y-1 active:translate-y-0.5 transition-all flex items-center justify-center gap-3"
                                    >
                                        <Save className="w-5 h-5" /> TẠO CÔNG VIỆC
                                    </button>
                                </div>
                            </form>
                        </motion.div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
};

export default CreateIssueModal;