import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    LayoutDashboard, FolderKanban, ChevronRight,
    UserCircle, AlertCircle, ListTodo, Clock,
    CheckCircle2, AlertTriangle
} from 'lucide-react';
import apiClient from '../services/apiClient';
import { issueApi } from '../services/issueApi';
import { showLiquidToast } from '../utils/toast';

const StudentDashboard = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState<any>(null);
    const [groups, setGroups] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [myIssues, setMyIssues] = useState<any[]>([]);
    const [loadingIssues, setLoadingIssues] = useState(false);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            void loadGroups(parsedUser.id);
            void loadMyIssues();
        } else {
            setLoading(false);
        }
    }, []);

    const loadMyIssues = async () => {
        setLoadingIssues(true);
        try {
            const res = await issueApi.getMyAssigned();
            if (res.data.success) {
                const data = res.data.data;
                setMyIssues(Array.isArray(data) ? data : []);
            }
        } catch (err: any) {
        } finally {
            setLoadingIssues(false);
        }
    };

    const loadGroups = async (studentId: number) => {
        try {
            const res = await apiClient.get(`/api/groups/student/${studentId}`);
            if (res.data.success) {
                const groupData = res.data.data;
                setGroups(Array.isArray(groupData) ? groupData : []);
            }
        } catch (err: any) {
            showLiquidToast('Không thể tải danh sách nhóm', 'error');
        } finally {
            setLoading(false);
        }
    };

    const statusIcon = (status: string) => {
        switch (status) {
            case 'DONE': return <CheckCircle2 size={14} className="text-emerald-400" />;
            case 'IN_PROGRESS': return <Clock size={14} className="text-blue-400" />;
            default: return <AlertTriangle size={14} className="text-slate-400" />;
        }
    };

    const statusLabel = (status: string) => {
        switch (status) {
            case 'DONE': return 'Hoàn thành';
            case 'IN_PROGRESS': return 'Đang làm';
            default: return 'Chờ';
        }
    };

    const inProgressCount = myIssues.filter((i: any) => i.status === 'IN_PROGRESS').length;
    const doneCount = myIssues.filter((i: any) => i.status === 'DONE').length;

    return (
        <div className="space-y-8 w-full animate-in fade-in duration-500">
            {/* Header */}
            <div className="flex justify-between items-start">
                <div>
                    <h1 className="text-3xl font-black text-white flex items-center gap-3">
                        <LayoutDashboard className="text-blue-400" />
                        Xin chào, {user?.username}!
                    </h1>
                    <p className="text-slate-400 mt-2 flex items-center gap-2">
                        <UserCircle size={16} className="text-slate-500" />
                        {user?.email}
                        <span className="w-1 h-1 rounded-full bg-slate-600 mx-1" />
                        <span className="text-xs font-mono text-slate-500">Sinh viên</span>
                    </p>
                </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.05 }}
                    className="bg-linear-to-br from-blue-600/10 to-transparent border border-blue-500/20 rounded-2xl p-6"
                >
                    <div className="flex items-center justify-between mb-4">
                        <span className="text-blue-400 text-[10px] font-black uppercase tracking-widest">Dự Án Tham Gia</span>
                        <div className="w-10 h-10 bg-blue-600/20 rounded-xl flex items-center justify-center">
                            <FolderKanban size={20} className="text-blue-400" />
                        </div>
                    </div>
                    <p className="text-4xl font-black text-white">
                        {loading ? <span className="animate-pulse">...</span> : groups.length}
                    </p>
                    <p className="text-xs text-slate-500 mt-1">Nhóm đang hoạt động</p>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.1 }}
                    className="bg-linear-to-br from-slate-600/10 to-transparent border border-slate-500/20 rounded-2xl p-6"
                >
                    <div className="flex items-center justify-between mb-4">
                        <span className="text-slate-400 text-[10px] font-black uppercase tracking-widest">Việc Được Giao</span>
                        <div className="w-10 h-10 bg-slate-600/20 rounded-xl flex items-center justify-center">
                            <ListTodo size={20} className="text-slate-400" />
                        </div>
                    </div>
                    <p className="text-4xl font-black text-white">
                        {loadingIssues ? <span className="animate-pulse">...</span> : myIssues.length}
                    </p>
                    <p className="text-xs text-slate-500 mt-1">Tất cả Issue được giao</p>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.15 }}
                    className="bg-linear-to-br from-emerald-600/10 to-transparent border border-emerald-500/20 rounded-2xl p-6"
                >
                    <div className="flex items-center justify-between mb-4">
                        <span className="text-emerald-400 text-[10px] font-black uppercase tracking-widest">Đã Hoàn Thành</span>
                        <div className="w-10 h-10 bg-emerald-600/20 rounded-xl flex items-center justify-center">
                            <CheckCircle2 size={20} className="text-emerald-400" />
                        </div>
                    </div>
                    <p className="text-4xl font-black text-emerald-400">{doneCount}</p>
                    <p className="text-xs text-slate-500 mt-1">Issue đã hoàn thành</p>
                </motion.div>

                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.2 }}
                    className="bg-linear-to-br from-blue-600/10 to-transparent border border-blue-500/20 rounded-2xl p-6"
                >
                    <div className="flex items-center justify-between mb-4">
                        <span className="text-blue-400 text-[10px] font-black uppercase tracking-widest">Đang Xử Lý</span>
                        <div className="w-10 h-10 bg-blue-600/20 rounded-xl flex items-center justify-center">
                            <Clock size={20} className="text-blue-400" />
                        </div>
                    </div>
                    <p className="text-4xl font-black text-blue-400">{inProgressCount}</p>
                    <p className="text-xs text-slate-500 mt-1">Issue đang xử lý</p>
                </motion.div>
            </div>

            {/* Groups List */}
            <div>
                <div className="flex items-center gap-3 mb-5">
                    <h2 className="text-xl font-black text-white">Nhóm Của Tôi</h2>
                    <span className="px-2.5 py-1 bg-blue-600/20 text-blue-400 rounded-full text-[10px] font-bold">
                        {groups.length} nhóm
                    </span>
                </div>

                {loading ? (
                    <div className="bg-white/2 border border-white/10 rounded-3xl p-20 text-center">
                        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto" />
                    </div>
                ) : groups.length === 0 ? (
                    <div className="bg-white/2 border border-white/10 rounded-3xl p-16 text-center">
                        <AlertCircle className="w-12 h-12 mx-auto mb-4 text-slate-600" />
                        <p className="text-slate-500 text-lg font-bold">Chưa tham gia nhóm nào</p>
                        <p className="text-slate-600 text-sm mt-2">Bạn sẽ được thêm vào nhóm bởi giảng viên hoặc quản trị viên.</p>
                    </div>
                ) : (
                    <div className="bg-white/2 border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
                        <div className="max-h-125 overflow-y-auto custom-scrollbar">
                            <table className="w-full text-left border-collapse">
                                <thead className="sticky top-0 bg-[#0a0f1e] z-10 border-b border-white/10">
                                    <tr className="text-slate-500 text-[10px] uppercase font-black tracking-widest">
                                        <th className="p-5">Mã Nhóm</th>
                                        <th className="p-5">Tên Đề Tài</th>
                                        <th className="p-5">Giảng Viên</th>
                                        <th className="p-5 text-center">Số TV</th>
                                        <th className="p-5 text-right">Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-white/5">
                                    {groups.map((group: any, idx: number) => (
                                        <motion.tr
                                            key={group.id}
                                            initial={{ opacity: 0, y: 8 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            transition={{ delay: idx * 0.03 }}
                                            onClick={() => navigate(`/groups/${group.id}`)}
                                            className="hover:bg-white/5 cursor-pointer transition-colors group"
                                        >
                                            <td className="p-5 font-mono font-black text-blue-400 text-sm">
                                                {group.groupId || '—'}
                                            </td>
                                            <td className="p-5">
                                                <span className="font-bold text-white group-hover:text-blue-400 transition-colors">
                                                    {group.groupName}
                                                </span>
                                            </td>
                                            <td className="p-5">
                                                <div className="flex items-center gap-2">
                                                    <UserCircle size={16} className="text-purple-400" />
                                                    <span className="text-sm text-slate-300">
                                                        {group.lecturerUsername || '—'}
                                                    </span>
                                                </div>
                                            </td>
                                            <td className="p-5 text-center">
                                                <span className="px-2.5 py-1 bg-white/5 text-slate-300 rounded-lg text-xs font-bold">
                                                    {group.members?.length || group.studentIds?.length || 0}
                                                </span>
                                            </td>
                                            <td className="p-5 text-right">
                                                <ChevronRight size={16} className="text-slate-600 group-hover:text-white transition-colors inline" />
                                            </td>
                                        </motion.tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>

            {/* Issue được giao cho tôi */}
            <div>
                <div className="flex items-center gap-3 mb-5">
                    <h2 className="text-xl font-black text-white flex items-center gap-2">
                        <ListTodo className="text-blue-400" size={22} />
                        Issue Được Giao Cho Tôi
                    </h2>
                    <span className="px-2.5 py-1 bg-blue-600/20 text-blue-400 rounded-full text-[10px] font-bold">
                        {myIssues.length} issue
                    </span>
                    <button onClick={loadMyIssues} className="p-1.5 bg-white/5 hover:bg-white/10 rounded-lg text-slate-500 hover:text-white transition-all" title="Làm mới">
                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                            <path d="M3 3v5h5"/>
                        </svg>
                    </button>
                </div>

                {loadingIssues ? (
                    <div className="bg-white/2 border border-white/10 rounded-3xl p-20 text-center">
                        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto" />
                    </div>
                ) : myIssues.length === 0 ? (
                    <div className="bg-white/2 border border-white/10 rounded-3xl p-16 text-center">
                        <ListTodo className="w-12 h-12 mx-auto mb-4 text-slate-600" />
                        <p className="text-slate-500 text-lg font-bold">Chưa có Issue nào</p>
                        <p className="text-slate-600 text-sm mt-2">Bạn chưa được gán Issue nào. Hãy kiểm tra lại sau.</p>
                    </div>
                ) : (
                    <div className="bg-white/2 border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
                        <div className="max-h-150 overflow-y-auto custom-scrollbar">
                            <table className="w-full text-left border-collapse">
                                <thead className="sticky top-0 bg-[#0a0f1e] z-10 border-b border-white/10">
                                    <tr className="text-slate-500 text-[10px] uppercase font-black tracking-widest">
                                        <th className="p-5">Nhóm</th>
                                        <th className="p-5">Issue</th>
                                        <th className="p-5">Loại</th>
                                        <th className="p-5">Trạng Thái</th>
                                        <th className="p-5">Deadline</th>
                                        <th className="p-5 text-right">Thao Tác</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-white/5">
                                    {myIssues.map((issue: any, idx: number) => (
                                        <motion.tr
                                            key={issue.issueId}
                                            initial={{ opacity: 0, y: 8 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            transition={{ delay: idx * 0.02 }}
                                            onClick={() => navigate(`/groups/${issue.groupId || (issue.group?.id)}`)}
                                            className="hover:bg-white/5 cursor-pointer transition-colors group"
                                        >
                                            <td className="p-5">
                                                <span className="font-mono text-xs font-bold text-purple-400">
                                                    {issue.groupName || issue.group?.groupName || `Nhóm #${issue.groupId || issue.group?.id}`}
                                                </span>
                                            </td>
                                            <td className="p-5">
                                                <div className="flex items-center gap-2">
                                                    <span className="font-mono text-[11px] text-blue-400 font-bold">
                                                        {issue.issueCode || ''}
                                                    </span>
                                                    <span className="font-bold text-white group-hover:text-blue-400 transition-colors text-sm">
                                                        {issue.title}
                                                    </span>
                                                </div>
                                            </td>
                                            <td className="p-5">
                                                <span className={`px-2 py-0.5 rounded-md text-[9px] font-black uppercase tracking-wider border ${
                                                    issue.type === 'EPIC' ? 'border-purple-500/30 text-purple-400 bg-purple-500/10' :
                                                    issue.type === 'SUB_TASK' ? 'border-cyan-500/30 text-cyan-400 bg-cyan-500/10' :
                                                    issue.type === 'BUG' ? 'border-red-500/30 text-red-400 bg-red-500/10' :
                                                    issue.type === 'STORY' ? 'border-emerald-500/30 text-emerald-400 bg-emerald-500/10' :
                                                    'border-blue-500/30 text-blue-400 bg-blue-500/10'
                                                }`}>
                                                    {issue.type}
                                                </span>
                                            </td>
                                            <td className="p-5">
                                                <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold ${
                                                    issue.status === 'DONE' ? 'text-emerald-400 bg-emerald-500/10' :
                                                    issue.status === 'IN_PROGRESS' ? 'text-blue-400 bg-blue-500/10' :
                                                    'text-slate-400 bg-slate-500/10'
                                                }`}>
                                                    {statusIcon(issue.status)}
                                                    {statusLabel(issue.status)}
                                                </div>
                                            </td>
                                            <td className="p-5 text-slate-400 text-xs font-mono">
                                                {issue.deadline ? new Date(issue.deadline).toLocaleDateString('vi-VN') : '—'}
                                            </td>
                                            <td className="p-5 text-right">
                                                <ChevronRight size={16} className="text-slate-600 group-hover:text-white transition-colors inline" />
                                            </td>
                                        </motion.tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StudentDashboard;