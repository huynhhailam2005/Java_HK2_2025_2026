import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Plus, LayoutDashboard, Clock, CheckCircle2, AlertCircle, RefreshCw, CloudLightning } from 'lucide-react';
import { issueApi } from '../services/issueApi';
import { jiraApi } from '../services/jiraApi'; // 🔥 Import API Jira
import { showLiquidToast } from '../utils/toast';
import CreateIssueModal from '../components/dashboard/CreateIssueModal';

interface IssueItem {
    id: number;
    title: string;
    issueType: string;
    status: string;
    assignee: { student: { username: string } } | null;
}

const StudentDashboard = () => {
    const [user, setUser] = useState<{ username?: string } | null>(null);
    const [issues, setIssues] = useState<IssueItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    // State cho hiệu ứng quay vòng vòng khi đang Sync
    const [isSyncing, setIsSyncing] = useState(false);

    const MOCK_GROUP_ID = 1;
    const IS_TEAM_LEADER = true;

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) setUser(JSON.parse(storedUser));
        void fetchIssues();
    }, []);

    const fetchIssues = async () => {
        setLoading(true);
        try {
            const res = await issueApi.getIssuesByGroup(MOCK_GROUP_ID);
            const fetchedData = res.data.data as unknown as IssueItem[];
            setIssues(fetchedData || []);
        } catch (error) {
            console.error("Lỗi fetch issues:", error);
            setIssues([
                { id: 101, title: 'Thiết kế Database', issueType: 'TASK', status: 'TODO', assignee: { student: { username: 'lam_sinhvien' } } },
                { id: 102, title: 'Làm API Login', issueType: 'STORY', status: 'IN_PROGRESS', assignee: { student: { username: 'nam_dev' } } },
                { id: 103, title: 'Lỗi văng trang Dashboard', issueType: 'BUG', status: 'DONE', assignee: null },
            ]);
        } finally {
            setLoading(false);
        }
    };

    const handleStatusChange = async (issueId: number, newStatus: string) => {
        try {
            await issueApi.updateIssueStatus(issueId, newStatus);
            showLiquidToast('Đã cập nhật trạng thái!', 'success');
            void fetchIssues();
        } catch (error) {
            console.error("Lỗi update trạng thái:", error);
            showLiquidToast('Lỗi cập nhật trạng thái!', 'error');
        }
    };

    // 🔥 HÀM ĐỒNG BỘ JIRA
    const handleSyncJira = async () => {
        if (!window.confirm("Bạn có chắc chắn muốn đẩy tất cả công việc của nhóm lên Jira không?")) return;

        setIsSyncing(true);
        try {
            await jiraApi.syncIssuesToJira(MOCK_GROUP_ID);
            showLiquidToast('Đồng bộ lên Jira thành công!', 'success');
            void fetchIssues(); // Load lại data cho chắc
        } catch (error: unknown) {
            const err = error as { response?: { data?: { message?: string } } };
            showLiquidToast(err.response?.data?.message || 'Lỗi khi đồng bộ với Jira!', 'error');
        } finally {
            setIsSyncing(false);
        }
    };

    const renderIssueCard = (issue: IssueItem) => (
        <motion.div key={issue.id} layout initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} className="bg-white/5 border border-white/10 rounded-2xl p-4 hover:bg-white/10 transition-colors shadow-sm group">
            <div className="flex justify-between items-start mb-3">
                <div className={`px-2 py-1 rounded text-[10px] font-black uppercase tracking-wider ${issue.issueType === 'BUG' ? 'bg-red-500/20 text-red-400' : issue.issueType === 'EPIC' ? 'bg-purple-500/20 text-purple-400' : 'bg-blue-500/20 text-blue-400'}`}>
                    {issue.issueType}
                </div>
                <span className="text-xs text-slate-500 font-mono">#{issue.id}</span>
            </div>
            <h4 className="text-white font-bold text-sm mb-4 leading-relaxed">{issue.title}</h4>
            <div className="flex items-center justify-between mt-auto">
                <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-full bg-linear-to-br from-slate-700 to-slate-600 flex items-center justify-center text-[10px] font-bold text-white border border-white/10" title={issue.assignee?.student?.username || 'Chưa phân công'}>
                        {issue.assignee ? issue.assignee.student.username.charAt(0).toUpperCase() : '?'}
                    </div>
                    <span className="text-xs text-slate-400 truncate max-w-20">
                        {issue.assignee ? issue.assignee.student.username : 'Chưa giao'}
                    </span>
                </div>

                <select
                    value={issue.status}
                    onChange={(e) => void handleStatusChange(issue.id, e.target.value)}
                    className="bg-black/40 border border-white/10 text-xs text-slate-300 rounded-lg py-1 px-2 outline-none focus:border-blue-500 cursor-pointer"
                >
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="DONE">Done</option>
                </select>
            </div>
        </motion.div>
    );

    return (
        <div className="space-y-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                        <LayoutDashboard className="text-blue-500" /> Không Gian Làm Việc
                    </h1>
                    <p className="text-slate-400 mt-2">Xin chào, <span className="text-blue-400 font-bold">{user?.username}</span>! Theo dõi và cập nhật tiến độ công việc của nhóm tại đây.</p>
                </div>
                <div className="flex flex-wrap gap-3">
                    <button onClick={() => void fetchIssues()} className="p-3 bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl text-slate-300 transition-colors" title="Làm mới">
                        <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
                    </button>

                    {IS_TEAM_LEADER && (
                        <>
                            {/* 🔥 NÚT ĐỒNG BỘ JIRA */}
                            <button
                                onClick={handleSyncJira}
                                disabled={isSyncing}
                                className="bg-indigo-500/20 hover:bg-indigo-500/40 border border-indigo-500/30 text-indigo-300 px-5 py-3 rounded-2xl font-bold transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <CloudLightning className={`w-5 h-5 ${isSyncing ? 'animate-pulse' : ''}`} />
                                {isSyncing ? 'Đang đồng bộ...' : 'Sync Jira'}
                            </button>

                            <button onClick={() => setIsCreateModalOpen(true)} className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-3 rounded-2xl font-bold transition-all shadow-lg shadow-blue-500/25 flex items-center gap-2">
                                <Plus className="w-5 h-5" /> Tạo Công Việc
                            </button>
                        </>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col max-h-175">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5">
                        <AlertCircle className="w-5 h-5 text-slate-400" />
                        <h3 className="font-bold text-slate-300">CẦN LÀM (TODO)</h3>
                        <span className="ml-auto bg-white/10 text-xs py-1 px-3 rounded-full text-white">{issues.filter(i => i.status === 'TODO').length}</span>
                    </div>
                    <div className="flex-1 overflow-y-auto space-y-4 pr-2 custom-scrollbar">
                        {loading ? <p className="text-slate-500 text-sm text-center">Đang tải...</p> :
                            issues.filter(i => i.status === 'TODO').map(renderIssueCard)}
                    </div>
                </div>

                <div className="bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col max-h-175">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5">
                        <Clock className="w-5 h-5 text-blue-400" />
                        <h3 className="font-bold text-blue-400">ĐANG LÀM</h3>
                        <span className="ml-auto bg-blue-500/20 text-blue-400 text-xs py-1 px-3 rounded-full">{issues.filter(i => i.status === 'IN_PROGRESS').length}</span>
                    </div>
                    <div className="flex-1 overflow-y-auto space-y-4 pr-2 custom-scrollbar">
                        {loading ? <p className="text-slate-500 text-sm text-center">Đang tải...</p> :
                            issues.filter(i => i.status === 'IN_PROGRESS').map(renderIssueCard)}
                    </div>
                </div>

                <div className="bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col max-h-175">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5">
                        <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                        <h3 className="font-bold text-emerald-400">ĐÃ XONG</h3>
                        <span className="ml-auto bg-emerald-500/20 text-emerald-400 text-xs py-1 px-3 rounded-full">{issues.filter(i => i.status === 'DONE').length}</span>
                    </div>
                    <div className="flex-1 overflow-y-auto space-y-4 pr-2 custom-scrollbar">
                        {loading ? <p className="text-slate-500 text-sm text-center">Đang tải...</p> :
                            issues.filter(i => i.status === 'DONE').map(renderIssueCard)}
                    </div>
                </div>
            </div>

            <CreateIssueModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                groupId={MOCK_GROUP_ID}
                onSuccess={() => void fetchIssues()}
            />
        </div>
    );
};

export default StudentDashboard;