import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { getApiErrorMessage, login } from '../services/authApi';
// 🛠️ Import hàm hú Toast từ App
import { showLiquidToast } from '../utils/toast.ts';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        if (email.trim() === '' || password === '') {
            setError('Vui lòng nhập đầy đủ thông tin đăng nhập.');
            return;
        }

        // CHEAT CODE DÀNH CHO ADMIN TEST
        if (email.trim() === 'admin' && password === '123') {
            const fakeAdmin = { username: 'Trùm Cuối', role: 'ADMIN' };
            localStorage.setItem('user', JSON.stringify(fakeAdmin));

            showLiquidToast('Đăng nhập ẩn danh: ADMIN', 'success');
            setTimeout(() => navigate('/dashboard/admin'), 800);
            return;
        }

        setIsSubmitting(true);
        setError('');

        try {
            const response = await login({ username: email.trim(), password });

            if (!response.success) {
                showLiquidToast(response.message || 'Đăng nhập thất bại', 'error');
                setError(response.message || 'Đăng nhập thất bại');
                setIsSubmitting(false);
                return;
            }

            localStorage.setItem('user', JSON.stringify(response.data));

            // ✅ BẮN TOAST (Nó sẽ sống xuyên qua quá trình chuyển trang)
            showLiquidToast('Đăng nhập thành công! Chào mừng trở lại.', 'success');

            const userData = response.data as { role: string; username: string };
            const userRole = userData.role;

            // Chuyển trang sau 0.8s để user kịp thấy hiệu ứng nảy của Toast
            setTimeout(() => {
                if (userRole === 'LECTURER') navigate('/dashboard/lecturer');
                else if (userRole === 'ADMIN') navigate('/dashboard/admin');
                else navigate('/dashboard/student');
            }, 700);

        } catch (err) {
            const errorMsg = getApiErrorMessage(err, 'Không thể kết nối đến Backend.');
            showLiquidToast(errorMsg, 'error');
            setError(errorMsg);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 1.05 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
            className="min-h-screen flex items-center justify-center w-full py-10 bg-[#050B20] bg-[url('/login-bg.png')] bg-cover bg-center bg-no-repeat relative"
        >
            <div className="absolute inset-0 bg-slate-900/40 z-0"></div>

            <div className="absolute left-16 bottom-16 w-96 text-white hidden xl:block z-10">
                <h3 className="text-4xl font-extrabold mb-4 tracking-tight drop-shadow-sm">Tối ưu hóa<br/>quy trình quản lý</h3>
                <p className="text-base text-slate-300 font-medium leading-relaxed">
                    Hệ thống hỗ trợ quản lý yêu cầu và tiến độ dự án phần mềm. Đồng bộ Jira và GitHub theo thời gian thực.
                </p>
            </div>

            <div className="w-full max-w-md relative z-20 mx-4">
                <div className="bg-white/10 backdrop-blur-3xl rounded-[40px] p-10 border border-white/20 shadow-[0_30px_60px_-15px_rgba(30,58,138,0.25)] relative overflow-hidden ring-1 ring-white/30">
                    <div className="absolute inset-0 rounded-[40px] shadow-[inset_0_2px_4px_rgba(255,255,255,0.6),inset_0_-2px_4px_rgba(0,0,0,0.02)] pointer-events-none z-10"></div>
                    <div className="relative z-30">
                        <div className="flex items-center justify-center gap-3 mb-8">
                            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-400 shadow-lg shadow-blue-500/30 flex items-center justify-center text-white font-bold">S</div>
                            <h1 className="text-3xl font-extrabold text-white tracking-wide">SRPM</h1>
                        </div>
                        <h2 className="text-2xl font-bold text-white mb-2">Đăng nhập</h2>
                        <form onSubmit={handleLogin} className="space-y-5">
                            {error && (
                                <div className="bg-red-500/10 backdrop-blur-sm text-red-400 p-4 rounded-2xl border border-red-500/30 flex items-start gap-3">
                                    <span className="text-sm font-medium">{error}</span>
                                </div>
                            )}
                            <div className="space-y-1.5">
                                <label className="text-sm font-semibold text-slate-300 ml-1">Tên đăng nhập / Email</label>
                                <input
                                    type="text" value={email} onChange={(e) => setEmail(e.target.value)}
                                    className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                    placeholder="admin@uth.edu.vn"
                                />
                            </div>
                            <div className="space-y-1.5">
                                <label className="block text-sm font-semibold text-slate-300 ml-1">Mật khẩu</label>
                                <input
                                    type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                                    className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                    placeholder="••••••••"
                                />
                            </div>
                            <button type="submit" disabled={isSubmitting} className="w-full mt-2 bg-gradient-to-r from-blue-600 to-cyan-500 text-white p-4 rounded-2xl font-bold shadow-lg hover:-translate-y-0.5 active:translate-y-0.5 transition-all duration-300">
                                {isSubmitting ? 'Đang xử lý...' : 'Đăng nhập hệ thống'}
                            </button>
                            <div className="text-center pt-2">
                                <p className="text-sm text-slate-400 font-medium">Chưa có tài khoản? <Link to="/register" className="text-blue-400 hover:underline font-bold">Đăng ký ngay</Link></p>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </motion.div>
    );
};

export default LoginPage;