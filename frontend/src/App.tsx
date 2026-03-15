import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
// CHUYỂN ẢNH NỀN RA ĐÂY
import loginBg from './assets/login-bg.png';

function AnimatedRoutes() {
    const location = useLocation();
    return (
        <AnimatePresence mode="wait">
            <Routes location={location} key={location.pathname}>
                <Route path="/" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
            </Routes>
        </AnimatePresence>
    );
}

function App() {
    return (
        // BỌC TOÀN BỘ APP BẰNG CÁI NỀN TĨNH NÀY
        <div
            className="relative min-h-screen bg-cover bg-center overflow-hidden font-sans selection:bg-blue-500/30"
            style={{ backgroundImage: `url(${loginBg})` }}
        >
            {/* Lớp phủ tối  */}
            <div className="absolute inset-0 bg-[#050B20]/60 z-0 pointer-events-none"></div>

            {/* Nội dung các trang sẽ chạy hiệu ứng trên lớp nền này */}
            <div className="relative z-10">
                <BrowserRouter>
                    <AnimatedRoutes />
                </BrowserRouter>
            </div>
        </div>
    );
}

export default App;