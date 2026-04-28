import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, UserCog, Users as GroupIcon, Edit3, X, Save, CloudLightning } from 'lucide-react';
import StatCard from '../components/common/StatCard';
import { adminApi } from '../services/adminApi';
import { jiraApi } from '../services/jiraApi';
import { showLiquidToast } from '../utils/toast';
import CreateIssueModal from '../components/dashboard/CreateIssueModal';

interface Entity {
    userId?: number;
    username?: string;
    email?: string;
    userRole?: string;

    groupId?: number;
    groupCode?: string;
    groupName?: string;
    lecturerName?: string;
    status?: string;
    jiraUrl?: string;

    members?: {
        studentId: number;
        studentName: string;
        groupRole: 'LEADER' | 'MEMBER';
    }[];
}

// 🔥 Thêm Interface định nghĩa kiểu dữ liệu từ API để xoá sổ "any"
interface ApiUserDto {
    id: number;
    username: string;
    email: string;
    role?: string;
}

interface ApiGroupDto {
    id: number;
    groupCode?: string;
    groupName: string;
    lecturer?: { username: string };
    status?: string;
    jiraUrl?: string;
}

const AdminDashboard = () => {
    const [isIssueModalOpen, setIsIssueModalOpen] = useState(false);

    const [activeTab, setActiveTab] = useState<'users' | 'groups'>('users');
    const [searchQuery, setSearchQuery] = useState('');

    const [data, setData] = useState<Entity[]>([]);
    const [loading, setLoading] = useState(false);

    const [selectedEntity, setSelectedEntity] = useState<Entity | null>(null);
    const [isPanelOpen, setIsPanelOpen] = useState(false);

    const [syncingGroupId, setSyncingGroupId] = useState<number | null>(null);

    const applyMockData = () => {
        if (activeTab === 'users') {
            setData([
                { userId: 1, username: 'admin_vip', email: 'admin@uth.edu.vn', userRole: 'ADMIN' },
                { userId: 2, username: 'gv_nguyenvana', email: 'nva@uth.edu.vn', userRole: 'LECTURER' },
                { userId: 3, username: 'sv_tranthib', email: 'ttb@st.uth.edu.vn', userRole: 'STUDENT' },
                { userId: 4, username: 'sv_lethic', email: 'ltc@st.uth.edu.vn', userRole: 'STUDENT' },
            ]);
        } else {
            setData([
                { groupId: 1, groupCode: 'GR-01', groupName: 'Hệ thống SRPM', lecturerName: 'Thầy Hưng', status: 'ACTIVE', jiraUrl: 'https://jira.com/srpm' },
                { groupId: 2, groupCode: 'GR-02', groupName: 'Website Bán Hàng', lecturerName: 'Cô Lan', status: 'PENDING', jiraUrl: 'https://jira.com/web' },
                { groupId: 3, groupCode: 'GR-03', groupName: 'App Quản Lý Gym', lecturerName: '', status: 'INACTIVE', jiraUrl: '' },
            ]);
        }
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const res = activeTab === 'users' ? await adminApi.getUsers() : await adminApi.getGroups();
            const rawData = res.data.data;

            if (Array.isArray(rawData) && rawData.length > 0) {
                if (activeTab === 'users') {
                    // 🔥 Ép kiểu rawData thành mảng ApiUserDto[] trước khi map
                    const usersData = rawData as unknown as ApiUserDto[];
                    const mappedUsers: Entity[] = usersData.map(u => ({
                        userId: u.id,
                        username: u.username,
                        email: u.email,
                        userRole: u.role || 'STUDENT'
                    }));
                    setData(mappedUsers);
                } else {
                    // 🔥 Ép kiểu rawData thành mảng ApiGroupDto[] trước khi map
                    const groupsData = rawData as unknown as ApiGroupDto[];
                    const mappedGroups: Entity[] = groupsData.map(g => ({
                        groupId: g.id,
                        groupCode: g.groupCode || `GR-${g.id}`,
                        groupName: g.groupName,
                        lecturerName: g.lecturer?.username || '',
                        status: g.status || 'ACTIVE',
                        jiraUrl: g.jiraUrl || ''
                    }));
                    setData(mappedGroups);
                }
            } else {
                applyMockData();
            }

        } catch (error) {
            console.error("Lỗi fetch data Admin:", error);
            applyMockData();
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTab]);

    const filteredData = data.filter(item => {
        const searchTarget = activeTab === 'users'
            ? ((item?.username || '') + (item?.email || ''))
            : (item?.groupName || '');
        return searchTarget.toLowerCase().includes(searchQuery.toLowerCase());
    });

    const openEditPanel = (entity: Entity) => {
        setSelectedEntity(entity);
        setIsPanelOpen(true);
    };

    const handleSyncGroupToJira = async (groupId?: number, groupName?: string) => {
        if (!groupId) return;
        if (!window.confirm(`Bạn có chắc muốn tạo Project trên Jira cho nhóm "${groupName}" không?`)) return;

        setSyncingGroupId(groupId);
        try {
            await jiraApi.syncGroupToJira(groupId);
            showLiquidToast(`Đã tạo Project Jira cho nhóm ${groupName} thành công!`, 'success');
            void fetchData();
        } catch (error: unknown) {
            const err = error as { response?: { data?: { message?: string } } };
            showLiquidToast(err.response?.data?.message || 'Lỗi khi đồng bộ lên Jira!', 'error');
        } finally {
            setSyncingGroupId(null);
        }
    };

    return (
        <div className="space-y-8 relative">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <StatCard title="Tổng người dùng" value="128" icon={<UserCog />} colorClass="from-blue-500 to-cyan-400" />
                <StatCard title="Số nhóm đồ án" value="32" icon={<GroupIcon />} colorClass="from-purple-500 to-pink-500" />
                <StatCard title="Yêu cầu chờ duyệt" value="12" icon={<Edit3 />} colorClass="from-amber-500 to-orange-500" />
            </div>

            <div className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-4xl p-4 flex flex-col md:flex-row gap-6 items-center justify-between shadow-2xl">
                <div className="flex bg-black/20 p-1.5 rounded-2xl border border-white/5">
                    <button
                        onClick={() => setActiveTab('users')}
                        className={`px-8 py-2.5 rounded-xl font-bold transition-all ${activeTab === 'users' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}
                    >
                        Người dùng
                    </button>
                    <button
                        onClick={() => setActiveTab('groups')}
                        className={`px-8 py-2.5 rounded-xl font-bold transition-all ${activeTab === 'groups' ? 'bg-blue-600 text-white shadow-lg' : 'text-slate-400 hover:text-white'}`}
                    >
                        Nhóm đồ án
                    </button>
                </div>

                <div className="relative w-full md:w-96 group">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-blue-400 transition-colors w-5 h-5" />
                    <input
                        type="text"
                        placeholder={activeTab === 'users' ? "Tìm theo tên, email..." : "Tìm tên nhóm..."}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full bg-white/5 border border-white/10 rounded-2xl py-3 pl-12 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50 backdrop-blur-md transition-all"
                    />
                </div>
            </div>

            <div className="grid grid-cols-1 gap-4">
                <AnimatePresence mode='popLayout'>
                    {loading ? (
                        <div className="text-center py-20 text-slate-400 font-medium">Đang tải dữ liệu...</div>
                    ) : filteredData.map((item, idx) => (
                        <motion.div
                            key={activeTab === 'users' ? item.userId : item.groupId}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: idx * 0.05 }}
                            className="group bg-white/5 hover:bg-white/10 backdrop-blur-md border border-white/10 rounded-3xl p-5 flex items-center justify-between transition-all hover:border-white/20 shadow-sm"
                        >
                            <div className="flex items-center gap-5">
                                <div className={`w-14 h-14 rounded-2xl flex items-center justify-center font-bold text-xl text-white shadow-inner bg-linear-to-br ${activeTab === 'users' ? 'from-blue-600/40 to-cyan-500/40' : 'from-purple-600/40 to-pink-500/40'}`}>
                                    {activeTab === 'users' ? item.username?.charAt(0).toUpperCase() : 'G' + item.groupId}
                                </div>
                                <div>
                                    <h3 className="text-white font-bold text-lg flex items-center gap-2">
                                        {activeTab === 'users' ? item.username : item.groupName}
                                        {activeTab === 'groups' && item.jiraUrl && (
                                            <a href={item.jiraUrl} target="_blank" rel="noreferrer" className="text-xs bg-blue-500/20 text-blue-400 px-2 py-0.5 rounded-md hover:bg-blue-500/40 transition-colors">
                                                Mở Jira
                                            </a>
                                        )}
                                    </h3>
                                    <p className="text-slate-400 text-sm">{activeTab === 'users' ? item.email : `Giảng viên: ${item.lecturerName || 'Chưa phân công'}`}</p>
                                </div>
                            </div>

                            <div className="flex items-center gap-3">
                                <span className={`px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest bg-white/5 border border-white/10 text-slate-300 mr-2`}>
                                    {activeTab === 'users' ? item.userRole : item.status}
                                </span>

                                {activeTab === 'groups' && (
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            void handleSyncGroupToJira(item.groupId, item.groupName);
                                        }}
                                        disabled={syncingGroupId === item.groupId}
                                        title="Tạo Project trên Jira"
                                        className={`p-3 rounded-xl transition-all border ${syncingGroupId === item.groupId ? 'bg-indigo-500/50 border-indigo-500/50 text-white animate-pulse' : 'bg-white/5 border-white/10 text-indigo-300 hover:bg-indigo-600 hover:text-white hover:border-indigo-400'} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <CloudLightning className={`w-5 h-5 ${syncingGroupId === item.groupId ? 'animate-bounce' : ''}`} />
                                    </button>
                                )}

                                <button
                                    onClick={() => openEditPanel(item)}
                                    className="p-3 bg-white/5 hover:bg-blue-600 text-white rounded-xl transition-all border border-white/10 hover:border-blue-400"
                                >
                                    <Edit3 className="w-5 h-5" />
                                </button>
                            </div>
                        </motion.div>
                    ))}
                </AnimatePresence>
            </div>

            <AnimatePresence>
                {isPanelOpen && (
                    <>
                        <motion.div
                            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                            onClick={() => setIsPanelOpen(false)}
                            className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100"
                        />
                        <motion.div
                            initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }}
                            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                            className="fixed top-0 right-0 bottom-0 w-full max-w-md bg-[#0f172a]/80 backdrop-blur-3xl border-l border-white/10 z-101 p-8 shadow-[-20px_0_50px_rgba(0,0,0,0.5)] overflow-y-auto"
                        >
                            <div className="flex items-center justify-between mb-10">
                                <h2 className="text-2xl font-black text-white tracking-tight uppercase">Chỉnh sửa thông tin</h2>
                                <button onClick={() => setIsPanelOpen(false)} className="p-2 hover:bg-white/10 rounded-full text-slate-400 transition-colors"><X /></button>
                            </div>

                            {selectedEntity && (
                                <div className="space-y-8 pb-10">
                                    <div className="p-6 bg-white/5 rounded-4xl border border-white/10 text-center">
                                        <div className="w-20 h-20 mx-auto bg-blue-600 rounded-3xl mb-4 flex items-center justify-center text-3xl font-black text-white">
                                            {activeTab === 'users' ? selectedEntity.username?.charAt(0) : 'G'}
                                        </div>
                                        <h4 className="text-white text-xl font-bold">{activeTab === 'users' ? selectedEntity.username : selectedEntity.groupName}</h4>
                                        <p className="text-slate-400">{activeTab === 'users' ? selectedEntity.email : 'Mã nhóm: ' + selectedEntity.groupCode}</p>
                                    </div>

                                    <div className="space-y-6">
                                        {activeTab === 'users' ? (
                                            <div className="space-y-2">
                                                <label className="text-sm font-bold text-slate-400 ml-1 uppercase tracking-widest">Quyền hạn (Role)</label>
                                                <select defaultValue={selectedEntity.userRole} className="w-full bg-white/5 border border-white/10 rounded-2xl p-4 text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
                                                    <option value="STUDENT" className='text-black'>Sinh viên</option>
                                                    <option value="LECTURER" className='text-black'>Giảng viên</option>
                                                    <option value="ADMIN" className='text-black'>Admin</option>
                                                </select>
                                            </div>
                                        ) : (
                                            <div className="space-y-6">
                                                <div className="space-y-4">
                                                    <div className="space-y-2">
                                                        <label className="text-sm font-bold text-slate-400 uppercase tracking-widest">Tên nhóm</label>
                                                        <input type="text" defaultValue={selectedEntity.groupName} className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500" />
                                                    </div>
                                                    <div className="grid grid-cols-2 gap-4">
                                                        <div className="space-y-2">
                                                            <label className="text-sm font-bold text-slate-400 uppercase tracking-widest">Jira URL</label>
                                                            <input type="text" defaultValue={selectedEntity.jiraUrl} className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500" />
                                                        </div>
                                                        <div className="space-y-2">
                                                            <label className="text-sm font-bold text-slate-400 uppercase tracking-widest">Giảng viên HD</label>
                                                            <select defaultValue={selectedEntity.lecturerName ? 'assigned' : 'none'} className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
                                                                <option value="none" className="text-black">-- Chưa chọn --</option>
                                                                <option value="assigned" className="text-black">{selectedEntity.lecturerName || 'Nguyễn Văn A'}</option>
                                                            </select>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="bg-white/5 border border-white/10 rounded-3xl p-5">
                                                    <h3 className="text-white font-bold mb-4 flex items-center justify-between">
                                                        <span>Thành viên nhóm</span>
                                                        <span className="bg-blue-600/20 text-blue-400 text-xs px-2 py-1 rounded-lg">
                                                            {selectedEntity.members?.length || 0}/5
                                                        </span>
                                                    </h3>

                                                    <div className="space-y-3">
                                                        {(selectedEntity.members || [
                                                            { studentId: 1, studentName: "Trần Sinh Viên A", groupRole: "LEADER" },
                                                            { studentId: 2, studentName: "Lê Sinh Viên B", groupRole: "MEMBER" }
                                                        ]).map((mem, i) => (
                                                            <div key={i} className="flex items-center justify-between bg-white/5 border border-white/5 p-3 rounded-2xl hover:bg-white/10 transition-colors">
                                                                <div className="flex items-center gap-3">
                                                                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${mem.groupRole === 'LEADER' ? 'bg-amber-500 text-white' : 'bg-slate-700 text-slate-300'}`}>
                                                                        {mem.studentName.charAt(0)}
                                                                    </div>
                                                                    <span className="text-slate-200 text-sm font-medium truncate max-w-30">{mem.studentName}</span>
                                                                </div>
                                                                <select
                                                                    defaultValue={mem.groupRole}
                                                                    className={`text-xs font-bold rounded-lg px-2 py-1 focus:outline-none cursor-pointer ${mem.groupRole === 'LEADER' ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30' : 'bg-slate-800 text-slate-400 border border-slate-700'}`}
                                                                >
                                                                    <option value="LEADER" className="text-black">Nhóm Trưởng</option>
                                                                    <option value="MEMBER" className="text-black">Thành viên</option>
                                                                </select>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <button
                                        onClick={() => {
                                            showLiquidToast('Cập nhật thành công!', 'success');
                                            setIsPanelOpen(false);
                                        }}
                                        className="w-full bg-linear-to-r from-blue-600 to-cyan-500 text-white p-5 rounded-3xl font-black shadow-xl hover:-translate-y-1 active:translate-y-0.5 transition-all flex items-center justify-center gap-3 mt-4"
                                    >
                                        <Save /> LƯU THAY ĐỔI
                                    </button>
                                </div>
                            )}
                        </motion.div>
                    </>
                )}
            </AnimatePresence>

            <button onClick={() => setIsIssueModalOpen(true)} className="p-3 bg-blue-600 text-white rounded-xl">Test Form Tạo Issue</button>
            <CreateIssueModal isOpen={isIssueModalOpen} onClose={() => setIsIssueModalOpen(false)} />
        </div>
    );
};

export default AdminDashboard;