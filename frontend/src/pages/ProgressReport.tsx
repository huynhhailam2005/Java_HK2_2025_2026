import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { BarChart3, Download, Github, RefreshCw, User, FileText, CheckCircle, Clock, AlertCircle, GitCommit } from 'lucide-react';
import { reportApi, type GroupProgressReport, type CommitItem } from '../services/reportApi';
import { showLiquidToast } from '../utils/toast';

const ProgressReport = () => {
    const { groupId } = useParams<{ groupId: string }>();
    const [report, setReport] = useState<GroupProgressReport | null>(null);
    const [loading, setLoading] = useState(true);
    const [exporting, setExporting] = useState(false);

    useEffect(() => {
        if (groupId) void fetchReport();
    }, [groupId]);

    const fetchReport = async () => {
        setLoading(true);
        try {
            const res = await reportApi.getGroupReport(parseInt(groupId!));
            if (res.data.success) {
                setReport(res.data.data);
            } else {
                showLiquidToast(res.data.message || 'Lỗi tải báo cáo', 'error');
            }
        } catch {
            showLiquidToast('Không thể kết nối server', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleExportPDF = async () => {
        setExporting(true);
        try {
            const res = await reportApi.exportReport(parseInt(groupId!), 'pdf');

            // Tạo blob URL và trigger download
            const blob = new Blob([res.data], { type: 'application/pdf' });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `BaoCao_TienDo_Nhom${report?.groupCode || groupId}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            showLiquidToast('✅ Tải file PDF thành công!', 'success');
        } catch {
            showLiquidToast('❌ Lỗi xuất PDF', 'error');
        } finally {
            setExporting(false);
        }
    };

    if (loading) {
        return (
            <div className="py-40 text-center">
                <div className="text-emerald-500 font-black animate-pulse uppercase text-xs tracking-widest">
                    Đang tổng hợp dữ liệu báo cáo...
                </div>
            </div>
        );
    }

    if (!report) {
        return (
            <div className="py-40 text-center">
                <p className="text-slate-500 text-lg font-black">Không có dữ liệu báo cáo</p>
                <p className="text-slate-600 text-sm mt-2">Nhóm chưa có Issue hoặc chưa được đồng bộ.</p>
            </div>
        );
    }

    const progressValue = Math.round(report.progress || 0);
    const members = report.memberContributions || [];

    return (
        <div className="space-y-8">
            {/* Header */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                        <BarChart3 className="text-emerald-400" />
                        Báo Cáo Tiến Độ: {report.groupName}
                    </h1>
                    <p className="text-slate-400 mt-2">Phân tích hiệu suất làm việc dựa trên Jira & GitHub</p>
                </div>
                <div className="flex gap-3">
                    <button onClick={fetchReport}
                            className="p-3 bg-white/5 border border-white/10 rounded-2xl text-slate-300 hover:bg-white/10 transition-all">
                        <RefreshCw className="w-5 h-5" />
                    </button>
                    <button onClick={handleExportPDF} disabled={exporting}
                            className="bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 disabled:cursor-wait text-white px-6 py-3 rounded-2xl font-bold transition-all flex items-center gap-2 shadow-lg shadow-emerald-500/20">
                        <Download className="w-5 h-5" />
                        {exporting ? 'Đang xuất...' : 'Xuất PDF'}
                    </button>
                </div>
            </div>

            {/* Overall Progress */}
            <div className="bg-linear-to-br from-emerald-500/20 to-teal-500/5 border border-emerald-500/20 rounded-4xl p-8 shadow-sm">
                <div className="flex justify-between items-end mb-6">
                    <div>
                        <span className="text-emerald-400 font-bold uppercase tracking-widest text-sm">Tiến độ tổng thể</span>
                        <h2 className="text-5xl font-black text-white mt-1">{progressValue}%</h2>
                    </div>
                    <div className="text-right text-slate-400 text-xs space-y-1">
                        <p className="flex items-center gap-2 justify-end">
                            <FileText className="w-3 h-3" />
                            {report.totalIssues} issues • {report.totalMembers} thành viên
                        </p>
                    </div>
                </div>
                <div className="w-full h-4 bg-black/40 rounded-full overflow-hidden border border-white/5 relative">
                    <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${progressValue}%` }}
                        className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 shadow-[0_0_20px_rgba(16,185,129,0.5)]"
                    />
                </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="bg-slate-600/10 border border-slate-500/30 rounded-2xl p-6 text-center">
                    <FileText className="w-8 h-8 mx-auto mb-2 text-slate-400" />
                    <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Tổng Issues</p>
                    <p className="text-4xl font-black text-white">{report.totalIssues}</p>
                </div>
                <div className="bg-emerald-600/10 border border-emerald-500/30 rounded-2xl p-6 text-center">
                    <CheckCircle className="w-8 h-8 mx-auto mb-2 text-emerald-400" />
                    <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Hoàn Thành</p>
                    <p className="text-4xl font-black text-emerald-400">{report.completedIssues}</p>
                </div>
                <div className="bg-blue-600/10 border border-blue-500/30 rounded-2xl p-6 text-center">
                    <Clock className="w-8 h-8 mx-auto mb-2 text-blue-400" />
                    <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Đang Làm</p>
                    <p className="text-4xl font-black text-blue-400">{report.inProgressIssues}</p>
                </div>
                <div className="bg-amber-600/10 border border-amber-500/30 rounded-2xl p-6 text-center">
                    <AlertCircle className="w-8 h-8 mx-auto mb-2 text-amber-400" />
                    <p className="text-slate-500 text-[10px] font-black uppercase mb-1">Chưa Làm</p>
                    <p className="text-4xl font-black text-amber-400">{report.todoIssues}</p>
                </div>
            </div>

            {/* Member Contributions */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Biểu đồ thanh đóng góp */}
                <div className="bg-white/5 border border-white/10 rounded-3xl p-6 shadow-sm">
                    <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                        <User className="text-blue-400" /> Đóng góp thành viên
                    </h3>
                    <div className="space-y-6">
                        {members.map((member) => (
                            <div key={member.memberId} className="space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-white font-bold">{member.username}</span>
                                    <span className="text-slate-400">{member.completionRate}%</span>
                                </div>
                                <div className="flex items-center gap-3 text-xs text-slate-500">
                                    <span>{member.role}</span>
                                    <span>•</span>
                                    <span>GitHub: {member.githubUsername}</span>
                                </div>
                                <div className="w-full h-2 bg-white/5 rounded-full overflow-hidden">
                                    <motion.div
                                        initial={{ width: 0 }}
                                        animate={{ width: `${member.completionRate}%` }}
                                        className="h-full bg-blue-500 rounded-full"
                                    />
                                </div>
                            </div>
                        ))}
                        {members.length === 0 && (
                            <p className="text-slate-500 italic text-sm text-center py-8">
                                Chưa có thành viên nào
                            </p>
                        )}
                    </div>
                </div>

                {/* Bảng chi tiết */}
                <div className="bg-white/5 border border-white/10 rounded-3xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse min-w-[400px]">
                            <thead className="bg-white/5 text-slate-400 text-xs uppercase font-black">
                            <tr>
                                <th className="p-4">Thành viên</th>
                                <th className="p-4 text-center">Được giao</th>
                                <th className="p-4 text-center">Hoàn thành</th>
                                <th className="p-4 text-center">Commits</th>
                                <th className="p-4 text-center">Tỉ lệ</th>
                            </tr>
                            </thead>
                            <tbody className="text-slate-300">
                            {members.map((member) => (
                                <tr key={member.memberId} className="border-t border-white/5 hover:bg-white/5 transition-colors">
                                    <td className="p-4">
                                        <div>
                                            <span className="font-bold text-white">{member.username}</span>
                                            <span className="text-xs text-slate-500 ml-2">({member.studentCode})</span>
                                        </div>
                                        <div className="text-xs text-slate-600 mt-0.5">{member.role}</div>
                                    </td>
                                    <td className="p-4 text-center font-bold">{member.assignedIssues}</td>
                                    <td className="p-4 text-center">
                                        <span className="text-emerald-400 font-bold">{member.completedIssues}</span>
                                    </td>
                                    <td className="p-4 text-center">
                                        <span className="text-blue-400 font-bold">{member.commitCount}</span>
                                    </td>
                                    <td className="p-4 text-center">
                                        <span className={`px-2 py-1 rounded-lg text-xs font-bold ${
                                            member.completionRate >= 80 ? 'bg-emerald-600/20 text-emerald-400' :
                                            member.completionRate >= 50 ? 'bg-amber-600/20 text-amber-400' :
                                            'bg-red-600/20 text-red-400'
                                        }`}>
                                            {member.completionRate}%
                                        </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Git Commit History */}
            <div className="bg-white/5 border border-white/10 rounded-3xl p-6 shadow-sm">
                <div className="flex items-center justify-between mb-6">
                    <h3 className="text-xl font-bold text-white flex items-center gap-2">
                        <GitCommit className="text-indigo-400" /> Lịch Sử Git Commits
                    </h3>
                    <span className="text-sm text-slate-400">
                        Tổng số: <span className="text-indigo-400 font-bold">{report.commitHistory?.length || 0}</span> commits
                    </span>
                </div>

                {report.commitHistory && report.commitHistory.length > 0 ? (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse min-w-[600px]">
                            <thead className="bg-white/5 text-slate-400 text-xs uppercase font-black">
                                <tr>
                                    <th className="p-3 pl-0">Thời gian</th>
                                    <th className="p-3">Tác giả</th>
                                    <th className="p-3">Nội dung</th>
                                    <th className="p-3 text-right pr-0">SHA</th>
                                </tr>
                            </thead>
                            <tbody className="text-slate-300">
                                {report.commitHistory.slice(0, 100).map((commit: CommitItem, idx: number) => {
                                    // Format date
                                    let formattedDate = commit.date;
                                    try {
                                        const d = new Date(commit.date);
                                        formattedDate = d.toLocaleDateString('vi-VN', {
                                            day: '2-digit',
                                            month: '2-digit',
                                            year: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        });
                                    } catch {}

                                    // Get short SHA
                                    const shortSha = commit.sha ? commit.sha.substring(0, 7) : '';

                                    // Get first line of message
                                    const shortMsg = commit.message?.split('\n')[0] || '';

                                    return (
                                        <tr key={commit.sha || idx} className="border-t border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="p-3 pl-0 text-xs text-slate-400 whitespace-nowrap">
                                                {formattedDate}
                                            </td>
                                            <td className="p-3">
                                                <span className="text-xs font-bold text-indigo-300">
                                                    @{commit.author}
                                                </span>
                                            </td>
                                            <td className="p-3 text-xs text-slate-300 max-w-[300px]">
                                                <span className="line-clamp-1">{shortMsg}</span>
                                            </td>
                                            <td className="p-3 pr-0 text-right">
                                                <code className="text-[10px] font-mono text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded">
                                                    {shortSha}
                                                </code>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                        {report.commitHistory.length > 100 && (
                            <p className="text-center text-slate-500 text-xs mt-4">
                                ... và {report.commitHistory.length - 100} commits khác
                            </p>
                        )}
                    </div>
                ) : (
                    <div className="text-center py-10">
                        <Github className="w-12 h-12 mx-auto mb-3 text-slate-600" />
                        <p className="text-slate-500 italic text-sm">
                            {report.githubRepoUrl ? 'Chưa có commit nào được ghi nhận' : 'GitHub chưa được cấu hình cho nhóm này'}
                        </p>
                    </div>
                )}
            </div>

            {/* Issues by Status */}
            {report.issuesByStatus && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* DONE */}
                    <div className="bg-[#0f172a]/50 border border-emerald-500/20 rounded-3xl p-5 shadow-2xl">
                        <h3 className="text-emerald-400 font-black text-lg mb-4 flex items-center gap-2">
                            <CheckCircle className="w-5 h-5" />
                            Done ({report.issuesByStatus.DONE?.length || 0})
                        </h3>
                        <div className="space-y-2 max-h-100 overflow-y-auto custom-scrollbar">
                            {(report.issuesByStatus.DONE || []).map((issue: any) => (
                                <div key={issue.issueId} className="bg-white/5 rounded-xl p-3 border border-white/5">
                                    <div className="flex items-center gap-2">
                                        {issue.issueCode && <span className="font-mono text-blue-400 text-[10px] font-bold">{issue.issueCode}</span>}
                                        <span className="text-white text-xs font-semibold line-clamp-1">{issue.title}</span>
                                    </div>
                                    <p className="text-slate-600 text-[10px] mt-1">👤 {issue.assignedTo}</p>
                                </div>
                            ))}
                            {(report.issuesByStatus.DONE || []).length === 0 && (
                                <p className="text-slate-600 italic text-xs text-center py-6">Chưa có Issue nào hoàn thành</p>
                            )}
                        </div>
                    </div>

                    {/* IN_PROGRESS */}
                    <div className="bg-[#0f172a]/50 border border-blue-500/20 rounded-3xl p-5 shadow-2xl">
                        <h3 className="text-blue-400 font-black text-lg mb-4 flex items-center gap-2">
                            <Clock className="w-5 h-5" />
                            In Progress ({report.issuesByStatus.IN_PROGRESS?.length || 0})
                        </h3>
                        <div className="space-y-2 max-h-100 overflow-y-auto custom-scrollbar">
                            {(report.issuesByStatus.IN_PROGRESS || []).map((issue: any) => (
                                <div key={issue.issueId} className="bg-white/5 rounded-xl p-3 border border-white/5">
                                    <div className="flex items-center gap-2">
                                        {issue.issueCode && <span className="font-mono text-blue-400 text-[10px] font-bold">{issue.issueCode}</span>}
                                        <span className="text-white text-xs font-semibold line-clamp-1">{issue.title}</span>
                                    </div>
                                    <p className="text-slate-600 text-[10px] mt-1">👤 {issue.assignedTo}</p>
                                </div>
                            ))}
                            {(report.issuesByStatus.IN_PROGRESS || []).length === 0 && (
                                <p className="text-slate-600 italic text-xs text-center py-6">Không có Issue nào đang làm</p>
                            )}
                        </div>
                    </div>

                    <div className="bg-[#0f172a]/50 border border-amber-500/20 rounded-3xl p-5 shadow-2xl">
                        <h3 className="text-amber-400 font-black text-lg mb-4 flex items-center gap-2">
                            <AlertCircle className="w-5 h-5" />
                            To Do ({report.issuesByStatus.TODO?.length || 0})
                        </h3>
                        <div className="space-y-2 max-h-100 overflow-y-auto custom-scrollbar">
                            {(report.issuesByStatus.TODO || []).map((issue: any) => (
                                <div key={issue.issueId} className="bg-white/5 rounded-xl p-3 border border-white/5">
                                    <div className="flex items-center gap-2">
                                        {issue.issueCode && <span className="font-mono text-blue-400 text-[10px] font-bold">{issue.issueCode}</span>}
                                        <span className="text-white text-xs font-semibold line-clamp-1">{issue.title}</span>
                                    </div>
                                    <p className="text-slate-600 text-[10px] mt-1">👤 {issue.assignedTo}</p>
                                </div>
                            ))}
                            {(report.issuesByStatus.TODO || []).length === 0 && (
                                <p className="text-slate-600 italic text-xs text-center py-6">Không có Issue nào cần làm</p>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProgressReport;