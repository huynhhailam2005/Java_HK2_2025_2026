import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Users, ChevronLeft, ChevronRight, ChevronDown, CloudUpload, RefreshCw, UserCircle, Plus, GitBranch, GitCommit, Eye, FilePlus, Download, ExternalLink, BarChart3
} from 'lucide-react';
import { showLiquidToast } from '../utils/toast';
import { groupApi } from '../services/groupApi';
import { issueApi } from '../services/issueApi';
import { githubApi } from '../services/githubApi';
import { studentSearchApi } from '../services/studentSearchApi';
import IssueDetailModal from '../components/IssueDetailModal';
import CreateIssueModal from '../components/CreateIssueModal';
import EditIssueModal from '../components/EditIssueModal';
import ProgressReport from './ProgressReport';

interface IssueDto {
    issueId: number;
    issueCode: string;
    title: string;
    description?: string;
    type: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
    status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
    assignedTo: string;
    deadline?: string;
    parentId?: number | null;
}

interface IssueTreeNode extends IssueDto {
    children: IssueTreeNode[];
}

const buildIssueTree = (flatIssues: IssueDto[]): IssueTreeNode[] => {
    if (!flatIssues || flatIssues.length === 0) return [];
    const map: Record<number, IssueTreeNode> = {};
    const roots: IssueTreeNode[] = [];
    flatIssues.forEach(issue => { map[issue.issueId] = { ...issue, children: [] }; });
    flatIssues.forEach(issue => {
        const node = map[issue.issueId];
        if (issue.parentId && map[issue.parentId]) {
            map[issue.parentId].children.push(node);
        } else { roots.push(node); }
    });
    roots.sort((a, b) => a.issueId - b.issueId);
    return roots;
};

const flattenTree = (nodes: IssueTreeNode[], level = 0, expandedNodes = new Set<number>()): Array<IssueTreeNode & { level: number }> => {
    const result: Array<IssueTreeNode & { level: number }> = [];
    nodes.forEach(node => {
        result.push({ ...node, level });
        if (node.children && node.children.length > 0 && expandedNodes.has(node.issueId)) {
            result.push(...flattenTree(node.children, level + 1, expandedNodes));
        }
    });
    return result;
};

const typeConfig: Record<string, { color: string; bg: string; border: string; label: string }> = {
    EPIC:     { color: 'text-purple-300', bg: 'bg-purple-600/20', border: 'border-purple-500/40', label: 'Epic' },
    STORY:    { color: 'text-emerald-300', bg: 'bg-emerald-600/20', border: 'border-emerald-500/40', label: 'Story' },
    TASK:     { color: 'text-blue-300',    bg: 'bg-blue-600/20',    border: 'border-blue-500/40',    label: 'Task' },
    BUG:      { color: 'text-red-300',     bg: 'bg-red-600/20',     border: 'border-red-500/40',     label: 'Bug' },
    SUB_TASK: { color: 'text-orange-300',  bg: 'bg-orange-600/20',  border: 'border-orange-500/40',  label: 'Sub-task' },
};

const statusConfig: Record<string, { color: string; bg: string; dot: string }> = {
    TODO:        { color: 'text-slate-400', bg: 'bg-slate-600/20', dot: 'bg-slate-400' },
    IN_PROGRESS: { color: 'text-blue-400',  bg: 'bg-blue-600/20',  dot: 'bg-blue-400' },
    DONE:        { color: 'text-emerald-400', bg: 'bg-emerald-600/20', dot: 'bg-emerald-400' },
    CANCELLED:   { color: 'text-red-400',   bg: 'bg-red-600/20',   dot: 'bg-red-400' },
};

