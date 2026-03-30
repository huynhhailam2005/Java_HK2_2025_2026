import { FolderKanban, CheckCircle, AlertCircle, Clock } from 'lucide-react';
// 🛠️ Import cái khuôn Lego StatCard xịn sò
import StatCard from '../components/common/StatCard';

export default function StudentDashboard() {
    return (
        <div className="w-full">
            <h1 className="text-3xl font-bold text-white mb-2">Tổng quan Dự án</h1>
            <p className="text-slate-400 mb-8">Chào mừng bạn quay lại! Dưới đây là tiến độ công việc của bạn.</p>

            {/* Khối Thẻ Thống Kê - Đã đập đi xây lại bằng Component */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">

                <StatCard
                    title="Dự án đang làm"
                    value="3"
                    icon={<FolderKanban size={24} />}
                    colorClass="bg-blue-500/20 text-blue-400"
                />

                <StatCard
                    title="Task đã hoàn thành"
                    value="12"
                    icon={<CheckCircle size={24} />}
                    colorClass="bg-green-500/20 text-green-400"
                />

                <StatCard
                    title="Task đang thực hiện"
                    value="5"
                    icon={<Clock size={24} />}
                    colorClass="bg-yellow-500/20 text-yellow-400"
                />

                <StatCard
                    title="Task trễ hạn"
                    value="1"
                    icon={<AlertCircle size={24} />}
                    colorClass="bg-red-500/20 text-red-400"
                />

            </div>

            {/* Vùng chứa Biểu đồ */}
            <div className="w-full h-64 bg-white/5 border border-white/10 rounded-2xl flex items-center justify-center border-dashed">
                <p className="text-slate-500">Khu vực hiển thị Biểu đồ (Chart.js / Recharts)</p>
            </div>
        </div>
    );
}