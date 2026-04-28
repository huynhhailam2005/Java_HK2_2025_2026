import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

// 🛠️ Import interface và hàm hú từ file utils
import { type ToastDetail } from './utils/toast';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardLayout from './layouts/DashboardLayout';

import StudentDashboard from './pages/StudentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import LecturerDashboard from './pages/LecturerDashboard';
import ManageProjects from './pages/ManageProjects';
import ProgressReport from './pages/ProgressReport';
import ResourcesPage from './pages/ResourcesPage';
import GradingPage from './pages/GradingPage';

const ComingSoon = ({ title }: { title: string }) => (
    <div className="p-8 w-full h-full flex flex-col items-center justify-center text-center">
        <div className="text-blue-400 mb-4">
            <svg className="w-16 h-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" /></svg>
        </div>
        <h1 className="text-2xl font-bold text-white mb-2">{title}</h1>
        <p className="text-slate-400">Tính năng này đang được nhóm phát triển. Vui lòng quay lại sau!</p>
    </div>
);

function App() {
    const [toastInfo, setToastInfo] = useState<ToastDetail | null>(null);

    useEffect(() => {
        const handleToast = (e: Event) => {
            const customEvent = e as CustomEvent<ToastDetail>;
            setToastInfo(customEvent.detail);
            setTimeout(() => setToastInfo(null), 2000);
        };
        window.addEventListener('show-liquid-toast', handleToast);
        return () => window.removeEventListener('show-liquid-toast', handleToast);
    }, []);

    return (
        <BrowserRouter>
            <AnimatePresence>
                {toastInfo && (
                    <motion.div
                        initial={{ opacity: 0, x: 100, scale: 0.5, filter: 'blur(10px)' }}
                        animate={{ opacity: 1, x: 0, scale: 1, filter: 'blur(0px)' }}
                        exit={{ opacity: 0, x: 50, scale: 0.9, filter: 'blur(10px)', transition: { duration: 0.5 } }}
                        transition={{ type: "spring", stiffness: 200, damping: 25 }}
                        className="fixed top-8 right-8 z-9999 min-w-[340px] bg-white/10 backdrop-blur-3xl border border-white/20 shadow-[0_25px_50px_-12px_rgba(0,0,0,0.5)] rounded-2xl p-5 flex items-center gap-4 overflow-hidden"
                    >
                        <motion.div
                            initial={{ x: '-100%' }}
                            animate={{ x: '250%' }}
                            transition={{ duration: 2, repeat: Infinity, repeatDelay: 1 }}
                            className="absolute inset-0 w-1/2 bg-gradient-to-r from-transparent via-white/10 to-transparent skew-x-[-25deg] pointer-events-none"
                        />
                        <motion.div
                            initial={{ width: "100%" }}
                            animate={{ width: "0%" }}
                            transition={{ duration: 2, ease: "linear" }}
                            className={`absolute bottom-0 left-0 h-1 ${toastInfo.type === 'success' ? 'bg-cyan-400' : 'bg-red-500'} opacity-50`}
                        />
                        <div className={`absolute left-0 top-0 bottom-0 w-1.5 ${toastInfo.type === 'success' ? 'bg-cyan-400 shadow-[0_0_15px_#22d3ee]' : 'bg-red-500 shadow-[0_0_15px_#ef4444]'}`}></div>
                        <div className={`relative z-10 w-12 h-12 rounded-xl flex items-center justify-center backdrop-blur-md border ${toastInfo.type === 'success' ? 'bg-cyan-400/20 border-cyan-400/30 text-cyan-300' : 'bg-red-500/20 border-red-500/30 text-red-300'}`}>
                            {toastInfo.type === 'success' ? (
                                <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" /></svg>
                            ) : (
                                <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" /></svg>
                            )}
                        </div>
                        <div className="relative z-10 flex-1">
                            <h4 className="text-white font-extrabold text-[12px] tracking-[0.15em] uppercase opacity-90">
                                {toastInfo.type === 'success' ? 'System Notification' : 'System Alert'}
                            </h4>
                            <p className="text-slate-200 text-sm mt-1 font-medium leading-tight">{toastInfo.message}</p>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route element={<DashboardLayout />}>
                    {/* 🔥 Tuyến chung (Báo cáo) */}
                    <Route path="/dashboard/report" element={<ProgressReport />} />

                    {/* Tuyến Sinh Viên */}
                    <Route path="/dashboard/student" element={<StudentDashboard />} />
                    <Route path="/projects" element={<ComingSoon title="Dự án của tôi" />} />
                    <Route path="/tasks" element={<ComingSoon title="Bảng Công việc (Kanban)" />} />
                    <Route path="/resources" element={<ResourcesPage />} />
                    <Route path="/team" element={<ComingSoon title="Thành viên nhóm" />} />

                    {/* Tuyến Giảng Viên */}
                    <Route path="/dashboard/lecturer" element={<LecturerDashboard />} />
                    <Route path="/manage-projects" element={<ManageProjects />} />
                    <Route path="/grading" element={<GradingPage />} />
                    <Route path="/students-list" element={<ComingSoon title="Danh sách Sinh viên" />} />

                    {/* Tuyến Admin */}
                    <Route path="/dashboard/admin" element={<AdminDashboard />} />
                    <Route path="/manage-users" element={<ComingSoon title="Quản lý Người dùng" />} />
                    <Route path="/system-settings" element={<ComingSoon title="Cài đặt Hệ thống" />} />
                </Route>
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;