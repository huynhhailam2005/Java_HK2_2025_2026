import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Users, BarChart3, ChevronLeft, Plus,
    ListTodo, Trash2, Crown, Search, CheckCircle,
    CircleDashed, AlertCircle
} from 'lucide-react';
import { showLiquidToast } from '../utils/toast';

interface IssueItem {
    issueId: number;
    issueCode: string;
    title: string;
    assigneeUsername: string;
    status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
    issueType: 'TASK' | 'BUG' | 'STORY' | 'SUB_TASK' | 'EPIC';
}

interface Member { id: number; name: string; role: 'LEADER' | 'MEMBER'; code: string; }

const GroupWorkspace = () => {
    const { groupId } = useParams();
    const navigate = useNavigate();

    const [activeTab, setActiveTab] = useState<'tasks' | 'members' | 'report'>('tasks');
    const [loading, setLoading] = useState(true);

    const [issues, setIssues] = useState<IssueItem[]>([]);
    const [members, setMembers] = useState<Member[]>([]);

    const [taskFilter, setTaskFilter] = useState<'ALL' | 'MINE' | string>('ALL');
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [newTask, setNewTask] = useState({ title: '', issueType: 'TASK', assigneeUsername: '' });

    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
    const [memberSearch, setMemberSearch] = useState('');

    const ALL_STUDENTS = [
        { id: 4, name: 'sv_hailam', code: 'SV-001' },
        { id: 5, name: 'sv_an', code: 'SV-002' },
        { id: 6, name: 'sv_binh', code: 'SV-003' },
        { id: 7, name: 'sv_cuong', code: 'SV-004' }
    ];

    const userDataStr = localStorage.getItem('user');
    const currentUser = userDataStr ? JSON.parse(userDataStr) : { username: 'Guest', role: 'STUDENT' };

    const CURRENT_USER_NAME = currentUser.username;
    const IS_LECTURER = currentUser.role === 'LECTURER';
    const IS_LEADER = members.some(m => m.name === CURRENT_USER_NAME && m.role === 'LEADER');

    useEffect(() => {
        let isMounted = true;
        const fetchData = async () => {
            if (isMounted) setLoading(true);
            await new Promise(r => setTimeout(r, 400));
            if (isMounted) {
                setIssues([
                    { issueId: 1, issueCode: 'ISSUE-01', title: 'Thiết kế Database hệ thống', assigneeUsername: 'sv_hailam', status: 'DONE', issueType: 'EPIC' },
                    { issueId: 2, issueCode: 'ISSUE-02', title: 'Code API Đăng nhập', assigneeUsername: 'sv_an', status: 'IN_PROGRESS', issueType: 'TASK' },
                    { issueId: 3, issueCode: 'ISSUE-03', title: 'Fix bug giao diện Mobile', assigneeUsername: CURRENT_USER_NAME, status: 'TODO', issueType: 'BUG' },
                ]);
                setMembers([
                    { id: 4, name: 'sv_hailam', role: 'LEADER', code: 'SV-001' },
                    { id: 5, name: 'sv_an', role: 'MEMBER', code: 'SV-002' },
                ]);
                setLoading(false);
            }
        };
        void fetchData();
        return () => { isMounted = false; };
    }, [groupId, CURRENT_USER_NAME]);

    const handleStatusChange = (issueId: number, newStatus: string) => {
        setIssues(issues.map(t => t.issueId === issueId ? { ...t, status: newStatus as IssueItem['status'] } : t));
        showLiquidToast('Đã cập nhật trạng thái', 'success');
    };

    const handleCreateTask = () => {
        if (!newTask.title || !newTask.assigneeUsername) return showLiquidToast('Vui lòng nhập tên và chọn người làm!', 'error');
        const createdIssue: IssueItem = {
            issueId: Date.now(),
            issueCode: `ISSUE-0${issues.length + 1}`,
            title: newTask.title,
            assigneeUsername: newTask.assigneeUsername,
            status: 'TODO',
            issueType: newTask.issueType as IssueItem['issueType']
        };
        setIssues([createdIssue, ...issues]);
        setNewTask({ title: '', issueType: 'TASK', assigneeUsername: '' });
        setIsTaskModalOpen(false);
        showLiquidToast(`Đã giao việc thành công!`, 'success');
    };

    const handleSetLeader = (memberId: number) => {
        setMembers(members.map(m => ({ ...m, role: m.id === memberId ? 'LEADER' : 'MEMBER' })));
        showLiquidToast('Đã chỉ định Nhóm trưởng mới', 'success');
    };

    const handleRemoveMember = (memberId: number) => {
        setMembers(members.filter(m => m.id !== memberId));
        showLiquidToast('Đã xóa sinh viên khỏi nhóm', 'success');
    };

    const handleAddMemberFromList = (student: typeof ALL_STUDENTS[0]) => {
        if (members.some(m => m.id === student.id)) return showLiquidToast('Sinh viên này đã có trong nhóm', 'error');
        setMembers([...members, { ...student, role: 'MEMBER' }]);
        setIsAddMemberOpen(false);
        setMemberSearch('');
        showLiquidToast(`Đã thêm ${student.name} vào nhóm`, 'success');
    };

    const renderIssues = () => {
        const filteredIssues = issues.filter(t => {
            if (taskFilter === 'ALL') return true;
            if (taskFilter === 'MINE') return t.assigneeUsername === CURRENT_USER_NAME;
            return t.assigneeUsername === taskFilter;
        });

        return (
            <div className="space-y-6">
                <div className="flex flex-col md:flex-row justify-between gap-4 items-center bg-white/5 p-4 rounded-2xl border border-white/10">
                    <div className="flex items-center gap-2 w-full md:w-auto overflow-x-auto custom-scrollbar pb-1 md:pb-0">
                        <button onClick={() => setTaskFilter('ALL')} className={`px-4 py-2 rounded-xl text-sm font-bold transition-all whitespace-nowrap ${taskFilter === 'ALL' ? 'bg-blue-600 text-white' : 'bg-black/20 text-slate-400 hover:text-white hover:bg-white/10'}`}>Tất cả</button>
                        {!IS_LECTURER && (
                            <button onClick={() => setTaskFilter('MINE')} className={`px-4 py-2 rounded-xl text-sm font-bold transition-all whitespace-nowrap ${taskFilter === 'MINE' ? 'bg-blue-600 text-white' : 'bg-black/20 text-slate-400 hover:text-white hover:bg-white/10'}`}>Việc của tôi</button>
                        )}
                        <div className="h-6 w-px bg-white/10 mx-1"></div>
                        <select value={taskFilter !== 'ALL' && taskFilter !== 'MINE' ? taskFilter : ''} onChange={(e) => setTaskFilter(e.target.value)} className="bg-black/40 border border-white/10 rounded-xl px-4 py-2 text-sm text-slate-300 outline-none focus:border-blue-500 cursor-pointer">
                            <option value="" disabled>Lọc theo thành viên</option>
                            {members.map(m => <option key={m.id} value={m.name} className="text-black">{m.name}</option>)}
                        </select>
                    </div>

                    <div className="w-full md:w-auto">
                        {IS_LEADER && !IS_LECTURER && (
                            <button onClick={() => setIsTaskModalOpen(true)} className="w-full md:w-auto bg-blue-600 hover:bg-blue-500 text-white px-5 py-2.5 rounded-xl font-bold flex justify-center items-center gap-2 transition-all shadow-lg shadow-blue-500/20">
                                <Plus size={18} /> Tạo Công Việc
                            </button>
                        )}
                    </div>
                </div>

                {filteredIssues.length === 0 ? (
                    <div className="text-center py-20 bg-white/5 border border-white/10 rounded-3xl text-slate-500">Chưa có công việc nào.</div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                        {filteredIssues.map(issue => {
                            const canEditStatus = !IS_LECTURER && (IS_LEADER || issue.assigneeUsername === CURRENT_USER_NAME);
                            return (
                                <motion.div key={issue.issueId} initial={{opacity:0, scale:0.95}} animate={{opacity:1, scale:1}} className="bg-black/40 border border-white/10 p-5 rounded-3xl hover:border-blue-500/50 transition-all flex flex-col h-full shadow-lg">
                                    <div className="flex justify-between items-start mb-4">
                                        <span className="text-xs font-mono font-bold text-blue-400 bg-blue-500/10 px-2.5 py-1 rounded-lg border border-blue-500/20">{issue.issueCode}</span>
                                        <select
                                            disabled={!canEditStatus}
                                            value={issue.status}
                                            onChange={(e) => handleStatusChange(issue.issueId, e.target.value)}
                                            className={`text-xs font-black px-3 py-1.5 rounded-xl outline-none transition-colors ${
                                                issue.status === 'DONE' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' :
                                                    issue.status === 'IN_PROGRESS' ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30' :
                                                        'bg-slate-700 text-slate-300 border border-slate-600'
                                            } ${canEditStatus ? 'cursor-pointer hover:brightness-125' : 'opacity-60 cursor-not-allowed'}`}
                                        >
                                            <option value="TODO" className="text-black">CHƯA LÀM</option>
                                            <option value="IN_PROGRESS" className="text-black">ĐANG LÀM</option>
                                            <option value="DONE" className="text-black">HOÀN THÀNH</option>
                                            <option value="CANCELLED" className="text-black">ĐÃ HỦY</option>
                                        </select>
                                    </div>
                                    {/* DA SUA LOI {title} THANH {issue.title} O DAY */}
                                    <h4 className="text-white font-bold text-lg mb-6 leading-snug flex-1">{issue.title}</h4>
                                    <div className="flex items-center justify-between mt-auto pt-4 border-t border-white/10">
                                        <div className="flex items-center gap-2">
                                            <div className="w-7 h-7 rounded-full bg-linear-to-br from-purple-500 to-pink-500 flex items-center justify-center text-xs font-black text-white shadow-inner">
                                                {issue.assigneeUsername.charAt(0).toUpperCase()}
                                            </div>
                                            <span className="text-sm font-medium text-slate-300">{issue.assigneeUsername}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <span className="flex items-center" title={issue.issueType}>
                                                {issue.issueType === 'BUG' ? <AlertCircle size={16} className="text-red-400" /> :
                                                    issue.issueType === 'EPIC' ? <CircleDashed size={16} className="text-purple-400" /> :
                                                        <CheckCircle size={16} className="text-blue-400" />}
                                            </span>
                                            <span className="text-[10px] px-2 py-0.5 rounded font-bold uppercase bg-slate-700 text-slate-400">{issue.issueType}</span>
                                        </div>
                                    </div>
                                </motion.div>
                            );
                        })}
                    </div>
                )}

                <AnimatePresence>
                    {isTaskModalOpen && (
                        <>
                            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setIsTaskModalOpen(false)} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100" />
                            <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-[#0f172a] border border-white/10 rounded-3xl p-8 z-101 shadow-2xl">
                                <h2 className="text-2xl font-black text-white mb-6 uppercase">Tạo Công Việc</h2>
                                <div className="space-y-4">
                                    <div>
                                        <label className="text-xs font-bold text-slate-400 uppercase mb-2 block">Tên công việc</label>
                                        <input type="text" value={newTask.title} onChange={e => setNewTask({...newTask, title: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500" placeholder="Nhập tên..." />
                                    </div>
                                    <div>
                                        <label className="text-xs font-bold text-slate-400 uppercase mb-2 block">Loại</label>
                                        <select value={newTask.issueType} onChange={e => setNewTask({...newTask, issueType: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500 cursor-pointer">
                                            <option value="EPIC" className="text-black">Epic</option>
                                            <option value="STORY" className="text-black">Story</option>
                                            <option value="TASK" className="text-black">Task</option>
                                            <option value="BUG" className="text-black">Bug</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="text-xs font-bold text-slate-400 uppercase mb-2 block">Người thực hiện</label>
                                        <select value={newTask.assigneeUsername} onChange={e => setNewTask({...newTask, assigneeUsername: e.target.value})} className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white outline-none focus:border-blue-500 cursor-pointer">
                                            <option value="" disabled>-- Chọn người làm --</option>
                                            {members.map(m => <option key={m.id} value={m.name} className="text-black">{m.name}</option>)}
                                        </select>
                                    </div>
                                    <div className="flex gap-3 mt-6">
                                        <button onClick={() => setIsTaskModalOpen(false)} className="flex-1 bg-white/5 hover:bg-white/10 text-white py-3 rounded-xl font-bold">Hủy</button>
                                        <button onClick={handleCreateTask} className="flex-1 bg-blue-600 hover:bg-blue-500 text-white py-3 rounded-xl font-bold">Xác Nhận</button>
                                    </div>
                                </div>
                            </motion.div>
                        </>
                    )}
                </AnimatePresence>
            </div>
        );
    };

    const renderMembers = () => (
        <div className="space-y-6 max-w-4xl mx-auto">
            <div className="flex justify-between items-center bg-white/5 p-4 rounded-2xl border border-white/10">
                <h3 className="text-white font-bold flex items-center gap-2"><Users size={20} className="text-blue-400"/> Quản lý Thành viên ({members.length})</h3>
                {IS_LECTURER && (
                    <button onClick={() => setIsAddMemberOpen(true)} className="bg-emerald-600 hover:bg-emerald-500 text-white px-5 py-2.5 rounded-xl font-bold text-sm flex items-center gap-2 transition-all shadow-lg shadow-emerald-500/20">
                        <Plus size={16}/> Thêm Thành Viên
                    </button>
                )}
            </div>

            <div className="grid grid-cols-1 gap-4">
                {members.map(m => (
                    <div key={m.id} className="bg-black/40 border border-white/10 rounded-2xl p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between group hover:border-blue-500/50 transition-all">
                        <div className="flex items-center gap-4 mb-4 sm:mb-0">
                            <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-bold text-xl text-white ${m.role === 'LEADER' ? 'bg-amber-500 shadow-[0_0_15px_rgba(245,158,11,0.3)]' : 'bg-slate-700'}`}>
                                {m.name.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <div className="text-white font-bold text-lg flex items-center gap-2">
                                    {m.name} <span className="text-xs bg-white/10 px-2 py-1 rounded-md text-slate-300 font-mono">{m.code}</span>
                                </div>
                                <div className={`text-xs font-bold uppercase mt-1 ${m.role === 'LEADER' ? 'text-amber-400' : 'text-slate-400'}`}>{m.role}</div>
                            </div>
                        </div>

                        {IS_LECTURER && (
                            <div className="flex items-center gap-2">
                                {m.role !== 'LEADER' && (
                                    <button onClick={() => handleSetLeader(m.id)} className="bg-amber-500/10 hover:bg-amber-500 text-amber-500 hover:text-white px-4 py-2 rounded-xl text-sm font-bold flex items-center gap-2 transition-all">
                                        <Crown size={16}/> Chỉ định Leader
                                    </button>
                                )}
                                <button onClick={() => handleRemoveMember(m.id)} className="bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white px-4 py-2 rounded-xl text-sm font-bold flex items-center gap-2 transition-all">
                                    <Trash2 size={16}/> Xóa
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            <AnimatePresence>
                {isAddMemberOpen && (
                    <>
                        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setIsAddMemberOpen(false)} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100" />
                        <motion.div initial={{ y: 50, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ y: 50, opacity: 0 }} className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-lg bg-[#0f172a] border border-white/10 rounded-3xl p-6 z-101 shadow-2xl">
                            <div className="flex justify-between items-center mb-6">
                                <h3 className="text-xl font-bold text-white uppercase">Thêm Sinh Viên</h3>
                            </div>

                            <div className="relative mb-6">
                                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" size={18}/>
                                <input
                                    type="text"
                                    placeholder="Gõ mã SV (VD: SV-001) hoặc tên..."
                                    value={memberSearch}
                                    onChange={e => setMemberSearch(e.target.value)}
                                    className="w-full bg-black/40 border border-white/10 rounded-2xl py-4 pl-12 pr-4 text-white outline-none focus:border-blue-500 transition-all text-sm"
                                />
                            </div>

                            <div className="max-h-72 overflow-y-auto custom-scrollbar space-y-2 pr-2">
                                {ALL_STUDENTS.filter(s => s.code.toLowerCase().includes(memberSearch.toLowerCase()) || s.name.toLowerCase().includes(memberSearch.toLowerCase())).map(student => (
                                    <div
                                        key={student.id}
                                        onClick={() => handleAddMemberFromList(student)}
                                        className="flex items-center justify-between p-4 rounded-xl bg-white/5 hover:bg-blue-600 border border-transparent cursor-pointer transition-all group"
                                    >
                                        <div className="flex flex-col">
                                            <span className="text-white font-bold">{student.name}</span>
                                            <span className="text-xs text-slate-400 group-hover:text-white/70 font-mono mt-1">{student.code}</span>
                                        </div>
                                        <div className="bg-white/10 p-2 rounded-lg group-hover:bg-white/20">
                                            <Plus size={16} className="text-white"/>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>
        </div>
    );

    if (loading) return <div className="text-center py-20 text-blue-400 animate-pulse font-bold text-xl">Đang tải dữ liệu nhóm...</div>;

    return (
        <div className="w-full max-w-7xl mx-auto space-y-6 flex flex-col h-full relative pb-10">
            <div className="flex items-center gap-4 border-b border-white/10 pb-6">
                <button onClick={() => navigate('/groups')} className="p-2 bg-white/5 hover:bg-white/10 rounded-full text-slate-300 transition-colors"><ChevronLeft size={24} /></button>
                <div>
                    <h1 className="text-2xl md:text-3xl font-black text-white tracking-tight">Hệ thống Quản lý Đồ án</h1>
                    <p className="text-slate-400 text-sm mt-1">Mã nhóm: <span className="font-mono text-blue-400">GR-0{groupId}</span></p>
                </div>
            </div>

            <div className="flex bg-black/20 p-1.5 rounded-2xl border border-white/5 w-fit overflow-x-auto max-w-full">
                <button onClick={() => setActiveTab('tasks')} className={`px-6 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all whitespace-nowrap ${activeTab === 'tasks' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}><ListTodo size={18} /> Công Việc</button>
                <button onClick={() => setActiveTab('members')} className={`px-6 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all whitespace-nowrap ${activeTab === 'members' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}><Users size={18} /> Quản Lý Thành Viên</button>
                <button onClick={() => setActiveTab('report')} className={`px-6 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all whitespace-nowrap ${activeTab === 'report' ? 'bg-emerald-600 text-white' : 'text-slate-400 hover:text-white'}`}><BarChart3 size={18} /> Báo Cáo</button>
            </div>

            <div className="flex-1 mt-6">
                <AnimatePresence mode="wait">
                    <motion.div key={activeTab} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }} transition={{ duration: 0.2 }}>
                        {activeTab === 'tasks' && renderIssues()}
                        {activeTab === 'members' && renderMembers()}
                        {activeTab === 'report' && <div className="text-center py-20 bg-white/5 rounded-3xl border border-white/10 text-slate-500">Nội dung báo cáo...</div>}
                    </motion.div>
                </AnimatePresence>
            </div>
        </div>
    );
};

export default GroupWorkspace;