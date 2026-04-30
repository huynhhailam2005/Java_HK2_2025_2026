import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Users, FolderKanban, Edit2, CheckCircle2, X, Trash2, Plus } from 'lucide-react';
import { showLiquidToast } from '../utils/toast';

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
    lecturerCode?: string;
    studentCode?: string;
}

// Khop voi AdminResponse.java cua backend
interface RawAdminResponse {
    id: number;
    username: string;
    email: string;
    role: string;
}

interface RawGroupResponse {
    id?: number;
    groupId?: number;
    code?: string;
    groupCode?: string;
    name?: string;
    groupName?: string;
    lecturerUsername?: string;
    lecturerName?: string;
}

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState<'users' | 'groups'>('users');
    const [loading, setLoading] = useState(true);

    const [groups, setGroups] = useState<AdminGroupItem[]>([]);
    const [usersList, setUsersList] = useState<AdminUserItem[]>([]);
    const [lecturerList, setLecturerList] = useState<AdminUserItem[]>([]);

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [editingGroup, setEditingGroup] = useState<AdminGroupItem>({ id: 0, code: '', name: '', lecturer: '' });

    useEffect(() => {
        let isMounted = true;

        const fetchDashboardData = async () => {
            if (isMounted) setLoading(true);
            try {
                const token = localStorage.getItem('token');
                const headers = {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                };

                // 1. KEO TOAN BO USER TU ADMIN CONTROLLER
                const userRes = await fetch('http://localhost:8080/api/admin/users', { headers });
                if (userRes.ok) {
                    const userData = await userRes.json();
                    if (userData.success && Array.isArray(userData.data) && isMounted) {
                        const formattedUsers: AdminUserItem[] = userData.data.map((u: RawAdminResponse) => ({
                            id: u.id,
                            username: u.username,
                            email: u.email,
                            role: u.role as 'ADMIN' | 'LECTURER' | 'STUDENT',
                        }));
                        setUsersList(formattedUsers);

                        // Loc rieng Giang vien
                        const lecs = formattedUsers.filter(u => u.role === 'LECTURER');
                        setLecturerList(lecs);
                    }
                }

                // 2. KEO TOAN BO GROUP
                const groupRes = await fetch('http://localhost:8080/api/groups', { headers });
                if (groupRes.ok) {
                    const groupData = await groupRes.json();
                    if (groupData.success && Array.isArray(groupData.data) && isMounted) {
                        setGroups(groupData.data.map((g: RawGroupResponse) => ({
                            id: g.groupId || g.id || 0,
                            code: g.groupCode || g.code || '',
                            name: g.groupName || g.name || '',
                            lecturer: g.lecturerUsername || g.lecturerName || 'Chưa phân công'
                        })));
                    }
                }

            } catch (err) {
                console.error("Fetch Error:", err);
                if (isMounted) showLiquidToast('Không thể kết nối tới Server', 'error');
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchDashboardData();
        return () => { isMounted = false; };
    }, []);

    const handleEditGroup = (group: AdminGroupItem) => {
        setEditingGroup(group);
        setIsEditModalOpen(true);
    };

    const handleSaveGroup = async () => {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`http://localhost:8080/api/groups/${editingGroup.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    lecturerUsername: editingGroup.lecturer === 'Chưa phân công' ? null : editingGroup.lecturer
                })
            });

            const result = await response.json();
            if (response.ok && result.success) {
                setGroups(groups.map(g => g.id === editingGroup.id ? editingGroup : g));
                setIsEditModalOpen(false);
                showLiquidToast('Đã cập nhật Giảng viên thành công', 'success');
            } else {
                showLiquidToast(result.message || 'Lỗi khi cập nhật', 'error');
            }
        } catch (err) {
            console.error(err);
            showLiquidToast('Lỗi kết nối tới Server', 'error');
        }
    };

    const handleDeleteUser = async (userId: number) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa user này?")) return;

        try {
            const token = localStorage.getItem('token');
            // SU DUNG API XOA CUA ADMIN CONTROLLER
            const response = await fetch(`http://localhost:8080/api/admin/users/${userId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            const result = await response.json();
            if (response.ok && result.success) {
                setUsersList(usersList.filter(u => u.id !== userId));
                showLiquidToast('Đã xóa người dùng', 'success');
            } else {
                showLiquidToast(result.message || 'Lỗi khi xóa', 'error');
            }
        } catch (err) {
            console.error(err);
            showLiquidToast('Lỗi kết nối tới Server', 'error');
        }
    };

    const renderUsers = () => (
        <div className="bg-white/5 border border-white/10 rounded-3xl p-6">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-6">
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <Users className="text-blue-400"/>
                    Quản lý Toàn bộ User {loading && <span className="animate-spin text-sm">⟳</span>}
                </h2>
                <button className="bg-blue-600 hover:bg-blue-500 text-white px-5 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all text-sm shadow-lg shadow-blue-500/20">
                    <Plus size={16}/> Thêm User
                </button>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="border-b border-white/10 text-slate-400 text-sm">
                        <th className="p-4 font-medium uppercase tracking-wider">ID</th>
                        <th className="p-4 font-medium uppercase tracking-wider">Tên Đăng Nhập</th>
                        <th className="p-4 font-medium uppercase tracking-wider">Email</th>
                        <th className="p-4 font-medium uppercase tracking-wider">Vai Trò</th>
                        <th className="p-4 font-medium uppercase tracking-wider text-right">Thao Tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    {usersList.map(user => (
                        <tr key={user.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                            <td className="p-4 font-mono text-sm text-slate-400">#{user.id}</td>
                            <td className="p-4 text-white font-bold">{user.username}</td>
                            <td className="p-4 text-slate-300 text-sm">{user.email}</td>
                            <td className="p-4">
                                    <span className={`px-3 py-1 text-xs font-bold rounded-lg uppercase tracking-wider
                                        ${user.role === 'ADMIN' ? 'bg-red-500/20 text-red-400' :
                                        user.role === 'LECTURER' ? 'bg-purple-500/20 text-purple-400' :
                                            'bg-blue-500/20 text-blue-400'}`}
                                    >
                                        {user.role}
                                    </span>
                            </td>
                            <td className="p-4 text-right flex justify-end gap-2">
                                <button className="bg-blue-500/10 text-blue-400 p-2 rounded-lg hover:bg-blue-500 hover:text-white transition-all" title="Sửa User">
                                    <Edit2 size={16}/>
                                </button>
                                <button
                                    onClick={() => handleDeleteUser(user.id)}
                                    className="bg-red-500/10 text-red-400 p-2 rounded-lg hover:bg-red-500 hover:text-white transition-all" title="Xóa User"
                                    disabled={user.role === 'ADMIN'}
                                >
                                    <Trash2 size={16} className={user.role === 'ADMIN' ? 'opacity-50' : ''}/>
                                </button>
                            </td>
                        </tr>
                    ))}
                    {usersList.length === 0 && !loading && (
                        <tr><td colSpan={5} className="p-4 text-center text-slate-500">Không có dữ liệu</td></tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );

    const renderGroups = () => (
        <div className="bg-white/5 border border-white/10 rounded-3xl p-6">
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                    <FolderKanban className="text-emerald-400"/>
                    Quản lý Nhóm Đồ Án {loading && <span className="animate-spin text-sm">⟳</span>}
                </h2>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                    <tr className="border-b border-white/10 text-slate-400 text-sm">
                        <th className="p-4 font-medium uppercase tracking-wider">Mã Nhóm</th>
                        <th className="p-4 font-medium uppercase tracking-wider">Tên Đề Tài</th>
                        <th className="p-4 font-medium uppercase tracking-wider">Giảng Viên Hướng Dẫn</th>
                        <th className="p-4 font-medium uppercase tracking-wider text-right">Thao Tác</th>
                    </tr>
                    </thead>
                    <tbody>
                    {groups.map(group => (
                        <tr key={group.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                            <td className="p-4 font-mono text-sm text-white">{group.code}</td>
                            <td className="p-4 text-white font-bold">{group.name}</td>
                            <td className="p-4">
                                    <span className={`px-3 py-1 text-xs font-bold rounded-lg ${group.lecturer === 'Chưa phân công' ? 'bg-red-500/20 text-red-400' : 'bg-emerald-500/20 text-emerald-400'}`}>
                                        {group.lecturer}
                                    </span>
                            </td>
                            <td className="p-4 text-right">
                                <button onClick={() => handleEditGroup(group)} className="bg-emerald-500/20 text-emerald-400 p-2 rounded-lg hover:bg-emerald-500 hover:text-white transition-all">
                                    <Edit2 size={16}/>
                                </button>
                            </td>
                        </tr>
                    ))}
                    {groups.length === 0 && !loading && (
                        <tr><td colSpan={4} className="p-4 text-center text-slate-500">Không có dữ liệu</td></tr>
                    )}
                    </tbody>
                </table>
            </div>

            <AnimatePresence>
                {isEditModalOpen && (
                    <>
                        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setIsEditModalOpen(false)} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100" />
                        <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }} className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-[#0f172a] border border-white/10 rounded-3xl p-8 z-101 shadow-2xl">
                            <h2 className="text-xl font-bold text-white mb-6 uppercase flex justify-between">Chỉnh sửa nhóm <X size={20} className="cursor-pointer text-slate-400" onClick={() => setIsEditModalOpen(false)}/></h2>
                            <div className="space-y-4">
                                <div>
                                    <label className="text-xs font-bold text-slate-400 uppercase tracking-widest block mb-2">Tên nhóm</label>
                                    <input type="text" value={editingGroup.name} disabled className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-slate-400 cursor-not-allowed" />
                                </div>
                                <div>
                                    <label className="text-xs font-bold text-slate-400 uppercase tracking-widest block mb-2">Phân công Giảng Viên</label>
                                    <select
                                        value={editingGroup.lecturer}
                                        onChange={(e) => setEditingGroup({...editingGroup, lecturer: e.target.value})}
                                        className="w-full bg-black/40 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500 cursor-pointer"
                                    >
                                        <option value="Chưa phân công" className="text-black">-- Chọn Giảng viên --</option>
                                        {lecturerList.map(lec => (
                                            <option key={lec.id} value={lec.username} className="text-black">{lec.username}</option>
                                        ))}
                                    </select>
                                </div>
                                <button onClick={handleSaveGroup} className="w-full bg-blue-600 hover:bg-blue-500 text-white py-4 rounded-xl font-bold mt-4 flex items-center justify-center gap-2">
                                    <CheckCircle2 size={18}/> LƯU THAY ĐỔI
                                </button>
                            </div>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>
        </div>
    );

    return (
        <div className="w-full max-w-7xl mx-auto space-y-8">
            <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                Hệ Thống Quản Trị
            </h1>

            <div className="flex bg-black/20 p-1.5 rounded-2xl border border-white/5 w-fit overflow-x-auto max-w-full">
                <button onClick={() => setActiveTab('users')} className={`px-6 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all whitespace-nowrap ${activeTab === 'users' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}><Users size={18}/> Người Dùng</button>
                <button onClick={() => setActiveTab('groups')} className={`px-6 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all whitespace-nowrap ${activeTab === 'groups' ? 'bg-emerald-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}><FolderKanban size={18}/> Quản Lý Nhóm</button>
            </div>

            {activeTab === 'users' && renderUsers()}
            {activeTab === 'groups' && renderGroups()}
        </div>
    );
};

export default AdminDashboard;