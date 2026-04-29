import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

import { type ToastDetail } from './utils/toast';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardLayout from './layouts/DashboardLayout';

import StudentDashboard from './pages/StudentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import LecturerDashboard from './pages/LecturerDashboard';
import GroupList from './pages/GroupList';
import GroupWorkspace from './pages/GroupWorkspace';

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
                        <motion.div initial={{ x: '-100%' }} animate={{ x: '250%' }} transition={{ duration: 2, repeat: Infinity, repeatDelay: 1 }} className="absolute inset-0 w-1/2 bg-gradient-to-r from-transparent via-white/10 to-transparent skew-x-[-25deg] pointer-events-none" />
                        <motion.div initial={{ width: "100%" }} animate={{ width: "0%" }} transition={{ duration: 2, ease: "linear" }} className={`absolute bottom-0 left-0 h-1 ${toastInfo.type === 'success' ? 'bg-cyan-400' : 'bg-red-500'} opacity-50`} />
                        <div className={`absolute left-0 top-0 bottom-0 w-1.5 ${toastInfo.type === 'success' ? 'bg-cyan-400 shadow-[0_0_15px_#22d3ee]' : 'bg-red-500 shadow-[0_0_15px_#ef4444]'}`}></div>
                        <div className={`relative z-10 w-12 h-12 rounded-xl flex items-center justify-center backdrop-blur-md border ${toastInfo.type === 'success' ? 'bg-cyan-400/20 border-cyan-400/30 text-cyan-300' : 'bg-red-500/20 border-red-500/30 text-red-300'}`}>
                            {toastInfo.type === 'success' ? <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" /></svg> : <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" /></svg>}
                        </div>
                        <div className="relative z-10 flex-1">
                            <h4 className="text-white font-extrabold text-[12px] tracking-[0.15em] uppercase opacity-90">{toastInfo.type === 'success' ? 'System Notification' : 'System Alert'}</h4>
                            <p className="text-slate-200 text-sm mt-1 font-medium leading-tight">{toastInfo.message}</p>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>

            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route element={<DashboardLayout />}>

                    {/* Tuyến chung cho SV & GV */}
                    <Route path="/groups" element={<GroupList />} />
                    <Route path="/groups/:groupId" element={<GroupWorkspace />} />

                    {/* Tuyến Sinh Viên */}
                    <Route path="/dashboard/student" element={<StudentDashboard />} />

                    {/* Tuyến Giảng Viên */}
                    <Route path="/dashboard/lecturer" element={<LecturerDashboard />} />

                    {/* Tuyến Admin */}
                    <Route path="/dashboard/admin" element={<AdminDashboard />} />

                </Route>
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;