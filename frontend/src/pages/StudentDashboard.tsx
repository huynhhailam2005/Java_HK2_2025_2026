import { FolderKanban, CheckCircle, AlertCircle, Clock } from 'lucide-react';

export default function StudentDashboard() {
    return (
        <div className="p-8 w-full">
            <h1 className="text-3xl font-bold text-white mb-2">Tổng quan Dự án</h1>
            <p className="text-slate-400 mb-8">Chào mừng bạn quay lại! Dưới đây là tiến độ công việc của bạn.</p>

            {/* Khối Thẻ Thống Kê */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">

                {/* Thẻ 1 */}
                <div className="bg-white/5 border border-white/10 p-6 rounded-2xl shadow-lg hover:bg-white/10 transition duration-300">
                    <div className="flex items-center justify-between mb-4">
                        <div className="p-3 bg-blue-500/20 text-blue-400 rounded-xl">
                            <FolderKanban size={24} />
                        </div>
                    </div>
                    <p className="text-sm text-slate-400 font-medium">Dự án đang làm</p>
                    <p className="text-3xl font-bold text-white mt-1">3</p>
                </div>

                {/* Thẻ 2 */}
                <div className="bg-white/5 border border-white/10 p-6 rounded-2xl shadow-lg hover:bg-white/10 transition duration-300">
                    <div className="flex items-center justify-between mb-4">
                        <div className="p-3 bg-green-500/20 text-green-400 rounded-xl">
                            <CheckCircle size={24} />
                        </div>
                    </div>
                    <p className="text-sm text-slate-400 font-medium">Task đã hoàn thành</p>
                    <p className="text-3xl font-bold text-white mt-1">12</p>
                </div>

                {/* Thẻ 3 */}
                <div className="bg-white/5 border border-white/10 p-6 rounded-2xl shadow-lg hover:bg-white/10 transition duration-300">
                    <div className="flex items-center justify-between mb-4">
                        <div className="p-3 bg-yellow-500/20 text-yellow-400 rounded-xl">
                            <Clock size={24} />
                        </div>
                    </div>
                    <p className="text-sm text-slate-400 font-medium">Task đang thực hiện</p>
                    <p className="text-3xl font-bold text-white mt-1">5</p>
                </div>

                {/* Thẻ 4 */}
                <div className="bg-white/5 border border-white/10 p-6 rounded-2xl shadow-lg hover:bg-white/10 transition duration-300">
                    <div className="flex items-center justify-between mb-4">
                        <div className="p-3 bg-red-500/20 text-red-400 rounded-xl">
                            <AlertCircle size={24} />
                        </div>
                    </div>
                    <p className="text-sm text-slate-400 font-medium">Task trễ hạn</p>
                    <p className="text-3xl font-bold text-red-400 mt-1">1</p>
                </div>

            </div>

            {/* Vùng chứa Biểu đồ (Sẽ làm ở bước sau) */}
            <div className="w-full h-64 bg-white/5 border border-white/10 rounded-2xl flex items-center justify-center border-dashed">
                <p className="text-slate-500">Khu vực hiển thị Biểu đồ (Chart.js / Recharts)</p>
            </div>
        </div>
    );
}