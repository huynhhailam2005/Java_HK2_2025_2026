import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
    Users, ChevronRight, ChevronDown,
    BookOpen, UserCircle, Layers, RefreshCw
} from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { issueApi } from '../services/issueApi';
import { type IssueDto, type IssueTreeNode, buildIssueTree } from '../types/api';

const IssueRow = ({ issue, depth = 0 }: { issue: IssueTreeNode; depth: number }) => {
    const [isOpen, setIsOpen] = useState(true);
    const hasChildren = issue.children && issue.children.length > 0;

    return (
        <>
            <div
                className="hover:bg-white/[0.05] transition-colors border-b border-white/5 flex items-center p-4 group"
                style={{ paddingLeft: `${depth * 28 + 16}px` }}
            >
                <div className="flex items-center gap-3 flex-1">
                    {/* Nút toggle mở rộng */}
                    <div className="w-6 h-6 flex items-center justify-center">
                        {hasChildren ? (
                            <button
                                onClick={(e) => { e.stopPropagation(); setIsOpen(!isOpen); }}
                                className="text-slate-500 hover:text-white transition-colors"
                            >
                                {isOpen ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                            </button>
                        ) : (
                            <div className="w-1.5 h-1.5 bg-slate-700 rounded-full" />
                        )}
                    </div>

                    <div className={`px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-tighter ${
                        issue.type === 'EPIC' ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30' :
                            issue.type === 'SUB_TASK' ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30' :
                                'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                    }`}>
                        {issue.type}
                    </div>

                    <div className="flex flex-col">
                        <span className="text-white font-bold text-sm leading-tight">{issue.title}</span>
                        <span className="text-[10px] text-slate-500 font-mono mt-0.5">{issue.issueCode || 'LOCAL-ONLY'}</span>
                    </div>
                </div>

                <div className="flex items-center gap-8 text-xs font-bold shrink-0 px-4">
                    <div className="flex items-center gap-2 text-slate-400 w-32 justify-end">
                        <span className="truncate max-w-[100px]">{issue.assignedTo}</span>
                        <UserCircle size={14} className={issue.assignedTo !== 'Chưa gán' ? "text-blue-400" : "text-slate-600"} />
                    </div>

                    <div className={`w-28 text-center py-1.5 rounded-lg border uppercase text-[10px] font-black ${
                        issue.status === 'DONE' ? 'text-emerald-400 border-emerald-500/20 bg-emerald-500/5' :
                            issue.status === 'IN_PROGRESS' ? 'text-blue-400 border-blue-500/20 bg-blue-500/5' :
                                'text-slate-500 border-white/5 bg-white/5'
                    }`}>
                        {issue.status.replace('_', ' ')}
                    </div>
                </div>
            </div>

            <AnimatePresence>
                {hasChildren && isOpen && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: 'auto', opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        className="overflow-hidden bg-white/[0.01]"
                    >
                        {issue.children.map(child => (
                            <IssueRow key={child.issueId} issue={child} depth={depth + 1} />
                        ))}
                    </motion.div>
                )}
            </AnimatePresence>
        </>
    );
};

