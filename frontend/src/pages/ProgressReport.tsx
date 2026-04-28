import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { BarChart3, Download, Github, Trello, RefreshCw, User } from 'lucide-react';
import { reportApi, type GroupProgressReport } from '../services/reportApi';
import { showLiquidToast } from '../utils/toast';

const ProgressReport = () => {
    const [report, setReport] = useState<GroupProgressReport | null>(null);
    const [loading, setLoading] = useState(true);
    const MOCK_GROUP_ID = 1;

    useEffect(() => {
        void fetchReport();
    }, []);

    const fetchReport = async () => {
        setLoading(true);
        try {
            const res = await reportApi.getGroupReport(MOCK_GROUP_ID);
            setReport(res.data.data);
        } catch (error) {
            console.error("Lỗi fetch report:", error);
            // 🔥 Đã xài showLiquidToast ở đây để Linter không la làng
            showLiquidToast('Không kết nối được server, đang tải dữ liệu mẫu!', 'error');

            // Mock data để mày thấy giao diện
            setReport({
                groupId: 1,
                groupName: "Hệ thống Quản lý Đồ án SRPM",
                overallProgress: 65,
                lastSyncJira: "2024-03-20 10:00",
                lastSyncGitHub: "2024-03-20 10:30",
                memberProgress: [
                    { studentId: 1, username: "leader_pro", fullName: "Nguyễn Văn A", completedTasks: 8, totalTasks: 10, commitCount: 45, contributionPercentage: 40 },
                    { studentId: 2, username: "dev_hard", fullName: "Trần Thị B", completedTasks: 5, totalTasks: 10, commitCount: 30, contributionPercentage: 35 },
                    { studentId: 3, username: "tester_chill", fullName: "Lê Văn C", completedTasks: 2, totalTasks: 10, commitCount: 5, contributionPercentage: 25 },
                ]
            });
        } finally {
            setLoading(false);
        }
    };

    // 🔥 Xài showLiquidToast khi bấm nút Xuất PDF
    const handleExportPDF = () => {
        showLiquidToast('Tính năng Xuất PDF đang được hệ thống xử lý...', 'success');
        // Sau này gọi reportApi.exportReport() ở đây
    };

    if (loading) return <div className="p-10 text-white font-medium">Đang tổng hợp dữ liệu báo cáo...</div>;

    return (
        <div className="space-y-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                        <BarChart3 className="text-emerald-400" /> Báo Cáo Tiến Độ
                    </h1>
                    <p className="text-slate-400 mt-2">Phân tích hiệu suất làm việc dựa trên Jira & GitHub</p>
                </div>
                <div className="flex gap-3">
                    <button onClick={() => void fetchReport()} className="p-3 bg-white/5 border border-white/10 rounded-2xl text-slate-300 hover:bg-white/10 transition-all">
                        <RefreshCw className="w-5 h-5" />
                    </button>
                    <button onClick={handleExportPDF} className="bg-emerald-600 hover:bg-emerald-500 text-white px-6 py-3 rounded-2xl font-bold transition-all flex items-center gap-2 shadow-lg shadow-emerald-500/20">
                        <Download className="w-5 h-5" /> Xuất PDF
                    </button>
                </div>
            </div>

            {/* Overall Progress Card */}
            <div className="bg-linear-to-br from-emerald-500/20 to-teal-500/5 border border-emerald-500/20 rounded-4xl p-8 shadow-sm">
                <div className="flex justify-between items-end mb-6">
                    <div>
                        <span className="text-emerald-400 font-bold uppercase tracking-widest text-sm">Tiến độ tổng thể</span>
                        <h2 className="text-5xl font-black text-white mt-1">{report?.overallProgress}%</h2>
                    </div>
                    <div className="text-right text-slate-400 text-xs space-y-1">
                        <p className="flex items-center gap-2 justify-end"><Trello className="w-3 h-3"/> Jira: {report?.lastSyncJira}</p>
                        <p className="flex items-center gap-2 justify-end"><Github className="w-3 h-3"/> GitHub: {report?.lastSyncGitHub}</p>
                    </div>
                </div>
                <div className="w-full h-4 bg-black/40 rounded-full overflow-hidden border border-white/5 relative">
                    <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${report?.overallProgress}%` }}
                        className="h-full bg-linear-to-r from-emerald-500 to-teal-400 shadow-[0_0_20px_rgba(16,185,129,0.5)]"
                    />
                </div>
            </div>

            {/* Member Contributions */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="bg-white/5 border border-white/10 rounded-3xl p-6 shadow-sm">
                    <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                        <User className="text-blue-400" /> Đóng góp thành viên
                    </h3>
                    <div className="space-y-6">
                        {report?.memberProgress.map((member) => (
                            <div key={member.studentId} className="space-y-2">
                                <div className="flex justify-between text-sm">
                                    <span className="text-white font-bold">{member.fullName}</span>
                                    <span className="text-slate-400">{member.contributionPercentage}%</span>
                                </div>
                                <div className="w-full h-2 bg-white/5 rounded-full overflow-hidden">
                                    <motion.div
                                        initial={{ width: 0 }}
                                        animate={{ width: `${member.contributionPercentage}%` }}
                                        className="h-full bg-blue-500"
                                    />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="bg-white/5 border border-white/10 rounded-3xl overflow-hidden shadow-sm">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse min-w-[400px]">
                            <thead className="bg-white/5 text-slate-400 text-xs uppercase font-black">
                            <tr>
                                <th className="p-4">Thành viên</th>
                                <th className="p-4 text-center">Tasks</th>
                                <th className="p-4 text-center">Commits</th>
                            </tr>
                            </thead>
                            <tbody className="text-slate-300">
                            {report?.memberProgress.map((member) => (
                                <tr key={member.studentId} className="border-t border-white/5 hover:bg-white/5 transition-colors">
                                    <td className="p-4 font-bold text-white">{member.username}</td>
                                    <td className="p-4 text-center">
                                        <span className="text-emerald-400">{member.completedTasks}</span>/{member.totalTasks}
                                    </td>
                                    <td className="p-4 text-center">
                                        <div className="flex items-center justify-center gap-1 text-blue-400">
                                            <Github className="w-3 h-3" /> {member.commitCount}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProgressReport;