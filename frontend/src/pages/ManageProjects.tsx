import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { LayoutDashboard, Users, Clock, CheckCircle2, AlertCircle, ChevronRight, BookOpen } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { issueApi } from '../services/issueApi';

interface GroupItem {
    groupId: number;
    groupCode: string;
    groupName: string;
    lecturerId?: number;
}

interface IssueItem {
    id: number;
    title: string;
    issueType: string;
    status: string;
    assignee: { student: { username: string } } | null;
}

interface ApiGroupDto {
    id: number;
    groupCode?: string;
    groupName: string;
    lecturer?: { id: number; username: string };
}

const ManageProjects = () => {
    const [user, setUser] = useState<{ id?: number; username?: string } | null>(null);
    const [myGroups, setMyGroups] = useState<GroupItem[]>([]);
    const [selectedGroup, setSelectedGroup] = useState<GroupItem | null>(null);
    const [groupIssues, setGroupIssues] = useState<IssueItem[]>([]);
    const [loadingGroups, setLoadingGroups] = useState(true);
    const [loadingIssues, setLoadingIssues] = useState(false);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            void fetchMyGroups(parsedUser.id);
        }
    }, []);

    const fetchMyGroups = async (lecturerId?: number) => {
        setLoadingGroups(true);
        try {
            const res = await adminApi.getGroups();
            const allGroups = res.data.data as unknown as ApiGroupDto[];

            const mappedGroups = allGroups.map(g => ({
                groupId: g.id,
                groupCode: g.groupCode || `GR-${g.id}`,
                groupName: g.groupName,
                lecturerId: g.lecturer?.id
            }));

            // 🔥 Lọc chuẫn nghiệp vụ: CHỈ hiển thị nhóm CỦA giảng viên này
            const filtered = mappedGroups.filter(g => g.lecturerId === lecturerId);
            setMyGroups(filtered); // Xóa dòng fallback ở đây

        } catch (error) {
            console.error("Lỗi fetch groups:", error);
            // Xóa luôn phần mock fallback ở dưới catch
            setMyGroups([]);
        } finally {
            setLoadingGroups(false);
        }
    };

    const fetchGroupIssues = async (groupId: number) => {
        setLoadingIssues(true);
        try {
            const res = await issueApi.getIssuesByGroup(groupId);
            setGroupIssues((res.data.data as unknown as IssueItem[]) || []);
        } catch (error) {
            console.error("Lỗi fetch issues:", error);
            // Vẫn giữ fallback issue để test giao diện Kanban nếu nhóm chưa có issue thật
            setGroupIssues([
                { id: 1, title: 'Lên ý tưởng đồ án', issueType: 'EPIC', status: 'DONE', assignee: { student: { username: 'lam_sv' } } },
                { id: 2, title: 'Thiết kế API', issueType: 'TASK', status: 'IN_PROGRESS', assignee: { student: { username: 'nam_sv' } } },
            ]);
        } finally {
            setLoadingIssues(false);
        }
    };

    const handleSelectGroup = (group: GroupItem) => {
        setSelectedGroup(group);
        void fetchGroupIssues(group.groupId);
    };

    const renderIssueCard = (issue: IssueItem) => (
        <motion.div key={issue.id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="bg-white/5 border border-white/10 rounded-2xl p-4 shadow-sm group hover:bg-white/10 transition-all">
            <div className="flex justify-between items-start mb-3">
                <div className={`px-2 py-1 rounded text-[10px] font-black uppercase tracking-wider ${issue.issueType === 'BUG' ? 'bg-red-500/20 text-red-400' : issue.issueType === 'EPIC' ? 'bg-purple-500/20 text-purple-400' : 'bg-blue-500/20 text-blue-400'}`}>
                    {issue.issueType}
                </div>
                <span className="text-xs text-slate-500 font-mono">#{issue.id}</span>
            </div>
            <h4 className="text-white font-bold text-sm mb-4 leading-relaxed">{issue.title}</h4>
            <div className="flex items-center gap-2 mt-auto">
                <div className="w-6 h-6 rounded-full bg-linear-to-br from-slate-700 to-slate-600 flex items-center justify-center text-[10px] font-bold text-white border border-white/10" title={issue.assignee?.student?.username || 'Chưa phân công'}>
                    {issue.assignee ? issue.assignee.student.username.charAt(0).toUpperCase() : '?'}
                </div>
                <span className="text-xs text-slate-400 truncate max-w-20">
                    {issue.assignee ? issue.assignee.student.username : 'Chưa giao'}
                </span>
            </div>
        </motion.div>
    );

    return (
        <div className="space-y-8 h-[calc(100vh-140px)] flex flex-col">
            <div className="shrink-0">
                <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                    <BookOpen className="text-purple-500" /> Quản Lý Đồ Án
                </h1>
                <p className="text-slate-400 mt-2">Xin chào Giảng viên <span className="text-purple-400 font-bold">{user?.username}</span>! Theo dõi tiến độ của các nhóm bên dưới.</p>
            </div>

            <div className="flex flex-col lg:flex-row gap-6 flex-1 min-h-0">
                <div className="lg:w-1/3 bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col min-h-75 overflow-hidden">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5 shrink-0">
                        <Users className="w-5 h-5 text-slate-400" />
                        <h3 className="font-bold text-slate-300 uppercase tracking-widest text-sm">Nhóm hướng dẫn</h3>
                        <span className="ml-auto bg-white/10 text-xs py-1 px-3 rounded-full text-white">{myGroups.length}</span>
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                        {loadingGroups ? <p className="text-slate-500 text-sm text-center">Đang tải...</p> :
                            myGroups.length === 0 ? (
                                <p className="text-slate-500 text-sm text-center italic">Chưa có nhóm nào được phân công.</p>
                            ) : (
                                myGroups.map(group => (
                                    <button
                                        key={group.groupId}
                                        onClick={() => handleSelectGroup(group)}
                                        className={`w-full text-left p-4 rounded-2xl border transition-all flex items-center justify-between ${selectedGroup?.groupId === group.groupId ? 'bg-purple-600/20 border-purple-500/50' : 'bg-white/5 border-white/5 hover:border-white/20'}`}
                                    >
                                        <div>
                                            <div className="text-xs font-mono text-purple-400 mb-1">{group.groupCode}</div>
                                            <div className="text-white font-bold">{group.groupName}</div>
                                        </div>
                                        <ChevronRight className={`w-5 h-5 transition-transform ${selectedGroup?.groupId === group.groupId ? 'text-purple-400 translate-x-1' : 'text-slate-600'}`} />
                                    </button>
                                ))
                            )
                        }
                    </div>
                </div>

                <div className="lg:w-2/3 bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col overflow-hidden">
                    {!selectedGroup ? (
                        <div className="flex-1 flex flex-col items-center justify-center text-slate-500">
                            <LayoutDashboard className="w-16 h-16 mb-4 opacity-20" />
                            <p>Chọn một nhóm bên trái để xem tiến độ chi tiết</p>
                        </div>
                    ) : (
                        <>
                            <div className="flex items-center justify-between mb-6 pb-4 border-b border-white/5 shrink-0">
                                <div>
                                    <h3 className="font-bold text-white text-lg">{selectedGroup.groupName}</h3>
                                    <p className="text-xs text-slate-400 font-mono mt-1">Mã nhóm: {selectedGroup.groupCode}</p>
                                </div>
                            </div>

                            <div className="flex-1 overflow-x-auto overflow-y-hidden custom-scrollbar pb-2">
                                <div className="flex gap-4 h-full min-w-175">
                                    <div className="flex-1 flex flex-col bg-white/5 rounded-2xl p-4 border border-white/5">
                                        <div className="flex items-center gap-2 mb-4 shrink-0">
                                            <AlertCircle className="w-4 h-4 text-slate-400" />
                                            <span className="font-bold text-sm text-slate-300">CẦN LÀM</span>
                                        </div>
                                        <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                                            {loadingIssues ? <p className="text-slate-500 text-xs">Đang tải...</p> : groupIssues.filter(i => i.status === 'TODO').map(renderIssueCard)}
                                        </div>
                                    </div>

                                    <div className="flex-1 flex flex-col bg-white/5 rounded-2xl p-4 border border-white/5">
                                        <div className="flex items-center gap-2 mb-4 shrink-0">
                                            <Clock className="w-4 h-4 text-blue-400" />
                                            <span className="font-bold text-sm text-blue-400">ĐANG LÀM</span>
                                        </div>
                                        <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                                            {loadingIssues ? <p className="text-slate-500 text-xs">Đang tải...</p> : groupIssues.filter(i => i.status === 'IN_PROGRESS').map(renderIssueCard)}
                                        </div>
                                    </div>

                                    <div className="flex-1 flex flex-col bg-white/5 rounded-2xl p-4 border border-white/5">
                                        <div className="flex items-center gap-2 mb-4 shrink-0">
                                            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                                            <span className="font-bold text-sm text-emerald-400">ĐÃ XONG</span>
                                        </div>
                                        <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                                            {loadingIssues ? <p className="text-slate-500 text-xs">Đang tải...</p> : groupIssues.filter(i => i.status === 'DONE').map(renderIssueCard)}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ManageProjects;