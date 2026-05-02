import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, ChevronRight } from 'lucide-react';

import apiClient from '../services/apiClient';

const LecturerDashboard = () => {
    const [groups, setGroups] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);

            apiClient.get(`/api/groups/lecturer/${parsedUser.id}`)
                .then(res => {
                    if (res.data.success) {
                        setGroups(res.data.data);
                    }
                })
                .catch(err => console.error(err))
                .finally(() => setLoading(false));
        }
    }, []);

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <h1 className="text-3xl font-black text-white flex items-center gap-3">
                <LayoutDashboard className="text-purple-500" /> Tổng Quan Giảng Viên
            </h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-[#1a1f2e] border border-white/5 p-5 rounded-2xl flex items-center gap-4">
                    <div className="w-12 h-12 bg-purple-600/20 rounded-xl flex items-center justify-center text-purple-400"><Users size={24}/></div>
                    <div>
                        <p className="text-slate-500 text-[10px] font-bold uppercase">Nhóm hướng dẫn</p>
                        <p className="text-3xl font-black text-white">{loading ? '...' : groups.length}</p>
                    </div>
                </div>
            </div>

            <div className="bg-white/[0.02] border border-white/10 rounded-3xl overflow-hidden shadow-2xl">
                <div className="max-h-[400px] overflow-y-auto custom-scrollbar">
                    <table className="w-full text-left border-collapse">
                        <thead className="sticky top-0 bg-[#0a0f1e] z-10 border-b border-white/10">
                        <tr className="text-slate-500 text-[10px] uppercase font-black tracking-widest">
                            <th className="p-5">Mã Nhóm</th>
                            <th className="p-5">Tên Đề Tài</th>
                            <th className="p-5 text-right">Thao Tác</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-white/5">
                        {groups.map((group) => (
                            <tr key={group.id} onClick={() => navigate(`/groups/${group.id}`)} className="hover:bg-white/[0.05] cursor-pointer transition-colors group">
                                <td className="p-5 font-mono font-black text-blue-400">{group.groupId}</td>
                                <td className="p-5 font-bold text-white group-hover:text-purple-400">{group.groupName}</td>
                                <td className="p-5 text-right"><ChevronRight className="inline text-slate-600" /></td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default LecturerDashboard;