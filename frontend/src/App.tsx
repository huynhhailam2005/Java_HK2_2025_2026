import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardLayout from './layouts/DashboardLayout';
import StudentDashboard from './pages/StudentDashboard'; // Import trang thật vừa tạo

// Component hiển thị cho những trang chưa code tới
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
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                <Route element={<DashboardLayout />}>
                    {/* TRANG THẬT CỦA SINH VIÊN */}
                    <Route path="/dashboard/student" element={<StudentDashboard />} />

                    {/* CÁC TRANG ĐANG XÂY DỰNG */}
                    <Route path="/projects" element={<ComingSoon title="Dự án của tôi" />} />
                    <Route path="/tasks" element={<ComingSoon title="Bảng Công việc (Kanban)" />} />
                    <Route path="/resources" element={<ComingSoon title="Tài nguyên & GitHub" />} />
                    <Route path="/team" element={<ComingSoon title="Thành viên nhóm" />} />

                    <Route path="/dashboard/lecturer" element={<ComingSoon title="Tổng quan Giảng viên" />} />
                    <Route path="/manage-projects" element={<ComingSoon title="Quản lý Lớp & Đồ án" />} />
                    <Route path="/grading" element={<ComingSoon title="Chấm điểm" />} />
                    <Route path="/students-list" element={<ComingSoon title="Danh sách Sinh viên" />} />

                    <Route path="/dashboard/admin" element={<ComingSoon title="Tổng quan Admin" />} />
                    <Route path="/manage-users" element={<ComingSoon title="Quản lý Người dùng" />} />
                    <Route path="/system-settings" element={<ComingSoon title="Cài đặt Hệ thống" />} />
                    <Route path="/settings" element={<ComingSoon title="Cài đặt cá nhân" />} />
                </Route>

                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;