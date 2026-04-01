import { Users, ShieldAlert, Activity, Database } from 'lucide-react';
// 🛠️ Thêm dòng này để lôi cái khuôn ra xài
import StatCard from '../components/common/StatCard';

export default function AdminDashboard() {
    return (
        <div className="w-full">
            <h1 className="text-3xl font-bold text-white mb-2">Quản trị Hệ thống</h1>
            <p className="text-slate-400 mb-8">Trang tổng quan dành riêng cho Admin. Theo dõi tài nguyên và bảo mật hệ thống.</p>

            {/* Khối Thẻ Thống Kê - BÂY GIỜ NGẮN GỌN NHƯ THẾ NÀY ĐÂY! */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
                <StatCard
                    title="Tổng Người dùng"
                    value="1,248"
                    icon={<Users size={24} />}
                    colorClass="bg-purple-500/20 text-purple-400"
                />
                <StatCard
                    title="Dung lượng Database"
                    value="45.2 GB"
                    icon={<Database size={24} />}
                    colorClass="bg-blue-500/20 text-blue-400"
                />
                <StatCard
                    title="Uptime Hệ thống"
                    value="99.9%"
                    icon={<Activity size={24} />}
                    colorClass="bg-green-500/20 text-green-400"
                />
                <StatCard
                    title="Cảnh báo rủi ro"
                    value="0"
                    icon={<ShieldAlert size={24} />}
                    colorClass="bg-red-500/20 text-red-400"
                />
            </div>

            {/* Vùng chứa Biểu đồ */}
            <div className="w-full h-64 bg-white/5 border border-white/10 rounded-2xl flex items-center justify-center border-dashed">
                <p className="text-slate-500">Khu vực hiển thị Biểu đồ Lưu lượng truy cập (Traffic)</p>
            </div>
        </div>
    );
}