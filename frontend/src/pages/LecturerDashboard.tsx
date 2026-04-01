import { Users, BookOpen, CheckCircle, Clock, ChevronRight } from 'lucide-react';
// 🛠️ Import khuôn StatCard
import StatCard from '../components/common/StatCard';

export default function LecturerDashboard() {
    return (
        <div className="w-full">
            <h1 className="text-3xl font-bold text-white mb-2">Tổng quan Giảng viên</h1>
            <p className="text-slate-400 mb-8">Theo dõi tiến độ đồ án và quản lý sinh viên của bạn.</p>

            {/* Khối Thẻ Thống Kê - Rút gọn siêu mượt */}
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
                <StatCard
                    title="Lớp/Đồ án đang phụ trách"
                    value="4"
                    icon={<BookOpen size={24} />}
                    colorClass="bg-blue-500/20 text-blue-400"
                />
                <StatCard
                    title="Tổng số Nhóm"
                    value="24"
                    icon={<Users size={24} />}
                    colorClass="bg-purple-500/20 text-purple-400"
                />
                <StatCard
                    title="Bài nộp chờ chấm"
                    value="12"
                    icon={<Clock size={24} />}
                    colorClass="bg-orange-500/20 text-orange-400"
                />
                <StatCard
                    title="Đã hoàn thành đánh giá"
                    value="85%"
                    icon={<CheckCircle size={24} />}
                    colorClass="bg-green-500/20 text-green-400"
                />
            </div>

            {/* Danh sách công việc cần làm ngay */}
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6 shadow-lg">
                <h2 className="text-xl font-bold text-white mb-4">Báo cáo gần đây cần duyệt</h2>

                <div className="space-y-3">
                    <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 transition-colors cursor-pointer group">
                        <div className="flex items-center gap-4">
                            <div className="w-2 h-2 rounded-full bg-orange-400"></div>
                            <div>
                                <h3 className="text-white font-medium">Nhóm 01 - Hệ thống quản lý thư viện</h3>
                                <p className="text-sm text-slate-400">Nộp báo cáo Sprint 2 • 2 giờ trước</p>
                            </div>
                        </div>
                        <button className="text-blue-400 p-2 rounded-lg group-hover:bg-blue-500/20 transition-colors">
                            <ChevronRight size={20} />
                        </button>
                    </div>

                    <div className="flex items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 transition-colors cursor-pointer group">
                        <div className="flex items-center gap-4">
                            <div className="w-2 h-2 rounded-full bg-orange-400"></div>
                            <div>
                                <h3 className="text-white font-medium">Nhóm 05 - App bán hàng đồ thể thao</h3>
                                <p className="text-sm text-slate-400">Nộp tài liệu thiết kế Database • 5 giờ trước</p>
                            </div>
                        </div>
                        <button className="text-blue-400 p-2 rounded-lg group-hover:bg-blue-500/20 transition-colors">
                            <ChevronRight size={20} />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}