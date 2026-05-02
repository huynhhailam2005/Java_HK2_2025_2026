import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Users, FolderKanban, Edit2, Trash2,
    Settings, UserCheck, Plus
} from 'lucide-react';
import { showLiquidToast } from '../utils/toast';
import { adminApi } from '../services/adminApi';
import GroupConfigModal from '../components/GroupConfigModal';
import UserEditModal from '../components/UserEditModal';
import GroupLecturerModal from '../components/GroupLecturerModal';
import CreateGroupModal from '../components/CreateGroupModal';
import ConfirmDeleteModal from '../components/ConfirmDeleteModal';

interface AdminGroupItem {
    id: number;
    code: string;
    name: string;
    lecturer: string;
}

interface AdminUserItem {
    id: number;
    username: string;
    email: string;
    role: 'ADMIN' | 'LECTURER' | 'STUDENT';
}

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState<'users' | 'groups'>('users');
    const [loading, setLoading] = useState(true);
    const [groups, setGroups] = useState<AdminGroupItem[]>([]);
    const [usersList, setUsersList] = useState<AdminUserItem[]>([]);
    const [isUserEditOpen, setIsUserEditOpen] = useState(false);
    const [editingUserId, setEditingUserId] = useState<number>(0);
    const [editingUserRole, setEditingUserRole] = useState<'STUDENT' | 'LECTURER'>('STUDENT');
    const [isConfigOpen, setIsConfigOpen] = useState(false);
    const [configGroupId, setConfigGroupId] = useState<number>(0);
    const [configGroupName, setConfigGroupName] = useState<string>('');
    const [isLecModalOpen, setIsLecModalOpen] = useState(false);
    const [selectedGroup, setSelectedGroup] = useState<any>(null);
    const [isCreateGroupOpen, setIsCreateGroupOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [deletingGroup, setDeletingGroup] = useState<any>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        setLoading(true);
        try {
            const [userRes, groupRes] = await Promise.all([
                adminApi.getUsers(),
                adminApi.getGroups()
            ]);

            if (userRes.data.success) {
                setUsersList(userRes.data.data.map((u: any) => ({
                    id: u.id, username: u.username, email: u.email, role: u.role
                })));
            }
            if (groupRes.data.success) {
                setGroups(groupRes.data.data.map((g: any) => ({
                    id: g.id,
                    code: g.groupId || 'N/A',
                    name: g.groupName || 'Chưa đặt tên',
                    lecturer: g.lecturerUsername || 'Chưa phân công'
                })));
            }
        } catch (err) {
            showLiquidToast('Lỗi kết nối tới Server!', 'error');
        } finally {
            setLoading(false);
        }
    };

    const confirmDeleteGroup = async () => {
        if (!deletingGroup) return;
        setIsDeleting(true);
        try {
            const res = await adminApi.deleteGroup(deletingGroup.id);
            if (res.data.success) {
                showLiquidToast(`Đã xóa sạch dữ liệu nhóm ${deletingGroup.name}`, 'success');
                loadDashboardData();
                setIsDeleteModalOpen(false);
            }
        } catch (err) {
            showLiquidToast('Lỗi khi xóa nhóm!', 'error');
        } finally {
            setIsDeleting(false);
            setDeletingGroup(null);
        }
    };

    return (
        <div className="w-full space-y-5 animate-in fade-in duration-500">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Stats Card: Users */}
                <div className="bg-[#1a1f2e] border border-white/5 p-5 rounded-[1.5rem] relative overflow-hidden group shadow-lg">
                    <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:scale-110 transition-transform"><Users size={70}/></div>
                    <div className="relative z-10 flex items-center gap-4">
                    <div className="w-11 h-11 bg-blue-600/20 rounded-xl flex items-center justify-center text-blue-400">
                        <Users size={22}/>
                    </div>
                    <div className="flex-1">
                        <h3 className="text-white text-sm font-medium">Người dùng</h3>
                        <div className="flex items-baseline gap-2">
                            <span className="text-3xl font-black text-white tabular-nums">{usersList.length}</span>
                        </div>
                    </div>
                    </div>
                </div>

                {/* Stats Card: Groups */}
                <div className="bg-[#1a1f2e] border border-white/5 p-5 rounded-[1.5rem] relative overflow-hidden group shadow-lg">
                    <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:scale-110 transition-transform"><FolderKanban size={70}/></div>
                    <div className="relative z-10 flex items-center gap-4">
                    <div className="w-11 h-11 bg-emerald-600/20 rounded-xl flex items-center justify-center text-emerald-400">
                        <FolderKanban size={22}/>
                    </div>
                    <div className="flex-1">
                        <h3 className="text-white text-sm font-medium">Nhóm đồ án</h3>
                        <div className="flex items-baseline gap-2">
                            <span className="text-3xl font-black text-white tabular-nums">{groups.length}</span>
                        </div>
                    </div>
                    </div>
                </div>
            </div>

            <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                <div className="flex bg-white/5 p-1 rounded-xl border border-white/10 shadow-inner">
                    <button onClick={() => setActiveTab('users')} className={`px-6 py-2 rounded-lg text-xs font-bold flex items-center gap-2 transition-all ${activeTab === 'users' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}>
                        <Users size={14}/> Người dùng
                    </button>
                    <button onClick={() => setActiveTab('groups')} className={`px-6 py-2 rounded-lg text-xs font-bold flex items-center gap-2 transition-all ${activeTab === 'groups' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'}`}>
                        <FolderKanban size={14}/> Nhóm đồ án
                    </button>
                </div>

                {activeTab === 'groups' && (
                    <button
                        onClick={() => setIsCreateGroupOpen(true)}
                        className="bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl text-xs font-black flex items-center gap-2 transition-all shadow-lg shadow-emerald-500/20 active:scale-95"
                    >
                        <Plus size={16}/> TẠO NHÓM MỚI
                    </button>
                )}
            </div>

            <div className="bg-white/[0.02] border border-white/10 rounded-[1.5rem] overflow-hidden shadow-2xl backdrop-blur-md relative">
                <div className="max-h-[480px] overflow-y-auto custom-scrollbar">
                    <AnimatePresence mode="wait">
                        <motion.div
                            key={activeTab}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            exit={{ opacity: 0 }}
                            transition={{ duration: 0.2 }}
                        >
                            <table className="w-full text-left border-collapse">
                                <thead className="sticky top-0 bg-[#1a2333] z-10 shadow-sm">
                                <tr className="text-slate-500 text-[10px] uppercase font-black tracking-widest border-b border-white/5">
                                    <th className="p-4">{activeTab === 'users' ? 'ID' : 'Mã Nhóm'}</th>
                                    <th className="p-4">{activeTab === 'users' ? 'Tài khoản' : 'Tên đề tài'}</th>
                                    <th className="p-4">{activeTab === 'users' ? 'Email' : 'Giảng viên'}</th>
                                    <th className="p-4 text-right">Thao tác</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-white/5">
                                {activeTab === 'users' ? (
                                    usersList.map(user => (
                                        <tr key={user.id} className="hover:bg-white/[0.03] transition-colors group">
                                            <td className="p-4 text-slate-500 font-mono text-[11px]">#{user.id}</td>
                                            <td className="p-4 font-bold text-sm text-white">{user.username}</td>
                                            <td className="p-4 text-slate-400 text-xs">{user.email}</td>
                                            <td className="p-4 text-right flex justify-end gap-2">
                                                <button onClick={() => { setEditingUserId(user.id); setEditingUserRole(user.role === 'LECTURER' ? 'LECTURER' : 'STUDENT'); setIsUserEditOpen(true); }} className="p-2 text-blue-400 hover:bg-blue-500/20 rounded-lg transition-all"><Edit2 size={16}/></button>
                                                <button onClick={() => { if(window.confirm(`Xóa user ${user.username}?`)) adminApi.deleteUser(user.id).then(loadDashboardData) }} className="p-2 text-red-400 hover:bg-red-500/20 rounded-lg transition-all"><Trash2 size={16}/></button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    groups.map(group => (
                                        <tr key={group.id} className="hover:bg-white/[0.03] transition-colors group">
                                            <td className="p-4 font-mono font-black text-blue-400 text-xs">{group.code}</td>
                                            <td className="p-4 font-bold text-sm text-white leading-tight">{group.name}</td>
                                            <td className="p-4">
                                                    <span className={`px-3 py-1 rounded-lg text-[9px] font-black uppercase tracking-wider ${group.lecturer === 'Chưa phân công' ? 'bg-red-500/10 text-red-400' : 'bg-blue-500/10 text-blue-400'}`}>
                                                        {group.lecturer}
                                                    </span>
                                            </td>
                                            <td className="p-4 text-right flex justify-end gap-2">
                                                <button onClick={() => { setSelectedGroup(group); setIsLecModalOpen(true); }} className="p-2 text-emerald-400 hover:bg-emerald-500/20 rounded-lg transition-all" title="Giảng viên"><UserCheck size={18}/></button>
                                                <button onClick={() => { setConfigGroupId(group.id); setConfigGroupName(group.name); setIsConfigOpen(true); }} className="p-2 text-amber-400 hover:bg-amber-500/20 rounded-lg transition-all" title="Config"><Settings size={18}/></button>
                                                <button onClick={() => { setDeletingGroup(group); setIsDeleteModalOpen(true); }} className="p-2 text-red-500 hover:bg-red-500/20 rounded-lg transition-all" title="Xóa"><Trash2 size={18}/></button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </motion.div>
                    </AnimatePresence>
                </div>
                {loading && <div className="text-center py-10 text-slate-500 animate-pulse font-black text-[10px] uppercase tracking-widest">Đang tải...</div>}
                {!loading && (activeTab === 'users' ? usersList : groups).length === 0 && <div className="text-center py-10 text-slate-600 italic text-sm">Dữ liệu hiện đang trống.</div>}
            </div>

            {/* --- Modals Integration --- */}
            <UserEditModal userId={editingUserId} userRole={editingUserRole} isOpen={isUserEditOpen} onClose={() => setIsUserEditOpen(false)} onSave={loadDashboardData} />
            <GroupConfigModal groupId={configGroupId} groupName={configGroupName} isOpen={isConfigOpen} onClose={() => setIsConfigOpen(false)} onSave={loadDashboardData} />
            <GroupLecturerModal isOpen={isLecModalOpen} onClose={() => setIsLecModalOpen(false)} group={selectedGroup} onSuccess={loadDashboardData} />
            <CreateGroupModal isOpen={isCreateGroupOpen} onClose={() => setIsCreateGroupOpen(false)} onSuccess={loadDashboardData} />
            <ConfirmDeleteModal isOpen={isDeleteModalOpen} onClose={() => setIsDeleteModalOpen(false)} onConfirm={confirmDeleteGroup} title={deletingGroup?.name || ''} loading={isDeleting} />
        </div>
    );
};

export default AdminDashboard;