const ManageProjects = () => {
    const [user, setUser] = useState<any>(null);
    const [myGroups, setMyGroups] = useState<any[]>([]);
    const [selectedGroup, setSelectedGroup] = useState<any | null>(null);
    const [treeIssues, setTreeIssues] = useState<IssueTreeNode[]>([]);
    const [loadingGroups, setLoadingGroups] = useState(true);
    const [loadingIssues, setLoadingIssues] = useState(false);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);
            fetchMyGroups(parsedUser.id);
        }
    }, []);

    const fetchMyGroups = async (lecturerId: number) => {
        setLoadingGroups(true);
        try {
            const res = await adminApi.getGroups();
            const allGroups = res.data.data as any[];
            // Lọc nhóm của giảng viên đang đăng nhập
            const filtered = allGroups.filter(g => g.lecturer?.id === lecturerId);
            setMyGroups(filtered);
        } catch (error) {
            console.error("Error fetching groups:", error);
        } finally {
            setLoadingGroups(false);
        }
    };

    const fetchGroupIssues = async (groupId: number) => {
        setLoadingIssues(true);
        try {
            const res = await issueApi.getIssuesByGroup(groupId);
            const flatData = res.data.data as unknown as IssueDto[];

            const tree = buildIssueTree(flatData);
            setTreeIssues(tree);
        } catch (error) {
            console.error("Error fetching issues:", error);
            setTreeIssues([]);
        } finally {
            setLoadingIssues(false);
        }
    };

    const handleSelectGroup = (group: any) => {
        setSelectedGroup(group);
        fetchGroupIssues(group.id);
    };

    return (
        <div className="space-y-8 h-[calc(100vh-140px)] flex flex-col">
            <div className="shrink-0 px-2">
                <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                    <BookOpen className="text-purple-500" /> Quản Lý Đồ Án
                </h1>
                <p className="text-slate-400 mt-2 italic text-sm">Giảng viên: <span className="text-purple-400 font-bold">{user?.username}</span></p>
            </div>

            <div className="flex flex-col lg:flex-row gap-6 flex-1 min-h-0">
                {/* SIDEBAR: DANH SÁCH NHÓM */}
                <div className="lg:w-1/3 bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col overflow-hidden">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5 shrink-0">
                        <Users className="w-5 h-5 text-slate-400" />
                        <h3 className="font-bold text-slate-300 uppercase tracking-widest text-xs">Nhóm hướng dẫn</h3>
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                        {loadingGroups ? (
                            <div className="text-center py-10 animate-pulse text-slate-600 font-bold text-xs uppercase">Đang tải...</div>
                        ) : (
                            myGroups.map(group => (
                                <button
                                    key={group.id}
                                    onClick={() => handleSelectGroup(group)}
                                    className={`w-full text-left p-4 rounded-2xl border transition-all flex items-center justify-between group ${
                                        selectedGroup?.id === group.id
                                            ? 'bg-purple-600/20 border-purple-500/50 shadow-lg'
                                            : 'bg-white/5 border-white/5 hover:border-white/20'
                                    }`}
                                >
                                    <div className="flex-1">
                                        <div className="text-[10px] font-mono mb-1 text-slate-500">{group.groupCode || 'N/A'}</div>
                                        <div className="text-white font-bold text-sm leading-tight group-hover:text-purple-300 transition-colors">{group.groupName}</div>
                                    </div>
                                    <ChevronRight size={18} className={selectedGroup?.id === group.id ? 'text-purple-400' : 'text-slate-600'} />
                                </button>
                            ))
                        )}
                    </div>
                </div>

                {/* MAIN: TREE VIEW ISSUES */}
                <div className="lg:w-2/3 bg-black/20 border border-white/5 rounded-3xl flex flex-col overflow-hidden relative shadow-2xl">
                    {!selectedGroup ? (
                        <div className="flex-1 flex flex-col items-center justify-center text-slate-500 p-10 text-center">
                            <Layers className="w-16 h-16 mb-4 opacity-10 animate-bounce" />
                            <p className="font-bold text-sm uppercase tracking-widest opacity-40">Chọn nhóm để xem cấu trúc công việc</p>
                        </div>
                    ) : (
                        <>
                            <div className="p-6 bg-white/[0.03] border-b border-white/5 flex justify-between items-center">
                                <div>
                                    <h3 className="font-black text-white text-lg leading-none">{selectedGroup.groupName}</h3>
                                    <p className="text-[10px] text-purple-400 font-mono mt-2 uppercase tracking-widest">Phân cấp Issue (Epic {'>'} Task {'>'} Sub-task)</p>
                                </div>
                                <button onClick={() => fetchGroupIssues(selectedGroup.id)} className="p-2.5 bg-white/5 rounded-xl text-slate-400 hover:text-white transition-all">
                                    <RefreshCw size={18} className={loadingIssues ? 'animate-spin' : ''} />
                                </button>
                            </div>

                            <div className="flex-1 overflow-y-auto custom-scrollbar">
                                {loadingIssues ? (
                                    <div className="flex flex-col items-center justify-center h-full gap-3">
                                        <div className="w-8 h-8 border-2 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
                                        <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Đang tải dữ liệu...</span>
                                    </div>
                                ) : treeIssues.length === 0 ? (
                                    <div className="p-20 text-center text-slate-600 italic text-sm">Chưa có công việc nào.</div>
                                ) : (
                                    <div className="divide-y divide-white/5 pb-20">
                                        {/* Header Giả cho Tree */}
                                        <div className="flex items-center px-4 py-3 bg-black/40 text-[9px] font-black text-slate-500 uppercase tracking-[0.2em] border-b border-white/5 sticky top-0 z-10">
                                            <div className="flex-1 pl-10">Cấu trúc phân tầng</div>
                                            <div className="w-32 text-right pr-10">Người gán</div>
                                            <div className="w-28 text-center pr-4">Trạng thái</div>
                                        </div>

                                        {treeIssues.map(node => (
                                            <IssueRow key={node.issueId} issue={node} depth={0} />
                                        ))}
                                    </div>
                                )}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ManageProjects;