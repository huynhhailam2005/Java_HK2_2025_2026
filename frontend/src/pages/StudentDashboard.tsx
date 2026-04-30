import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { LayoutDashboard, CheckSquare, Clock, AlertCircle, FolderKanban } from 'lucide-react';
import StatCard from '../components/common/StatCard';

interface Task {
    id: string;
    title: string;
    groupName: string; // THÊM TRƯỜNG NÀY ĐỂ BIẾT TASK CỦA NHÓM NÀO
    deadline: string;
    status: 'TODO' | 'IN_PROGRESS' | 'DONE';
}

const StudentDashboard = () => {
    const [tasks, setTasks] = useState<Task[]>([]);

    // Đọc tên để chào
    const userDataStr = localStorage.getItem('user');
    const userName = userDataStr ? JSON.parse(userDataStr).username : 'Sinh Viên';

    useEffect(() => {
        // Mock data
        setTasks([
            { id: '1', title: 'Thiết kế Database hệ thống', groupName: 'Hệ thống Quản lý KTX', deadline: '20/03/2024', status: 'TODO' },
            { id: '2', title: 'Code API Đăng nhập', groupName: 'Hệ thống Quản lý KTX', deadline: '25/03/2024', status: 'IN_PROGRESS' },
            { id: '3', title: 'Viết báo cáo chương 1', groupName: 'Đồ án Phân tích Thiết kế', deadline: '15/03/2024', status: 'DONE' },
            { id: '4', title: 'Test UI màn hình chính', groupName: 'App Quản Lý Thư Viện', deadline: '22/03/2024', status: 'TODO' },
        ]);
    }, []);

    const columns = [
        { id: 'TODO', title: 'Cần làm', icon: <AlertCircle size={18} className="text-slate-400" />, color: 'border-slate-500', bg: 'bg-slate-500/10' },
        { id: 'IN_PROGRESS', title: 'Đang làm', icon: <Clock size={18} className="text-blue-400" />, color: 'border-blue-500', bg: 'bg-blue-500/10' },
        { id: 'DONE', title: 'Đã xong', icon: <CheckSquare size={18} className="text-emerald-400" />, color: 'border-emerald-500', bg: 'bg-emerald-500/10' }
    ];

    return (
        <div className="space-y-8 w-full">
            <div>
                <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                    <LayoutDashboard className="text-blue-400" /> Xin chào, {userName}!
                </h1>
                <p className="text-slate-400 mt-2">Tổng quan các công việc bạn được giao từ tất cả các nhóm đồ án.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <StatCard title="Tổng dự án tham gia" value="3" icon={<FolderKanban />} colorClass="from-purple-500 to-pink-500" />
                <StatCard title="Việc cần hoàn thành" value={tasks.filter(t => t.status !== 'DONE').length.toString()} icon={<AlertCircle />} colorClass="from-amber-500 to-orange-500" />
                <StatCard title="Việc đã xong" value={tasks.filter(t => t.status === 'DONE').length.toString()} icon={<CheckSquare />} colorClass="from-emerald-500 to-teal-400" />
            </div>

            <div className="bg-white/5 border border-white/10 rounded-3xl p-6">
                <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
                    <CheckSquare className="text-blue-400" /> Bảng Công Việc Tổng Hợp
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
                    {columns.map(col => (
                        <div key={col.id} className={`bg-black/20 border-t-2 ${col.color} rounded-2xl p-4 min-h-64`}>
                            <div className="flex items-center justify-between mb-4">
                                <h4 className="text-white font-bold flex items-center gap-2">{col.icon} {col.title}</h4>
                                <span className={`text-xs font-bold px-2 py-1 rounded-lg ${col.bg} ${col.color.replace('border-', 'text-')}`}>
                                    {tasks.filter(t => t.status === col.id).length}
                                </span>
                            </div>

                            <div className="space-y-3">
                                {tasks.filter(t => t.status === col.id).map((task, idx) => (
                                    <motion.div
                                        key={task.id}
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        transition={{ delay: idx * 0.1 }}
                                        className="bg-white/5 border border-white/5 p-4 rounded-xl hover:bg-white/10 transition-colors"
                                    >
                                        <h5 className="text-white font-bold text-sm mb-2 leading-snug">{task.title}</h5>

                                        {/* 🔥 HIỂN THỊ TÊN NHÓM CỰC KỲ RÕ RÀNG */}
                                        <div className="flex items-center gap-1.5 mb-3 bg-purple-500/10 border border-purple-500/20 w-fit px-2 py-1 rounded-md">
                                            <FolderKanban size={12} className="text-purple-400" />
                                            <span className="text-[10px] font-bold text-purple-400 uppercase tracking-wider">{task.groupName}</span>
                                        </div>

                                        <div className="flex items-center gap-2 text-xs text-slate-400 font-mono">
                                            <Clock size={12} /> Hạn chót: {task.deadline}
                                        </div>
                                    </motion.div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default StudentDashboard;