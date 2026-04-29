import { useState, useEffect } from 'react';
import { LayoutDashboard, Users, Clock, CheckCircle } from 'lucide-react';
import StatCard from '../components/common/StatCard';
import { adminApi } from '../services/adminApi';

interface ApiGroupDto {
    id: number;
    lecturer?: { id: number };
}

const LecturerDashboard = () => {
    const [user, setUser] = useState<{ id?: number; username?: string } | null>(null);
    const [stats, setStats] = useState({ totalGroups: 0, pendingReviews: 3, activeIssues: 12 });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setUser(parsedUser);

            // Lấy tổng số nhóm mà giảng viên này quản lý
            adminApi.getGroups()
                .then(res => {
                    const allGroups = res.data.data as unknown as ApiGroupDto[];
                    const myGroups = allGroups.filter(g => g.lecturer?.id === parsedUser.id);
                    setStats(prev => ({ ...prev, totalGroups: myGroups.length }));
                })
                .catch(console.error);
        }
    }, []);

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                    <LayoutDashboard className="text-purple-500" /> Tổng Quan Giảng Viên
                </h1>
                <p className="text-slate-400 mt-2">Xin chào, <span className="text-purple-400 font-bold">{user?.username}</span>! Chúc bạn một ngày làm việc hiệu quả.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <StatCard title="Tổng nhóm hướng dẫn" value={stats.totalGroups.toString()} icon={<Users />} colorClass="from-purple-500 to-pink-500" />
            </div>

            <div className="bg-white/5 border border-white/10 rounded-3xl p-6">
                <h2 className="text-xl font-bold text-white mb-4">Hoạt động gần đây</h2>
                <div className="bg-black/20 rounded-2xl p-8 border border-white/5 flex flex-col items-center justify-center text-slate-500">
                    <Clock className="w-12 h-12 mb-3 opacity-20" />
                    <p>Chưa có hoạt động nào nổi bật trong ngày hôm nay.</p>
                </div>
            </div>
        </div>
    );
};

export default LecturerDashboard;