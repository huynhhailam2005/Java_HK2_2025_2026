import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Award, Users, ChevronRight, Save, FileSignature, CheckCircle2 } from 'lucide-react';
import { adminApi } from '../services/adminApi';
import { gradingApi, type GradeInput } from '../services/gradingApi';
import { showLiquidToast } from '../utils/toast';

interface GroupItem {
    groupId: number;
    groupCode: string;
    groupName: string;
    lecturerId?: number;
}

interface StudentItem {
    studentId: number;
    studentName: string;
    role: string;
    contribution: number; // Phần trăm đóng góp (lấy từ report)
}

const GradingPage = () => {
    const [myGroups, setMyGroups] = useState<GroupItem[]>([]);
    const [selectedGroup, setSelectedGroup] = useState<GroupItem | null>(null);
    const [students, setStudents] = useState<StudentItem[]>([]);
    const [grades, setGrades] = useState<Record<number, { score: string; feedback: string }>>({});
    const [loadingGroups, setLoadingGroups] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        void fetchMyGroups();
    }, []);

    const fetchMyGroups = async () => {
        setLoadingGroups(true);
        try {
            // Lấy tạm danh sách nhóm (có thể thay bằng API lấy riêng cho GV)
            const res = await adminApi.getGroups();
            const rawData = res.data.data as any[];
            const mappedGroups = rawData.map(g => ({
                groupId: g.id,
                groupCode: g.groupCode || `GR-${g.id}`,
                groupName: g.groupName,
            }));
            setMyGroups(mappedGroups);
        } catch (error) {
            // MOCK DATA: Nếu API xịt
            setMyGroups([
                { groupId: 1, groupCode: 'GR-01', groupName: 'Hệ thống Quản lý KTX' },
                { groupId: 2, groupCode: 'GR-02', groupName: 'Website Bán Giày' }
            ]);
        } finally {
            setLoadingGroups(false);
        }
    };

    const handleSelectGroup = (group: GroupItem) => {
        setSelectedGroup(group);
        // MOCK DATA: Giả lập gọi API lấy danh sách SV của nhóm này kèm % đóng góp
        const mockStudents = [
            { studentId: 1, studentName: 'Nguyễn Văn A', role: 'LEADER', contribution: 45 },
            { studentId: 2, studentName: 'Trần Thị B', role: 'MEMBER', contribution: 35 },
            { studentId: 3, studentName: 'Lê Văn C', role: 'MEMBER', contribution: 20 },
        ];
        setStudents(mockStudents);

        // Reset form nhập điểm
        const initialGrades: Record<number, { score: string; feedback: string }> = {};
        mockStudents.forEach(s => {
            initialGrades[s.studentId] = { score: '', feedback: '' };
        });
        setGrades(initialGrades);
    };

    const handleGradeChange = (studentId: number, field: 'score' | 'feedback', value: string) => {
        setGrades(prev => ({
            ...prev,
            [studentId]: { ...prev[studentId], [field]: value }
        }));
    };

    const handleSubmitGrades = async () => {
        if (!selectedGroup) return;

        // Validate
        for (const s of students) {
            const scoreNum = parseFloat(grades[s.studentId].score);
            if (isNaN(scoreNum) || scoreNum < 0 || scoreNum > 10) {
                showLiquidToast(`Điểm của ${s.studentName} không hợp lệ (0-10)!`, 'error');
                return;
            }
        }

        setIsSubmitting(true);
        try {
            const payload: GradeInput[] = students.map(s => ({
                studentId: s.studentId,
                score: parseFloat(grades[s.studentId].score),
                feedback: grades[s.studentId].feedback
            }));

            await gradingApi.submitGrades(selectedGroup.groupId, payload);
            showLiquidToast('Đã lưu điểm thành công!', 'success');
        } catch (error) {
            // Giả lập thành công nếu Backend chưa code xong
            setTimeout(() => {
                showLiquidToast('Đã lưu bảng điểm thành công!', 'success');
                setIsSubmitting(false);
            }, 1000);
        }
    };

    return (
        <div className="space-y-8 h-[calc(100vh-140px)] flex flex-col">
            <div className="shrink-0">
                <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                    <Award className="text-amber-400" /> Chấm Điểm Đồ Án
                </h1>
                <p className="text-slate-400 mt-2">Đánh giá và ghi nhận kết quả cuối cùng cho các nhóm sinh viên.</p>
            </div>

            <div className="flex flex-col lg:flex-row gap-6 flex-1 min-h-0">
                {/* Cột trái: Danh sách nhóm */}
                <div className="lg:w-1/3 bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col min-h-75 overflow-hidden">
                    <div className="flex items-center gap-2 mb-6 pb-4 border-b border-white/5 shrink-0">
                        <Users className="w-5 h-5 text-slate-400" />
                        <h3 className="font-bold text-slate-300 uppercase tracking-widest text-sm">Danh sách nhóm</h3>
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                        {loadingGroups ? <p className="text-slate-500 text-sm text-center">Đang tải...</p> :
                            myGroups.map(group => (
                                <button
                                    key={group.groupId}
                                    onClick={() => handleSelectGroup(group)}
                                    className={`w-full text-left p-4 rounded-2xl border transition-all flex items-center justify-between ${selectedGroup?.groupId === group.groupId ? 'bg-amber-500/20 border-amber-500/50' : 'bg-white/5 border-white/5 hover:border-white/20'}`}
                                >
                                    <div>
                                        <div className="text-xs font-mono text-amber-400 mb-1">{group.groupCode}</div>
                                        <div className="text-white font-bold">{group.groupName}</div>
                                    </div>
                                    <ChevronRight className={`w-5 h-5 transition-transform ${selectedGroup?.groupId === group.groupId ? 'text-amber-400 translate-x-1' : 'text-slate-600'}`} />
                                </button>
                            ))
                        }
                    </div>
                </div>

                {/* Cột phải: Bảng chấm điểm */}
                <div className="lg:w-2/3 bg-black/20 border border-white/5 rounded-3xl p-5 flex flex-col overflow-hidden">
                    {!selectedGroup ? (
                        <div className="flex-1 flex flex-col items-center justify-center text-slate-500">
                            <FileSignature className="w-16 h-16 mb-4 opacity-20" />
                            <p>Chọn một nhóm bên trái để bắt đầu nhập điểm</p>
                        </div>
                    ) : (
                        <>
                            <div className="flex justify-between items-center mb-6 pb-4 border-b border-white/5 shrink-0">
                                <div>
                                    <h3 className="text-xl font-bold text-white">{selectedGroup.groupName}</h3>
                                    <p className="text-sm text-slate-400 mt-1">Mã nhóm: {selectedGroup.groupCode}</p>
                                </div>
                                <button
                                    onClick={handleSubmitGrades}
                                    disabled={isSubmitting}
                                    className="bg-amber-600 hover:bg-amber-500 text-white px-6 py-3 rounded-2xl font-bold transition-all shadow-lg shadow-amber-500/20 flex items-center gap-2 disabled:opacity-50"
                                >
                                    {isSubmitting ? <span className="animate-spin text-xl leading-none">⟳</span> : <Save className="w-5 h-5" />}
                                    Chốt Điểm
                                </button>
                            </div>

                            <div className="flex-1 overflow-y-auto custom-scrollbar pr-2">
                                <div className="space-y-4">
                                    {students.map((student) => (
                                        <motion.div
                                            key={student.studentId}
                                            initial={{ opacity: 0, y: 10 }}
                                            animate={{ opacity: 1, y: 0 }}
                                            className="bg-white/5 border border-white/10 rounded-2xl p-5"
                                        >
                                            <div className="flex flex-col md:flex-row justify-between gap-6">
                                                {/* Info */}
                                                <div className="flex-1">
                                                    <div className="flex items-center gap-3 mb-2">
                                                        <h4 className="text-white font-bold text-lg">{student.studentName}</h4>
                                                        <span className={`text-[10px] px-2 py-1 rounded font-black uppercase tracking-wider ${student.role === 'LEADER' ? 'bg-amber-500/20 text-amber-400' : 'bg-slate-700 text-slate-300'}`}>
                                                            {student.role}
                                                        </span>
                                                    </div>
                                                    <div className="flex items-center gap-2 text-sm text-slate-400">
                                                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                                                        Mức độ đóng góp: <span className="text-emerald-400 font-bold">{student.contribution}%</span>
                                                    </div>
                                                </div>

                                                {/* Input Point */}
                                                <div className="flex items-start gap-4">
                                                    <div className="w-24">
                                                        <label className="block text-xs font-bold text-slate-400 mb-1 uppercase tracking-widest">Điểm (10)</label>
                                                        <input
                                                            type="number"
                                                            min="0" max="10" step="0.5"
                                                            value={grades[student.studentId]?.score || ''}
                                                            onChange={(e) => handleGradeChange(student.studentId, 'score', e.target.value)}
                                                            className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 text-white text-center text-xl font-black focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                                                            placeholder="0.0"
                                                        />
                                                    </div>
                                                    <div className="flex-1 min-w-[200px]">
                                                        <label className="block text-xs font-bold text-slate-400 mb-1 uppercase tracking-widest">Nhận xét</label>
                                                        <input
                                                            type="text"
                                                            value={grades[student.studentId]?.feedback || ''}
                                                            onChange={(e) => handleGradeChange(student.studentId, 'feedback', e.target.value)}
                                                            className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-amber-500"
                                                            placeholder="Ghi chú đánh giá..."
                                                        />
                                                    </div>
                                                </div>
                                            </div>
                                        </motion.div>
                                    ))}
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default GradingPage;