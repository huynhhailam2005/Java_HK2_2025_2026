import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Users, ChevronRight, LayoutGrid, Plus, X, Save, AlignLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { showLiquidToast } from '../utils/toast';

interface GroupItem {
    id: number;
    code: string;
    name: string;
    role: 'LEADER' | 'MEMBER' | 'LECTURER';
    lecturerName?: string;
    memberCount: number;
}

const GroupList = () => {
    const [groups, setGroups] = useState<GroupItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    // Da them truong description theo dung yeu cau
    const [newGroup, setNewGroup] = useState({ code: '', name: '', description: '' });
    const navigate = useNavigate();

    const userDataStr = localStorage.getItem('user');
    const currentUser = userDataStr ? JSON.parse(userDataStr) : { username: '', role: 'STUDENT' };
    const userRole = currentUser.role;

    useEffect(() => {
        setLoading(true);
        setTimeout(() => {
            if (userRole === 'LECTURER') {
                setGroups([
                    { id: 1, code: 'GR-01', name: 'Hệ thống Quản lý KTX', role: 'LECTURER', memberCount: 4 },
                    { id: 2, code: 'GR-02', name: 'Website Bán Hàng Điện Tử', role: 'LECTURER', memberCount: 4 },
                ]);
            } else {
                // Mock cho Student
                setGroups([
                    { id: 1, code: 'GR-01', name: 'Hệ thống Quản lý KTX', role: 'LEADER', lecturerName: 'gv_nguyenvana', memberCount: 4 },
                    { id: 3, code: 'GR-03', name: 'App Quản Lý Thư Viện', role: 'MEMBER', lecturerName: 'gv_tranthib', memberCount: 4 },
                ]);
            }
            setLoading(false);
        }, 500);
    }, [userRole]);

    const handleCreateGroup = () => {
        if (!newGroup.code || !newGroup.name) return showLiquidToast('Vui lòng nhập đủ thông tin mã và tên nhóm', 'error');
        const created: GroupItem = {
            id: Date.now(),
            code: newGroup.code,
            name: newGroup.name,
            role: 'LECTURER',
            memberCount: 0 // Nhom moi tao chua co sinh vien
        };
        setGroups([created, ...groups]);
        setIsCreateModalOpen(false);
        setNewGroup({ code: '', name: '', description: '' });
        showLiquidToast('Đã tạo nhóm thành công. Hãy vào nhóm để thêm sinh viên!', 'success');
    };

    return (
        <div className="space-y-8 max-w-6xl mx-auto w-full">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                        <LayoutGrid className="text-blue-400" />
                        {userRole === 'LECTURER' ? 'Quản Lý Nhóm Đồ Án' : 'Dự Án Của Tôi'}
                    </h1>
                </div>
                {userRole === 'LECTURER' && (
                    <button
                        onClick={() => setIsCreateModalOpen(true)}
                        className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-3 rounded-2xl font-bold flex items-center gap-2 transition-all shadow-lg shadow-blue-500/20"
                    >
                        <Plus size={20} /> Tạo Nhóm Mới
                    </button>
                )}
            </div>

            {loading ? (
                <div className="flex justify-center py-20 text-blue-400"><span className="animate-spin text-4xl">⟳</span></div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {groups.map((group, index) => (
                        <motion.div
                            key={group.id}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: index * 0.1 }}
                            onClick={() => navigate(`/groups/${group.id}`)}
                            className="group relative bg-white/5 hover:bg-white/10 border border-white/10 hover:border-blue-500/50 rounded-3xl p-6 cursor-pointer transition-all duration-300"
                        >
                            <div className="relative z-10 flex justify-between items-start mb-4">
                                <div className="w-12 h-12 rounded-xl bg-linear-to-br from-blue-600 to-cyan-500 flex items-center justify-center font-bold text-xl text-white">
                                    {group.name.charAt(0).toUpperCase()}
                                </div>
                                <span className={`text-[10px] px-3 py-1.5 rounded-lg font-black uppercase tracking-wider
                                    ${group.role === 'LEADER' ? 'bg-amber-500/20 text-amber-400' :
                                    group.role === 'LECTURER' ? 'bg-purple-500/20 text-purple-400' : 'bg-slate-700 text-slate-300'}`}
                                >
                                    {group.role}
                                </span>
                            </div>
                            <div className="relative z-10">
                                <p className="text-sm font-mono text-blue-400 mb-1">{group.code}</p>
                                <h3 className="text-xl font-bold text-white mb-4 line-clamp-2">{group.name}</h3>
                                <div className="pt-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-400">
                                    <span className="flex items-center gap-1.5"><Users size={14}/> {group.memberCount} SV</span>
                                    <ChevronRight size={16} className="group-hover:text-white transition-colors" />
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </div>
            )}

            <AnimatePresence>
                {isCreateModalOpen && (
                    <>
                        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setIsCreateModalOpen(false)} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100" />
                        <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-[#0f172a] border border-white/10 rounded-3xl p-8 z-101 shadow-2xl">
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-xl font-bold text-white uppercase">Thiết lập nhóm mới</h2>
                                <button onClick={() => setIsCreateModalOpen(false)} className="text-slate-400 hover:text-white"><X size={20}/></button>
                            </div>
                            <div className="space-y-5">
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-400 uppercase tracking-widest">Mã Nhóm</label>
                                    <input type="text" value={newGroup.code} onChange={e => setNewGroup({...newGroup, code: e.target.value})} placeholder="VD: GR-04" className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500" />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-400 uppercase tracking-widest">Tên Nhóm / Đề Tài</label>
                                    <input type="text" value={newGroup.name} onChange={e => setNewGroup({...newGroup, name: e.target.value})} placeholder="VD: Hệ Thống AI Nhận Diện..." className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500" />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2"><AlignLeft size={14}/> Mô tả Đề tài</label>
                                    <textarea value={newGroup.description} onChange={e => setNewGroup({...newGroup, description: e.target.value})} placeholder="Nhập mô tả chi tiết yêu cầu đề tài..." rows={3} className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500 resize-none" />
                                </div>
                                <button onClick={handleCreateGroup} className="w-full bg-blue-600 hover:bg-blue-500 text-white py-4 rounded-2xl font-bold transition-all flex justify-center items-center gap-2 mt-4">
                                    <Save size={20} /> XÁC NHẬN TẠO
                                </button>
                            </div>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>
        </div>
    );
};

export default GroupList;