const IssueRow = ({ issue, level, isExpanded, hasChildren, onToggle, onPushJira, onViewDetail, onEditIssue, onAddChild, isTeamLeader, allIssuesMap }: any) => {
    const type = typeConfig[issue.type] || typeConfig.TASK;
    const status = statusConfig[issue.status] || statusConfig.TODO;

    const parentSyncedToJira = !issue.parentId ||
        (allIssuesMap && allIssuesMap[issue.parentId] && allIssuesMap[issue.parentId].issueCode);
    const canPushToJira = !issue.issueCode && (!issue.parentId || parentSyncedToJira);

    return (
        <tr className={`group transition-all duration-150 ${
            level === 0
                ? 'bg-white/[0.02] hover:bg-white/[0.04]'
                : 'hover:bg-white/[0.03]'
        }`}>
            <td className="py-4 pr-4 pl-0" style={{ paddingLeft: `${level * 28 + 16}px` }}>
                <div className="flex items-center gap-2">
                    {/* Tree connector + toggle */}
                    <div className="flex items-center shrink-0">
                        {hasChildren ? (
                            <button onClick={() => onToggle(issue.issueId)}
                                    className="w-6 h-6 flex items-center justify-center rounded-lg bg-white/5 hover:bg-white/20 text-slate-500 hover:text-white transition-all">
                                {isExpanded ? <ChevronDown size={13} /> : <ChevronRight size={13} />}
                            </button>
                        ) : (
                            <span className="w-6 h-6 flex items-center justify-center">
                                <span className="w-1.5 h-1.5 rounded-full bg-slate-600"/>
                            </span>
                        )}
                    </div>
                    {/* Issue Code */}
                    <span className="font-mono text-blue-400 font-bold text-[11px] tracking-tight">
                        {issue.issueCode || (
                            <span className="text-slate-600 italic text-[10px]">Chưa đồng bộ</span>
                        )}
                    </span>
                </div>
            </td>
            <td className="py-4 pr-4">
                <div className="flex items-center gap-2.5 min-w-0">
                    {/* Type badge */}
                    <span className={`shrink-0 px-2 py-0.5 rounded-md text-[9px] font-black uppercase tracking-wider border ${type.bg} ${type.color} ${type.border}`}>
                        {type.label}
                    </span>
                    {/* Title */}
                    <span className={`text-sm truncate ${
                        issue.status === 'DONE' ? 'text-slate-500 line-through' :
                        issue.status === 'CANCELLED' ? 'text-slate-600 line-through' :
                        'text-white font-semibold'
                    }`}>
                        {issue.title}
                    </span>
                </div>
            </td>
            <td className="py-4 pr-4">
                <div className="flex items-center gap-2">
                    <UserCircle size={14} className={issue.assignedTo === 'Chưa gán' ? 'text-slate-600' : 'text-blue-400'}/>
                    <span className={`text-xs ${issue.assignedTo === 'Chưa gán' ? 'text-slate-500 italic' : 'text-slate-300'}`}>
                        {issue.assignedTo}
                    </span>
                </div>
            </td>
            <td className="py-4 pr-4">
                <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold ${status.bg} ${status.color}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${status.dot}`}/>
                    {issue.status === 'IN_PROGRESS' ? 'In Progress' :
                     issue.status === 'TODO' ? 'To Do' :
                     issue.status === 'DONE' ? 'Done' :
                     issue.status === 'CANCELLED' ? 'Cancelled' : issue.status}
                </div>
            </td>
            <td className="py-4 text-center">
                <div className="flex items-center justify-center gap-1.5 opacity-0 group-hover:opacity-100 transition-all duration-200">
                    {isTeamLeader && issue.type !== 'SUB_TASK' && (
                        <button onClick={() => onAddChild(issue)}
                                className="p-2 bg-emerald-600/20 text-emerald-400 rounded-xl hover:bg-emerald-600 hover:text-white transition-all"
                                title="Tạo Issue con">
                            <Plus size={14}/>
                        </button>
                    )}
                    {isTeamLeader && (
                        <button onClick={() => onEditIssue(issue)}
                                className="p-2 bg-amber-600/20 text-amber-400 rounded-xl hover:bg-amber-600 hover:text-white transition-all"
                                title="Sửa Issue">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                        </button>
                    )}
                    {canPushToJira ? (
                        <button onClick={() => onPushJira(issue.issueId)}
                                className="p-2 bg-emerald-600/20 text-emerald-400 rounded-xl hover:bg-emerald-600 hover:text-white transition-all"
                                title="Đẩy lên Jira">
                            <CloudUpload size={14}/>
                        </button>
                    ) : issue.parentId && !parentSyncedToJira ? (
                        <button disabled
                                className="p-2 bg-red-600/20 text-red-400 rounded-xl cursor-not-allowed opacity-50"
                                title="Cha chưa được đẩy lên Jira">
                            <CloudUpload size={14}/>
                        </button>
                    ) : null}
                    <button onClick={() => onViewDetail(issue)}
                            className="p-2 bg-blue-600/20 text-blue-400 rounded-xl hover:bg-blue-600 hover:text-white transition-all"
                            title="Xem chi tiết">
                        <Eye size={14}/>
                    </button>
                </div>
            </td>
        </tr>
    );
};

const MyTasksTab = ({ issues, currentUser, onViewDetail, onEditIssue, isTeamLeader, onRefresh }: any) => {
    const [submittingId, setSubmittingId] = useState<number | null>(null);

    const myIssues = issues.filter((i: any) => i.assignedTo === currentUser?.username);

    const todo = myIssues.filter((i: any) => i.status === 'TODO').length;
    const inProgress = myIssues.filter((i: any) => i.status === 'IN_PROGRESS').length;
    const done = myIssues.filter((i: any) => i.status === 'DONE').length;
    const cancelled = myIssues.filter((i: any) => i.status === 'CANCELLED').length;

    const handleSubmit = async (issueId: number) => {
        try {
            const Swal = (await import('sweetalert2')).default;
            const result = await Swal.fire({
                title: 'Nộp bài',
                html: '<textarea id="submit-note" placeholder="Nhập nội dung nộp bài..." style="width:100%;background:#1e293b;color:#fff;border:1px solid rgba(255,255,255,0.1);border-radius:8px;padding:10px;font-size:14px;outline:none;resize:vertical;min-height:80px;"></textarea>',
                showCancelButton: true,
                confirmButtonColor: '#10b981', cancelButtonColor: '#6b7280',
                confirmButtonText: 'Nộp', cancelButtonText: 'Hủy',
                background: '#0f172a', color: '#fff',
                preConfirm: () => {
                    const note = (document.getElementById('submit-note') as HTMLTextAreaElement)?.value || '';
                    return note;
                }
            });
            if (!result.isConfirmed) return;

            setSubmittingId(issueId);
            const res = await issueApi.submitIssue(issueId, result.value || '');
            if (res.data.success) {
                showLiquidToast('✅ Nộp bài thành công! Issue đã hoàn thành.', 'success');
                onRefresh();
            } else showLiquidToast(res.data.message || 'Lỗi nộp bài', 'error');
        } catch (err: any) {
            showLiquidToast(err.response?.data?.message || err.message || 'Lỗi nộp bài', 'error');
        } finally { setSubmittingId(null); }
    };

    const st = (s: string) => ({
        'DONE': { text: 'Done', color: 'text-emerald-400', bg: 'bg-emerald-600/20', dot: 'bg-emerald-400' },
        'IN_PROGRESS': { text: 'In Progress', color: 'text-blue-400', bg: 'bg-blue-600/20', dot: 'bg-blue-400' },
        'CANCELLED': { text: 'Cancelled', color: 'text-red-400', bg: 'bg-red-600/20', dot: 'bg-red-400' },
    }[s] || { text: 'To Do', color: 'text-slate-400', bg: 'bg-slate-600/20', dot: 'bg-slate-400' });

    const tp = (t: string) => ({
        'EPIC': { text: 'Epic', c: 'text-purple-300', bg: 'bg-purple-600/20', bd: 'border-purple-500/40' },
        'STORY': { text: 'Story', c: 'text-emerald-300', bg: 'bg-emerald-600/20', bd: 'border-emerald-500/40' },
        'BUG': { text: 'Bug', c: 'text-red-300', bg: 'bg-red-600/20', bd: 'border-red-500/40' },
        'SUB_TASK': { text: 'Sub-task', c: 'text-orange-300', bg: 'bg-orange-600/20', bd: 'border-orange-500/40' },
    }[t] || { text: 'Task', c: 'text-blue-300', bg: 'bg-blue-600/20', bd: 'border-blue-500/40' });

    return (
        <div className="space-y-5">
            <div className="flex items-center gap-4 flex-wrap">
                <div className="bg-blue-600/10 border border-blue-500/30 rounded-2xl p-4 text-center min-w-[100px]">
                    <p className="text-slate-500 text-[9px] font-black uppercase mb-1">Tổng</p>
                    <p className="text-2xl font-black text-white">{myIssues.length}</p>
                </div>
                <div className="bg-slate-600/10 border border-slate-500/30 rounded-2xl p-4 text-center min-w-[100px]">
                    <p className="text-slate-500 text-[9px] font-black uppercase mb-1">To Do</p>
                    <p className="text-2xl font-black text-slate-400">{todo}</p>
                </div>
                <div className="bg-blue-600/10 border border-blue-500/30 rounded-2xl p-4 text-center min-w-[100px]">
                    <p className="text-slate-500 text-[9px] font-black uppercase mb-1">In Progress</p>
                    <p className="text-2xl font-black text-blue-400">{inProgress}</p>
                </div>
                <div className="bg-emerald-600/10 border border-emerald-500/30 rounded-2xl p-4 text-center min-w-[100px]">
                    <p className="text-slate-500 text-[9px] font-black uppercase mb-1">Done</p>
                    <p className="text-2xl font-black text-emerald-400">{done}</p>
                </div>
                {cancelled > 0 && (
                    <div className="bg-red-600/10 border border-red-500/30 rounded-2xl p-4 text-center min-w-[100px]">
                        <p className="text-slate-500 text-[9px] font-black uppercase mb-1">Cancelled</p>
                        <p className="text-2xl font-black text-red-400">{cancelled}</p>
                    </div>
                )}
            </div>

            {myIssues.length === 0 ? (
                <div className="bg-white/[0.02] border border-white/10 rounded-3xl p-16 text-center">
                    <p className="text-slate-500 text-lg font-black">Chưa có Issue nào</p>
                    <p className="text-slate-600 text-sm mt-2">Bạn chưa được gán Issue nào.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {myIssues.map((issue: any) => {
                        const s = st(issue.status);
                        const t = tp(issue.type);
                        const locked = issue.status === 'DONE' || issue.status === 'CANCELLED';
                        return (
                            <motion.div key={issue.issueId} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
                                className={`bg-[#0f172a]/80 border rounded-2xl p-5 transition-all ${locked ? 'border-emerald-500/20 opacity-80' : 'border-white/10 hover:border-blue-500/30'}`}>
                                <div className="flex items-center justify-between mb-3">
                                    <div className="flex items-center gap-2">
                                        <span className={`px-2 py-0.5 rounded-md text-[9px] font-black uppercase tracking-wider border ${t.bg} ${t.c} ${t.bd}`}>{t.text}</span>
                                        {issue.issueCode && <span className="font-mono text-blue-400 font-bold text-[10px]">{issue.issueCode}</span>}
                                    </div>
                                    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold ${s.bg} ${s.color}`}>
                                        <span className={`w-1.5 h-1.5 rounded-full ${s.dot}`}/>{s.text}
                                        {locked && <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>}
                                    </div>
                                </div>
                                <h4 className={`text-sm font-bold mb-2 ${locked ? 'text-slate-400' : 'text-white'}`}>{issue.title}</h4>
                                {issue.description && <p className="text-xs text-slate-500 line-clamp-2 mb-3">{issue.description}</p>}
                                {issue.deadline && (
                                    <div className="flex items-center gap-1.5 mb-3">
                                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-slate-500"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                                        <span className="text-xs text-slate-500 font-mono">Hạn: {new Date(issue.deadline).toLocaleDateString('vi-VN')}</span>
                                    </div>
                                )}
                                <div className="border-t border-white/5 my-3"/>
                                <div className="flex items-center justify-end gap-2">
                                    {!locked && (
                                        <button onClick={() => handleSubmit(issue.issueId)} disabled={submittingId === issue.issueId}
                                            className="flex items-center gap-1.5 px-4 py-2 bg-gradient-to-r from-emerald-600 to-emerald-500 text-white rounded-xl text-[10px] font-black uppercase tracking-wider hover:from-emerald-500 hover:to-emerald-400 transition-all shadow-lg shadow-emerald-600/20 disabled:opacity-50">
                                            {submittingId === issue.issueId ? (
                                                <><div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"/> Đang nộp...</>
                                            ) : (
                                                <><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6 9 17l-5-5"/></svg> Nộp bài</>
                                            )}
                                        </button>
                                    )}
                                    {!locked && (isTeamLeader || issue.assignedTo === currentUser?.username) && (
                                        <button onClick={() => onEditIssue(issue)} className="p-2 bg-amber-600/20 text-amber-400 rounded-xl hover:bg-amber-600 hover:text-white transition-all" title="Sửa Issue">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/></svg>
                                        </button>
                                    )}
                                    <button onClick={() => onViewDetail(issue)} className="p-2 bg-blue-600/20 text-blue-400 rounded-xl hover:bg-blue-600 hover:text-white transition-all" title="Xem chi tiết"><Eye size={14}/></button>
                                </div>
                            </motion.div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};


const GroupWorkspace = () => {
    const { groupId } = useParams();
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<'tasks' | 'my-tasks' | 'members' | 'commits' | 'report'>('tasks');
    const [loading, setLoading] = useState(true);
    const [groupData, setGroupData] = useState<any>(null);
    const [treeIssues, setTreeIssues] = useState<IssueTreeNode[]>([]);
    const [flatIssues, setFlatIssues] = useState<IssueDto[]>([]);
    const [expandedNodes, setExpandedNodes] = useState<Set<number>>(new Set());
    const [commitsData, setCommitsData] = useState<any>(null);
    const [commitsLoading, setCommitsLoading] = useState(false);
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [selectedIssue, setSelectedIssue] = useState<any>(null);
    const [commitHistoryData, setCommitHistoryData] = useState<any>(null);
    const [commitHistoryLoading, setCommitHistoryLoading] = useState(false);
    const [historyDays, setHistoryDays] = useState(30);
    const [memberMappings, setMemberMappings] = useState<any[]>([]);
    const [memberMappingsLoading, setMemberMappingsLoading] = useState(false);
    const [selectedMemberFilter, setSelectedMemberFilter] = useState<string>('all');
    const [isMemberCommitsModalOpen, setIsMemberCommitsModalOpen] = useState(false);
    const [selectedMemberCommits, setSelectedMemberCommits] = useState<any>(null);
    const [selectedMemberDetails, setSelectedMemberDetails] = useState<any>(null);
    const [isAddMemberModalOpen, setIsAddMemberModalOpen] = useState(false);
    const [isStudentDetailModalOpen, setIsStudentDetailModalOpen] = useState(false);
    const [selectedMember, setSelectedMember] = useState<any>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<any[]>([]);
    const [searching, setSearching] = useState(false);
    const [syncingJira, setSyncingJira] = useState(false);
    const [isCreateIssueModalOpen, setIsCreateIssueModalOpen] = useState(false);
    const [createParentId, setCreateParentId] = useState<number | null>(null);
    const [createIssueType, setCreateIssueType] = useState<'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK'>('TASK');
    const [isEditIssueModalOpen, setIsEditIssueModalOpen] = useState(false);
    const [editIssue, setEditIssue] = useState<any>(null);
    const [hideEditAssignee, setHideEditAssignee] = useState(false);
    const [currentUser] = useState(() => {
        const data = localStorage.getItem('user');
        try { return data ? JSON.parse(data) : null; }
        catch { return null; }
    });

    useEffect(() => { if (groupId) loadData(); }, [groupId]);

    const loadData = async () => {
        setLoading(true);
        try {
            const gRes = await groupApi.getById(parseInt(groupId!));
            const gResData = gRes.data as { success: boolean; data: any };
            if (gResData.success) {
                setGroupData(gResData.data);
                const iRes = await issueApi.getIssuesByGroup(gResData.data.id);
                const rawIssues = iRes.data.data || [];
                setFlatIssues(rawIssues);
                const tree = buildIssueTree(rawIssues);
                setTreeIssues(tree);
                const allNodeIds = new Set<number>();
                const collectIds = (nodes: IssueTreeNode[]) => nodes.forEach(n => {
                    allNodeIds.add(n.issueId);
                    if (n.children) collectIds(n.children);
                });
                collectIds(tree);
                setExpandedNodes(allNodeIds);
            }
        } catch (error) { showLiquidToast('Lỗi tải dữ liệu', 'error'); }
        finally { setLoading(false); }
    };

    const loadCommits = async () => {
        setCommitsLoading(true);
        try {
            const res = await githubApi.getTeamCommits(parseInt(groupId!));
            if (res.data.success) setCommitsData(res.data.data);
        } catch (error) { showLiquidToast('Lỗi tải dữ liệu commits', 'error'); }
        finally { setCommitsLoading(false); }
    };

    const loadCommitHistory = async () => {
        setCommitHistoryLoading(true);
        try {
            const res = await githubApi.getTeamCommitHistory(parseInt(groupId!), historyDays);
            if (res.data.success) setCommitHistoryData(res.data.data);
        } catch (error) { showLiquidToast('Lỗi tải lịch sử commits', 'error'); }
        finally { setCommitHistoryLoading(false); }
    };

    const loadMemberMappings = async () => {
        setMemberMappingsLoading(true);
        try {
            const res = await githubApi.getMemberMappings(parseInt(groupId!));
            if (res.data.success) setMemberMappings(res.data.data || []);
        } catch (error) {
            console.error('Lỗi tải member mappings:', error);
        } finally {
            setMemberMappingsLoading(false);
        }
    };

    const loadMemberCommits = async (memberId: number, memberName: string) => {
        try {
            const res = await githubApi.getMemberCommits(parseInt(groupId!), memberId);
            setSelectedMemberCommits(res.data.data);
            setSelectedMemberDetails({ id: memberId, name: memberName });
            setIsMemberCommitsModalOpen(true);
        } catch (error: any) {
            const msg = error.response?.data?.message || 'Lỗi tải commit cá nhân';
            showLiquidToast(msg, 'error');
        }
    };

    useEffect(() => { if (activeTab === 'commits') loadCommits(); }, [activeTab, groupId]);

    useEffect(() => { if (activeTab === 'commits') loadCommitHistory(); }, [activeTab, groupId, historyDays]);

    useEffect(() => { if (activeTab === 'commits') loadMemberMappings(); }, [activeTab, groupId]);

    const allIssuesMap: Record<number, IssueDto> = {};
    flatIssues.forEach(issue => {
        allIssuesMap[issue.issueId] = issue;
    });

    const handleToggleNode = (id: number) => {
        const newSet = new Set(expandedNodes);
        if (newSet.has(id)) newSet.delete(id); else newSet.add(id);
        setExpandedNodes(newSet);
    };

    const handleSyncFromJira = async () => {
        if (!groupData?.jiraProjectKey) {
            showLiquidToast('Nhóm chưa cấu hình Jira Project Key!', 'error');
            return;
        }

        setSyncingJira(true);
        try {
            const res = await issueApi.syncFromJira(groupData.id, groupData.jiraProjectKey);
            if (res.data.success) {
                const msg = res.data.message || 'Đồng bộ thành công!';
                showLiquidToast('✅ ' + msg, 'success');
                loadData();
            } else {
                showLiquidToast(res.data.message || 'Lỗi đồng bộ từ Jira', 'error');
            }
        } catch (err: any) {
            const errorMsg = err.response?.data?.message || err.message || 'Lỗi đồng bộ Jira';
            showLiquidToast(errorMsg, 'error');
        } finally {
            setSyncingJira(false);
        }
    };

    const handlePushJira = async (id: number) => {
        try {
            const issue = flatIssues.find(i => i.issueId === id);
            if (!issue) {
                showLiquidToast('Không tìm thấy Issue!', 'error');
                return;
            }

            // Check if subtask parent is synced
            if (issue.type === 'SUB_TASK' && issue.parentId) {
                const parent = allIssuesMap[issue.parentId];
                if (parent && !parent.issueCode) {
                    showLiquidToast(`Vui lòng push Issue cha "${parent.title}" lên Jira trước!`, 'error');
                    return;
                }
            }

            showLiquidToast('Đang đẩy lên Jira...', 'success');
            const res = await issueApi.pushToJira(id);
            if (res.data.success) {
                showLiquidToast('Sync thành công! (Cha tự động được push cùng nếu cần)', 'success');
                loadData();
            }
        } catch (err: any) {
            const errorMsg = err.response?.data?.message || err.message || 'Lỗi Push Jira';
            showLiquidToast(errorMsg, 'error');
        }
    };

    const members = groupData?.groupMembers || groupData?.members || [];
    const teamLeader = members.find((m: any) =>
        m.memberRole === 'TEAM_LEADER' || m.role === 'TEAM_LEADER'
    );
    const isTeamLeader = teamLeader && currentUser && (
        teamLeader.id === currentUser.id ||
        teamLeader.studentId === currentUser.id ||
        teamLeader.student?.id === currentUser.id
    );

    const isLecturer = currentUser?.role === 'LECTURER';
    const isAdmin = currentUser?.role === 'ADMIN';
    const canManageMembers = isLecturer || isAdmin;

    const handleOpenCreateIssue = () => {
        setCreateParentId(null);
        setCreateIssueType('TASK');
        setIsCreateIssueModalOpen(true);
    };

    const handleEditIssue = (issue: any, fromMyTasks?: boolean) => {
        setEditIssue(issue);
        setHideEditAssignee(!!fromMyTasks);
        setIsEditIssueModalOpen(true);
    };

    const handleOpenCreateChildIssue = (parentIssue: any) => {
        if (parentIssue.type === 'SUB_TASK') return; // Sub-task không thể làm cha

        let childType: 'EPIC' | 'TASK' | 'STORY' | 'BUG' | 'SUB_TASK';
        if (parentIssue.type === 'EPIC') {
            childType = 'TASK';
        } else {
            childType = 'SUB_TASK';
        }
        setCreateParentId(parentIssue.issueId);
        setCreateIssueType(childType);
        setIsCreateIssueModalOpen(true);
    };

    const handleOpenAddMember = () => {
        setSearchQuery('');
        setSearchResults([]);
        setIsAddMemberModalOpen(true);
    };

    const handleSearchStudent = async (q: string) => {
        setSearchQuery(q);
        if (q.trim().length < 2) {
            setSearchResults([]);
            return;
        }
        setSearching(true);
        try {
            const res = await studentSearchApi.search(q);
            if (res.data.success) {
                const memberStudentIds = new Set(members.map((m: any) => m.studentId || m.student?.id));
                const filtered = (res.data.data || []).filter((s: any) => !memberStudentIds.has(s.id));
                setSearchResults(filtered);
            }
        } catch {
            setSearchResults([]);
        } finally {
            setSearching(false);
        }
    };

    const handleAddMember = async (studentId: number) => {
        try {
            const res = await groupApi.addMember(parseInt(groupId!), studentId);
            if (res.data.success) {
                showLiquidToast('Thêm thành viên thành công!', 'success');
                setIsAddMemberModalOpen(false);
                loadData();
            } else {
                showLiquidToast(res.data.message || 'Thêm thất bại', 'error');
            }
        } catch {
            showLiquidToast('Lỗi khi thêm thành viên', 'error');
        }
    };

    const handleRemoveMember = async (member: any) => {
        const memberId = member.groupMemberId || member.id;
        const name = member.username || member.studentUsername || 'này';

        // Dùng SweetAlert2 để xác nhận
        try {
            const Swal = (await import('sweetalert2')).default;
            const result = await Swal.fire({
                title: 'Xác nhận xóa?',
                text: `Bạn có chắc muốn xóa ${name} khỏi nhóm?`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ef4444',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'Xóa',
                cancelButtonText: 'Hủy',
                background: '#0f172a',
                color: '#fff',
            });

            if (!result.isConfirmed) return;

            const res = await groupApi.removeMember(parseInt(groupId!), memberId);
            if (res.data.success) {
                showLiquidToast('Xóa thành viên thành công!', 'success');
                loadData();
            } else {
                showLiquidToast(res.data.message || 'Xóa thất bại', 'error');
            }
        } catch {
            showLiquidToast('Lỗi khi xóa thành viên', 'error');
        }
    };

    const handleAssignTeamLeader = async (member: any) => {
        const name = member.username || 'sinh viên';
        const studentId = member.id; // Student ID

        try {
            const Swal = (await import('sweetalert2')).default;

            if (teamLeader) {
                // Đã có nhóm trưởng -> hỏi đổi
                const result = await Swal.fire({
                    title: 'Đổi nhóm trưởng?',
                    text: `Nhóm đã có nhóm trưởng. Bạn có muốn đổi sang ${name}?`,
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#f59e0b',
                    cancelButtonColor: '#6b7280',
                    confirmButtonText: 'Đổi',
                    cancelButtonText: 'Hủy',
                    background: '#0f172a',
                    color: '#fff',
                });

                if (!result.isConfirmed) return;

                const res = await groupApi.changeTeamLeader(parseInt(groupId!), studentId);
                if (res.data.success) {
                    showLiquidToast(`Đã đổi nhóm trưởng sang ${name}!`, 'success');
                    loadData();
                } else {
                    showLiquidToast(res.data.message || 'Đổi thất bại', 'error');
                }
            } else {
                const result = await Swal.fire({
                    title: 'Chỉ định nhóm trưởng?',
                    text: `Bạn có muốn chỉ định ${name} làm nhóm trưởng?`,
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#f59e0b',
                    cancelButtonColor: '#6b7280',
                    confirmButtonText: 'Chỉ định',
                    cancelButtonText: 'Hủy',
                    background: '#0f172a',
                    color: '#fff',
                });

                if (!result.isConfirmed) return;

                const res = await groupApi.assignTeamLeader(parseInt(groupId!), studentId);
                if (res.data.success) {
                    showLiquidToast(`Đã chỉ định ${name} làm nhóm trưởng!`, 'success');
                    loadData();
                } else {
                    showLiquidToast(res.data.message || 'Chỉ định thất bại', 'error');
                }
            }
        } catch {
            showLiquidToast('Lỗi khi thao tác', 'error');
        }
    };

    const handleRemoveTeamLeader = async () => {
        if (!teamLeader) return;
        try {
            const Swal = (await import('sweetalert2')).default;
            const result = await Swal.fire({
                title: 'Xóa nhóm trưởng?',
                text: 'Nhóm sẽ không có nhóm trưởng. Bạn có chắc?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#ef4444',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'Xóa',
                cancelButtonText: 'Hủy',
                background: '#0f172a',
                color: '#fff',
            });

            if (!result.isConfirmed) return;

            const res = await groupApi.removeTeamLeader(parseInt(groupId!));
            if (res.data.success) {
                showLiquidToast('Đã xóa nhóm trưởng!', 'success');
                loadData();
            } else {
                showLiquidToast(res.data.message || 'Xóa thất bại', 'error');
            }
        } catch {
            showLiquidToast('Lỗi khi xóa nhóm trưởng', 'error');
        }
    };

    const handleViewMemberDetail = (member: any) => {
        setSelectedMember(member);
        setIsStudentDetailModalOpen(true);
    };

    const flattenedIssues = flattenTree(treeIssues, 0, expandedNodes);

    if (loading) return <div className="py-40 text-center text-blue-500 font-black animate-pulse uppercase text-xs">Đang tải Workspace...</div>;

    return (
        <div className="w-full space-y-8 pb-20 animate-in fade-in duration-500">
            <div className="flex justify-between items-center border-b border-white/10 pb-6">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate(-1)} className="p-2 bg-white/5 rounded-xl"><ChevronLeft /></button>
                    <h1 className="text-3xl font-black text-white tracking-tighter">{groupData?.groupName}</h1>
                </div>
                <button onClick={loadData} className="p-2.5 bg-white/5 text-slate-400 rounded-xl hover:text-white"><RefreshCw size={20}/></button>
            </div>

            {/* Tab Menu */}
            <div className="flex bg-black/20 p-1.5 rounded-2xl border border-white/5 w-fit">
                <button onClick={() => setActiveTab('tasks')} className={`px-6 py-2.5 rounded-xl text-xs font-black uppercase transition-all ${activeTab === 'tasks' ? 'bg-blue-600 text-white' : 'text-slate-500'}`}>Tổng quan</button>
                {!isLecturer && (
                    <button onClick={() => setActiveTab('my-tasks')} className={`px-6 py-2.5 rounded-xl text-xs font-black uppercase transition-all ${activeTab === 'my-tasks' ? 'bg-blue-600 text-white' : 'text-slate-500'}`}>Việc của tôi</button>
                )}
                <button onClick={() => setActiveTab('members')} className={`px-6 py-2.5 rounded-xl text-xs font-black uppercase transition-all ${activeTab === 'members' ? 'bg-blue-600 text-white' : 'text-slate-500'}`}>Thành Viên</button>
                <button onClick={() => setActiveTab('commits')} className={`px-6 py-2.5 rounded-xl text-xs font-black uppercase transition-all flex items-center gap-2 ${activeTab === 'commits' ? 'bg-blue-600 text-white' : 'text-slate-500'}`}><GitBranch size={14}/> GitHub</button>
                <button onClick={() => setActiveTab('report')} className={`px-6 py-2.5 rounded-xl text-xs font-black uppercase transition-all flex items-center gap-2 ${activeTab === 'report' ? 'bg-blue-600 text-white' : 'text-slate-500'}`}><BarChart3 size={14}/> Báo cáo</button>
            </div>

            <AnimatePresence mode="wait">
                <motion.div key={activeTab} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -10 }}>

                    {/* CASE 1: TASKS */}
                    {activeTab === 'tasks' && (
                        <div className="space-y-5">
                            {/* Header + Nút tạo Issue */}
                            <div className="flex items-center justify-between">
                                {/* Thống kê nhanh */}
                                {flatIssues.length > 0 && (
                                    <div className="flex items-center gap-3 flex-wrap">
                                        {Object.entries(
                                            flatIssues.reduce((acc: Record<string, number>, i: any) => {
                                                acc[i.type] = (acc[i.type] || 0) + 1;
                                                return acc;
                                            }, {} as Record<string, number>)
                                        ).map(([type, count]) => {
                                            const cfg = typeConfig[type] || typeConfig.TASK;
                                            return (
                                                <div key={type} className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl border ${cfg.bg} ${cfg.border}`}>
                                                    <span className={`text-[9px] font-black uppercase tracking-wider ${cfg.color}`}>{cfg.label}</span>
                                                    <span className={`text-xs font-bold ${cfg.color}`}>{count}</span>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                                {flatIssues.length === 0 && <div />}

                                {isTeamLeader && (
                                    <div className="flex items-center gap-2">
                                        <button onClick={handleSyncFromJira} disabled={syncingJira}
                                                className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-black uppercase tracking-wider transition-all shadow-lg shadow-emerald-600/20 shrink-0 disabled:opacity-50 disabled:cursor-not-allowed">
                                            <Download size={16} className={syncingJira ? 'animate-bounce' : ''}/> {syncingJira ? 'Đang đồng bộ...' : 'Đồng bộ Jira'}
                                        </button>
                                        <button onClick={handleOpenCreateIssue}
                                                className="flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-xs font-black uppercase tracking-wider transition-all shadow-lg shadow-blue-600/20 shrink-0">
                                            <FilePlus size={16}/> Tạo Issue
                                        </button>
                                    </div>
                                )}
                            </div>

                            <div className="bg-[#0f172a]/50 border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                    <tr className="border-b border-white/5">
                                        <th className="py-4 px-6 text-slate-500 text-[9px] uppercase font-black tracking-[0.2em] w-[140px]">Mã Issue</th>
                                        <th className="py-4 pr-4 text-slate-500 text-[9px] uppercase font-black tracking-[0.2em]">Tiêu Đề</th>
                                        <th className="py-4 pr-4 text-slate-500 text-[9px] uppercase font-black tracking-[0.2em] w-[130px]">Người Gán</th>
                                        <th className="py-4 pr-4 text-slate-500 text-[9px] uppercase font-black tracking-[0.2em] w-[120px]">Trạng Thái</th>
                                        <th className="py-4 pr-6 text-slate-500 text-[9px] uppercase font-black tracking-[0.2em] w-[80px] text-center">Thao Tác</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-white/[0.04]">
                                    {flattenedIssues.map(issue => (
                                        <IssueRow key={issue.issueId} issue={issue} level={issue.level} isExpanded={expandedNodes.has(issue.issueId)} hasChildren={issue.children.length > 0} onToggle={handleToggleNode} onPushJira={handlePushJira} onViewDetail={(iss: any) => { setSelectedIssue(iss); setIsDetailModalOpen(true); }} onEditIssue={(iss: any) => handleEditIssue(iss, false)} onAddChild={handleOpenCreateChildIssue} isTeamLeader={isTeamLeader} allIssuesMap={allIssuesMap} />
                                    ))}
                                    {flattenedIssues.length === 0 && (
                                        <tr>
                                            <td colSpan={5} className="py-20 text-center">
                                                <div className="text-slate-600">
                                                    <p className="text-lg font-black">Chưa có Issue nào</p>
                                                    <p className="text-sm mt-2">Nhóm chưa được gán công việc hoặc chưa đồng bộ từ Jira.</p>
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}

                    {/* CASE 2: MY TASKS - Việc của tôi */}
                    {!isLecturer && activeTab === 'my-tasks' && (
                        <MyTasksTab
                            issues={flatIssues}
                            currentUser={currentUser}
                            onViewDetail={(iss: any) => { setSelectedIssue(iss); setIsDetailModalOpen(true); }}
                            onEditIssue={(iss: any) => handleEditIssue(iss, true)}
                            isTeamLeader={isTeamLeader}
                            onRefresh={loadData}
                        />
                    )}

                    {activeTab === 'members' && (
                        <div className="max-w-4xl mx-auto space-y-6">
                            {/* Header + Add button */}
                            <div className="flex items-center justify-between">
                                <div>
                                    <h3 className="text-xl font-black text-white">Thành Viên Nhóm</h3>
                                    <p className="text-sm text-slate-500 mt-1">{members.length} thành viên</p>
                                </div>
                                {canManageMembers && (
                                    <button onClick={handleOpenAddMember}
                                            className="flex items-center gap-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-black uppercase tracking-wider transition-all">
                                        <Plus size={16}/> Thêm thành viên
                                    </button>
                                )}
                            </div>

                            {/* Team Leader - same size as members */}
                            {teamLeader && (
                                <div className="flex items-center justify-between p-5 bg-gradient-to-r from-amber-500/10 via-amber-500/5 to-transparent rounded-2xl border border-amber-500/30 hover:bg-amber-500/[0.08] hover:border-amber-500/40 transition-all group">
                                    <div>
                                        <p className="text-white font-bold flex items-center gap-3">
                                            {teamLeader.username}
                                            <span className="px-2.5 py-0.5 bg-amber-500/20 text-amber-400 text-[9px] font-black rounded-full uppercase tracking-widest">
                                                Nhóm Trưởng
                                            </span>
                                        </p>
                                        <p className="text-xs text-slate-500 font-mono tracking-wider mt-1">
                                            {teamLeader.studentCode || '—'}
                                        </p>
                                        <p className="text-xs text-slate-600 mt-0.5">
                                            {teamLeader.email || ''}
                                        </p>
                                        <p className="text-xs text-slate-600 mt-0.5">
                                            Tham gia: {teamLeader.joinedAt ? new Date(teamLeader.joinedAt).toLocaleDateString('vi-VN') : '—'}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <button onClick={() => handleViewMemberDetail(teamLeader)}
                                                className="p-2 bg-blue-600/10 text-blue-400 rounded-xl hover:bg-blue-600 hover:text-white transition-all"
                                                title="Xem chi tiết">
                                            <Eye size={15}/>
                                        </button>
                                        {canManageMembers && (
                                            <button onClick={handleRemoveTeamLeader}
                                                    className="p-2 bg-red-500/10 text-red-400 rounded-xl hover:bg-red-500 hover:text-white transition-all"
                                                    title="Xóa nhóm trưởng">
                                                <Users size={15}/>
                                            </button>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Members list */}
                            <div className="space-y-3">
                                {members.filter((m: any) => (m.memberRole || m.role) !== 'TEAM_LEADER').map((m: any) => (
                                    <div key={m.id}
                                         className="flex items-center justify-between p-5 bg-white/5 rounded-2xl border border-white/5 hover:bg-white/[0.08] hover:border-white/20 transition-all group">
                                        <div>
                                            <p className="text-white font-bold">{m.username || 'Unknown'}</p>
                                            <p className="text-xs text-slate-500 font-mono tracking-wider mt-0.5">{m.studentCode || '—'}</p>
                                            <p className="text-xs text-slate-600 mt-0.5">{m.email || ''}</p>
                                            <p className="text-xs text-slate-600 mt-0.5">
                                                Tham gia: {m.joinedAt ? new Date(m.joinedAt).toLocaleDateString('vi-VN') : '—'}
                                            </p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <button onClick={() => handleViewMemberDetail(m)}
                                                    className="p-2 bg-blue-600/10 text-blue-400 rounded-xl hover:bg-blue-600 hover:text-white transition-all"
                                                    title="Xem chi tiết">
                                                <Eye size={15}/>
                                            </button>
                                            {canManageMembers && (
                                                <>
                                                    <button onClick={() => handleAssignTeamLeader(m)}
                                                            className="p-2 bg-amber-600/10 text-amber-400 rounded-xl hover:bg-amber-600 hover:text-white transition-all"
                                                            title="Chỉ định làm nhóm trưởng">
                                                        <Users size={15}/>
                                                    </button>
                                                    <button onClick={() => handleRemoveMember(m)}
                                                            className="p-2 bg-red-600/10 text-red-400 rounded-xl hover:bg-red-600 hover:text-white transition-all opacity-0 group-hover:opacity-100"
                                                            title="Xóa khỏi nhóm">
                                                        <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                            <path d="M3 6h18"/>
                                                            <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                                                            <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>
                                                        </svg>
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* CASE 4: COMMITS */}
                    {activeTab === 'commits' && (
                        <div className="space-y-6">
                            {commitsLoading ? (
                                <div className="text-center py-20 text-blue-500 font-black animate-pulse uppercase text-xs tracking-widest">Đang quét GitHub...</div>
                            ) : commitsData ? (
                                <>
                                    {/* Bộ lọc thành viên */}
                                    <div className="bg-[#0f172a]/50 border border-white/10 rounded-3xl p-5 shadow-2xl">
                                        <div className="flex flex-col md:flex-row md:items-center gap-4">
                                            <div className="flex-1">
                                                <label className="text-slate-400 text-[10px] font-black uppercase tracking-widest">Lọc theo thành viên</label>
                                                <select
                                                    value={selectedMemberFilter}
                                                    onChange={(e) => setSelectedMemberFilter(e.target.value)}
                                                    className="mt-1.5 w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-white text-sm outline-none focus:border-blue-500/50"
                                                >
                                                    <option value="all" className="bg-[#0f172a]">Tất cả thành viên</option>
                                                    {memberMappings
                                                        .filter((mm: any) => mm.githubUsername)
                                                        .map((mm: any) => (
                                                            <option key={mm.studentId} value={mm.studentUsername} className="bg-[#0f172a]">
                                                                {mm.studentUsername} (@{mm.githubUsername})
                                                            </option>
                                                        ))}
                                                    <option value="unmapped" className="bg-[#0f172a]">Chưa có GitHub</option>
                                                </select>
                                            </div>
                                            {memberMappingsLoading ? (
                                                <span className="text-slate-500 text-xs animate-pulse">Đang tải mapping...</span>
                                            ) : (
                                                <div className="flex gap-2 flex-wrap items-center">
                                                    <span className="bg-emerald-600/20 text-emerald-400 px-3 py-1.5 rounded-lg text-[10px] font-black">
                                                        Đã mapping: {memberMappings.filter(m => m.isMapped).length}
                                                    </span>
                                                    <span className="bg-amber-600/20 text-amber-400 px-3 py-1.5 rounded-lg text-[10px] font-black">
                                                        Chưa mapping: {memberMappings.filter(m => !m.isMapped).length}
                                                    </span>
                                                    <span className="bg-red-600/20 text-red-400 px-3 py-1.5 rounded-lg text-[10px] font-black">
                                                        Chưa liên kết: {commitsData.memberStats?.length - memberMappings.length}
                                                    </span>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Bảng danh sách thành viên */}
                                    <div className="bg-[#0f172a]/50 border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
                                        <div className="p-5 border-b border-white/5 flex items-center justify-between">
                                            <h3 className="text-white font-black text-sm">Danh Sách Thành Viên & GitHub</h3>
                                            <span className="text-slate-500 text-[10px] font-black uppercase tracking-widest">
                                                {commitsData.memberStats?.length || 0} thành viên
                                            </span>
                                        </div>
                                        <div className="overflow-x-auto">
                                        <table className="w-full text-left border-collapse">
                                            <thead className="bg-black/40 text-slate-500 text-[10px] uppercase font-black tracking-[0.2em]">
                                            <tr>
                                                <th className="p-5">Mã SV</th>
                                                <th className="p-5">Họ Tên</th>
                                                <th className="p-5">GitHub</th>
                                                <th className="p-5">Trạng thái</th>
                                                <th className="p-5 text-center">Commits</th>
                                                <th className="p-5">Gần nhất</th>
                                                <th className="p-5 text-center">Chi tiết</th>
                                            </tr>
                                            </thead>
                                            <tbody className="divide-y divide-white/5">
                                            {(commitsData.memberStats || [])
                                                .filter((m: any) => {
                                                    if (selectedMemberFilter === 'all') return true;
                                                    if (selectedMemberFilter === 'unmapped') return !m.githubUsername;
                                                    return m.studentName === selectedMemberFilter;
                                                })
                                                .map((m: any, idx: number) => {
                                                    const mapping = memberMappings.find(
                                                        (mm: any) => mm.studentUsername === m.studentName || mm.githubUsername === m.githubUsername
                                                    );
                                                    const mappingStatus = mapping?.mappingStatus || (m.githubUsername ? 'MAPPED' : 'NOT_MAPPED');

                                                    const statusCfg: Record<string, { label: string; color: string; bg: string }> = {
                                                        NOT_MAPPED:                { label: 'Chưa mapping', color: 'text-red-400', bg: 'bg-red-600/20' },
                                                        MAPPED_BUT_NOT_COLLABORATOR: { label: 'Chưa collaborator', color: 'text-amber-400', bg: 'bg-amber-600/20' },
                                                        MAPPED_AS_ADMIN:           { label: 'Admin', color: 'text-purple-400', bg: 'bg-purple-600/20' },
                                                        MAPPED_AS_MAINTAIN:        { label: 'Maintain', color: 'text-indigo-400', bg: 'bg-indigo-600/20' },
                                                        MAPPED_AS_WRITE:           { label: 'Write', color: 'text-emerald-400', bg: 'bg-emerald-600/20' },
                                                        MAPPED_AS_TRIAGE:          { label: 'Triage', color: 'text-blue-400', bg: 'bg-blue-600/20' },
                                                        MAPPED_AS_READ:            { label: 'Read', color: 'text-slate-400', bg: 'bg-slate-600/20' },
                                                    };
                                                    const sc = statusCfg[mappingStatus] || { label: mappingStatus, color: 'text-slate-400', bg: 'bg-slate-600/20' };

                                                    return (
                                                        <tr key={idx} className="hover:bg-white/[0.02]">
                                                            <td className="p-5 font-mono text-blue-400 text-xs">{m.studentCode}</td>
                                                            <td className="p-5 font-bold text-white text-sm">{m.studentName}</td>
                                                            <td className="p-5 text-slate-400 text-sm">
                                                                {m.githubUsername ? (
                                                                    <a href={`https://github.com/${m.githubUsername}`}
                                                                       target="_blank" rel="noreferrer"
                                                                       className="text-blue-400 hover:text-blue-300 underline underline-offset-2">
                                                                        @{m.githubUsername}
                                                                    </a>
                                                                ) : <span className="text-slate-600 italic">Chưa có</span>}
                                                            </td>
                                                            <td className="p-5">
                                                                {mapping ? (
                                                                    <span className={`inline-block px-2.5 py-1 rounded-lg text-[9px] font-black uppercase tracking-wider ${sc.bg} ${sc.color}`}>
                                                                        {sc.label}
                                                                    </span>
                                                                ) : (
                                                                    <span className="text-slate-600 text-[10px] italic">—</span>
                                                                )}
                                                            </td>
                                                            <td className="p-5 text-center">
                                                                <span className="bg-blue-600/20 text-blue-400 px-3 py-1 rounded-lg font-bold text-xs">{m.totalCommits}</span>
                                                            </td>
                                                            <td className="p-5 text-slate-500 text-xs">
                                                                {m.lastCommitDate ? new Date(m.lastCommitDate).toLocaleDateString('vi-VN') : '—'}
                                                            </td>
                                                            <td className="p-5 text-center">
                                                                <button
                                                                    onClick={() => {
                                                                        const member = memberMappings.find(
                                                                            (mm: any) => mm.studentUsername === m.studentName
                                                                        );
                                                                        if (member) {
                                                                            loadMemberCommits(member.groupMemberId, m.studentName);
                                                                        } else {
                                                                            showLiquidToast('Không tìm thấy thông tin member', 'error');
                                                                        }
                                                                    }}
                                                                    disabled={!m.githubUsername}
                                                                    className="p-2 bg-emerald-600/20 text-emerald-400 rounded-xl hover:bg-emerald-600 hover:text-white transition-all disabled:opacity-30 disabled:cursor-not-allowed"
                                                                    title="Xem commits cá nhân"
                                                                >
                                                                    <GitBranch size={14}/>
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    );
                                                })}
                                            </tbody>
                                        </table>
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                                        <div className="bg-blue-600/10 border border-blue-500/30 rounded-2xl p-6 text-center">
                                            <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Tổng Commits</p>
                                            <p className="text-4xl font-black text-blue-400">{commitsData.totalTeamCommits || 0}</p>
                                        </div>

                                        <div className="bg-emerald-600/10 border border-emerald-500/30 rounded-2xl p-6 text-center">
                                            <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Đã mapping</p>
                                            <p className="text-4xl font-black text-emerald-400">{memberMappings.filter(m => m.isMapped).length}/{memberMappings.length}</p>
                                        </div>
                                        <div className="bg-amber-600/10 border border-amber-500/30 rounded-2xl p-6 text-center">
                                            <p className="text-slate-500 text-[10px] font-black uppercase mb-1">TB Commits/Mem</p>
                                            <p className="text-4xl font-black text-amber-400">{commitsData.averageCommitsPerMember?.toFixed(1) || 0}</p>
                                        </div>
                                    </div>

                                    <div className="bg-[#0f172a]/50 border border-white/10 rounded-3xl p-6 shadow-2xl">
                                        <div className="flex justify-between items-center mb-6">
                                            <h3 className="text-xl font-black text-white">Lịch Sử Commits Theo Thời Gian</h3>
                                            <div className="flex items-center gap-3">
                                                <label className="text-slate-400 text-sm">Khoảng thời gian:</label>
                                                <select
                                                    value={historyDays}
                                                    onChange={(e) => setHistoryDays(parseInt(e.target.value))}
                                                    className="bg-white/5 border border-white/10 rounded-lg px-3 py-1 text-white text-sm"
                                                >
                                                    <option value={7} className="bg-[#0f172a]">7 ngày</option>
                                                    <option value={14} className="bg-[#0f172a]">14 ngày</option>
                                                    <option value={30} className="bg-[#0f172a]">30 ngày</option>
                                                    <option value={60} className="bg-[#0f172a]">60 ngày</option>
                                                    <option value={90} className="bg-[#0f172a]">90 ngày</option>
                                                </select>
                                            </div>
                                        </div>

                                        {commitHistoryLoading ? (
                                            <div className="text-center py-10 text-blue-500 font-black animate-pulse uppercase text-xs tracking-widest">Đang tải lịch sử...</div>
                                        ) : commitHistoryData ? (
                                            <div className="space-y-4">
                                                {(commitHistoryData.dailyStats || []).map((day: any, idx: number) => (
                                                    <div key={idx} className="bg-white/5 border border-white/10 rounded-2xl p-4">
                                                        <div className="flex justify-between items-center mb-3">
                                                            <div className="flex items-center gap-3">
                                                                <span className="text-white font-bold text-sm">
                                                                    {new Date(day.date).toLocaleDateString('vi-VN', {
                                                                        weekday: 'long',
                                                                        year: 'numeric',
                                                                        month: 'long',
                                                                        day: 'numeric'
                                                                    })}
                                                                </span>
                                                                <span className="bg-blue-600/20 text-blue-400 px-2 py-1 rounded text-xs font-bold">
                                                                    {day.totalCommits} commits
                                                                </span>
                                                            </div>
                                                            <div className="flex gap-1">
                                                                {Object.entries(day.contributors || {}).map(([author, count]: [string, any]) => (
                                                                    <span key={author} className="bg-slate-600/20 text-slate-300 px-2 py-1 rounded text-xs">
                                                                        {author}: {count}
                                                                    </span>
                                                                ))}
                                                            </div>
                                                        </div>
                                                        <div className="space-y-2">
                                                            {(day.commits || []).map((commit: any, commitIdx: number) => (
                                                                <div key={commitIdx} className="flex items-center gap-3 text-sm">
                                                                    <span className="text-slate-500 font-mono text-xs w-16 truncate">{commit.sha?.substring(0, 7)}</span>
                                                                    <span className="text-slate-400 flex-1 truncate">{commit.message}</span>
                                                                    <span className="text-blue-400 text-xs">{commit.author}</span>
                                                                    <span className="text-slate-600 text-xs">
                                                                        {new Date(commit.date).toLocaleTimeString('vi-VN', {
                                                                            hour: '2-digit',
                                                                            minute: '2-digit'
                                                                        })}
                                                                    </span>
                                                                </div>
                                                            ))}
                                                        </div>
                                                    </div>
                                                ))}
                                                {(commitHistoryData.dailyStats || []).length === 0 && (
                                                    <div className="text-center py-10 text-slate-500 italic">
                                                        Không có commits trong khoảng thời gian này
                                                    </div>
                                                )}
                                            </div>
                                        ) : (
                                            <div className="text-center py-10 text-slate-500 italic">
                                                Không thể tải lịch sử commits
                                            </div>
                                        )}
                                    </div>

                                </>
                            ) : <div className="text-center py-20 text-slate-600 italic">Nhóm chưa cấu hình GitHub repository.</div>}
                        </div>
                    )}

                    {/* CASE 5: REPORT */}
                    {activeTab === 'report' && (
                        <ProgressReport />
                    )}
                </motion.div>
            </AnimatePresence>

            {/* Add Member Modal */}
            <AnimatePresence>
                {isAddMemberModalOpen && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
                        onClick={() => setIsAddMemberModalOpen(false)}
                    >
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0, y: 20 }}
                            animate={{ scale: 1, opacity: 1, y: 0 }}
                            exit={{ scale: 0.9, opacity: 0, y: 20 }}
                            className="bg-[#0f172a] border border-white/10 rounded-3xl p-6 w-full max-w-lg mx-4 shadow-2xl"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="flex justify-between items-center mb-6">
                                <h3 className="text-xl font-black text-white">Thêm Thành Viên</h3>
                                <button onClick={() => setIsAddMemberModalOpen(false)}
                                        className="p-1.5 bg-white/5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-all">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <line x1="18" y1="6" x2="6" y2="18"/>
                                        <line x1="6" y1="6" x2="18" y2="18"/>
                                    </svg>
                                </button>
                            </div>

                            {/* Search input */}
                            <div className="relative mb-4">
                                <input
                                    type="text"
                                    placeholder="Tìm kiếm sinh viên theo mã, tên, email..."
                                    value={searchQuery}
                                    onChange={(e) => handleSearchStudent(e.target.value)}
                                    className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white text-sm placeholder-slate-600 focus:outline-none focus:border-blue-500/50 transition-all"
                                    autoFocus
                                />
                                {searching && (
                                    <div className="absolute right-3 top-3">
                                        <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"/>
                                    </div>
                                )}
                            </div>

                            {/* Search results */}
                            <div className="max-h-72 overflow-y-auto space-y-2 custom-scrollbar">
                                {searchQuery.length >= 2 && searchResults.length === 0 && !searching && (
                                    <div className="text-center py-8 text-slate-600 italic text-sm">
                                        Không tìm thấy sinh viên nào
                                    </div>
                                )}
                                {searchResults.map((student: any) => (
                                    <div key={student.id}
                                         className="flex items-center justify-between p-3 bg-white/5 rounded-xl hover:bg-white/[0.08] border border-white/5 hover:border-blue-500/30 transition-all cursor-pointer group"
                                         onClick={() => handleAddMember(student.id)}
                                    >
                                        <div>
                                            <p className="text-white font-semibold text-sm">{student.username}</p>
                                            <p className="text-xs text-slate-500 font-mono">{student.studentCode}</p>
                                            <p className="text-xs text-slate-600 mt-0.5">{student.email || ''}</p>
                                        </div>
                                        <button className="px-3 py-1.5 bg-emerald-600/20 text-emerald-400 rounded-lg text-xs font-bold opacity-0 group-hover:opacity-100 transition-all hover:bg-emerald-600 hover:text-white">
                                            + Thêm
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* Student Detail Modal */}
            <AnimatePresence>
                {isStudentDetailModalOpen && selectedMember && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
                        onClick={() => setIsStudentDetailModalOpen(false)}
                    >
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0, y: 20 }}
                            animate={{ scale: 1, opacity: 1, y: 0 }}
                            exit={{ scale: 0.9, opacity: 0, y: 20 }}
                            className="bg-[#0f172a] border border-white/10 rounded-3xl p-6 w-full max-w-md mx-4 shadow-2xl"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="flex justify-between items-center mb-6">
                                <h3 className="text-xl font-black text-white">Chi Tiết Thành Viên</h3>
                                <button onClick={() => setIsStudentDetailModalOpen(false)}
                                        className="p-1.5 bg-white/5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-all">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <line x1="18" y1="6" x2="6" y2="18"/>
                                        <line x1="6" y1="6" x2="18" y2="18"/>
                                    </svg>
                                </button>
                            </div>

                            <div className="flex flex-col items-center mb-6">
                                <p className="text-white font-bold text-xl">
                                    {selectedMember.username || 'Unknown'}
                                </p>
                                {(selectedMember.memberRole || selectedMember.role) === 'TEAM_LEADER' && (
                                    <span className="mt-2 px-4 py-1 bg-amber-500/20 text-amber-400 text-[10px] font-black rounded-full uppercase tracking-widest">
                                        Nhóm Trưởng
                                    </span>
                                )}
                            </div>

                            <div className="space-y-3">
                                <div className="flex justify-between p-3 bg-white/5 rounded-xl">
                                    <span className="text-slate-500 text-sm">Mã sinh viên</span>
                                    <span className="text-white font-mono text-sm">{selectedMember.studentCode || '—'}</span>
                                </div>
                                <div className="flex justify-between p-3 bg-white/5 rounded-xl">
                                    <span className="text-slate-500 text-sm">Email</span>
                                    <span className="text-white text-sm">{selectedMember.email || '—'}</span>
                                </div>
                                <div className="flex justify-between p-3 bg-white/5 rounded-xl">
                                    <span className="text-slate-500 text-sm">Vai trò</span>
                                    <span className="text-white text-sm">
                                        {(selectedMember.memberRole || selectedMember.role) === 'TEAM_LEADER' ? 'Nhóm trưởng' : 'Thành viên'}
                                    </span>
                                </div>
                                <div className="flex justify-between p-3 bg-white/5 rounded-xl">
                                    <span className="text-slate-500 text-sm">Ngày tham gia</span>
                                    <span className="text-white text-sm">
                                        {selectedMember.joinedAt
                                            ? new Date(selectedMember.joinedAt).toLocaleDateString('vi-VN')
                                            : '—'}
                                    </span>
                                </div>
                            </div>

                            {canManageMembers && (
                                <div className="flex gap-3 mt-6">
                                    {(selectedMember.memberRole || selectedMember.role) !== 'TEAM_LEADER' && (
                                        <button onClick={() => {
                                            setIsStudentDetailModalOpen(false);
                                            handleAssignTeamLeader(selectedMember);
                                        }}
                                                className="flex-1 px-4 py-2.5 bg-amber-600/20 text-amber-400 rounded-xl text-xs font-black uppercase tracking-wider hover:bg-amber-600 hover:text-white transition-all">
                                            Chỉ định nhóm trưởng
                                        </button>
                                    )}
                                    <button onClick={() => {
                                        setIsStudentDetailModalOpen(false);
                                        handleRemoveMember(selectedMember);
                                    }}
                                            className="flex-1 px-4 py-2.5 bg-red-600/20 text-red-400 rounded-xl text-xs font-black uppercase tracking-wider hover:bg-red-600 hover:text-white transition-all">
                                        Xóa khỏi nhóm
                                    </button>
                            </div>
                            )}
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* Member Commits Modal */}
            <AnimatePresence>
                {isMemberCommitsModalOpen && selectedMemberCommits && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
                        onClick={() => setIsMemberCommitsModalOpen(false)}
                    >
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0, y: 20 }}
                            animate={{ scale: 1, opacity: 1, y: 0 }}
                            exit={{ scale: 0.9, opacity: 0, y: 20 }}
                            className="bg-[#0f172a] border border-white/10 rounded-3xl p-6 w-full max-w-2xl mx-4 shadow-2xl max-h-[80vh] overflow-y-auto"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="flex justify-between items-center mb-6">
                                <h3 className="text-xl font-black text-white flex items-center gap-2">
                                    <GitCommit className="text-emerald-400" size={20}/>
                                    Commits của: {selectedMemberDetails?.name}
                                </h3>
                                <button onClick={() => setIsMemberCommitsModalOpen(false)}
                                        className="p-1.5 bg-white/5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-all">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <line x1="18" y1="6" x2="6" y2="18"/>
                                        <line x1="6" y1="6" x2="18" y2="18"/>
                                    </svg>
                                </button>
                            </div>

                            <div className="space-y-1 mb-4">
                                <div className="bg-white/5 rounded-xl p-3 flex justify-between items-center">
                                    <span className="text-slate-400 text-sm">Tổng commits</span>
                                    <span className="text-2xl font-black text-blue-400">{selectedMemberCommits.totalCommits || 0}</span>
                                </div>
                                {selectedMemberCommits.lastCommitDate && (
                                    <div className="bg-white/5 rounded-xl p-3 flex justify-between items-center">
                                        <span className="text-slate-400 text-sm">Commit cuối</span>
                                        <span className="text-white text-sm font-mono">
                                            {new Date(selectedMemberCommits.lastCommitDate).toLocaleDateString('vi-VN')}
                                        </span>
                                    </div>
                                )}
                            </div>

                            <h4 className="text-white font-bold text-sm mb-3">Danh sách commits:</h4>
                            <div className="space-y-3">
                                {(selectedMemberCommits.commits || []).length > 0 ? (
                                    (selectedMemberCommits.commits || []).map((commit: any, idx: number) => (
                                        <div key={idx} className="flex items-start gap-3 p-3 bg-white/5 rounded-xl border border-white/5 hover:border-blue-500/30 transition-all">
                                            <div className="w-8 h-8 rounded-full bg-emerald-600/20 flex items-center justify-center shrink-0 mt-0.5">
                                                <GitCommit className="text-emerald-400" size={14}/>
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-2 mb-1">
                                                    <span className="font-mono text-[10px] text-slate-500 bg-white/5 px-1.5 py-0.5 rounded">
                                                        {commit.sha?.toString().substring(0, 7)}
                                                    </span>
                                                    <a href={commit.html_url} target="_blank" rel="noreferrer"
                                                       className="text-blue-400 hover:text-blue-300" title="Xem trên GitHub">
                                                        <ExternalLink size={12}/>
                                                    </a>
                                                </div>
                                                <p className="text-white text-sm font-medium truncate">
                                                    {(commit.commit?.message || commit.message || '').split('\n')[0]}
                                                </p>
                                                <p className="text-slate-500 text-xs mt-1">
                                                    {commit.commit?.author?.date
                                                        ? new Date(commit.commit.author.date).toLocaleString('vi-VN')
                                                        : commit.date
                                                            ? new Date(commit.date).toLocaleString('vi-VN')
                                                            : ''}
                                                </p>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="text-center py-8 text-slate-500 italic">
                                        Không có dữ liệu commit cho thành viên này
                                    </div>
                                )}
                            </div>

                            {selectedMemberCommits.lastCommitUrl && (
                                <div className="mt-6 text-center">
                                    <a href={selectedMemberCommits.lastCommitUrl}
                                       target="_blank" rel="noreferrer"
                                       className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600/20 text-blue-400 rounded-xl text-xs font-black hover:bg-blue-600 hover:text-white transition-all">
                                        <ExternalLink size={14}/> Xem trên GitHub
                                    </a>
                                </div>
                            )}
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>

            <IssueDetailModal isOpen={isDetailModalOpen} onClose={() => setIsDetailModalOpen(false)} issue={selectedIssue} />

            {/* Create Issue Modal */}
            <CreateIssueModal
                isOpen={isCreateIssueModalOpen}
                onClose={() => setIsCreateIssueModalOpen(false)}
                groupId={groupData?.id}
                onSuccess={loadData}
                initialParentId={createParentId}
                initialIssueType={createIssueType}
            />

            {/* Edit Issue Modal */}
            <EditIssueModal
                isOpen={isEditIssueModalOpen}
                onClose={() => setIsEditIssueModalOpen(false)}
                issue={editIssue}
                groupId={groupData?.id}
                onSuccess={loadData}
                hideAssignee={hideEditAssignee}
            />
        </div>
    );
};

export default GroupWorkspace;

