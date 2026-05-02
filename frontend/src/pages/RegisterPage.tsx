import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { getApiErrorMessage, register } from '../services/authApi';
import { showLiquidToast } from '../utils/toast.ts';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [role, setRole] = useState('STUDENT');
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        if (username.trim() === '' || email.trim() === '' || password === '' || confirmPassword === '') {
            setError('Vui lòng điền đầy đủ thông tin để đăng ký.');
            return;
        }

        if (password !== confirmPassword) {
            showLiquidToast('Mật khẩu không khớp!', 'error');
            setError('Mật khẩu không khớp.');
            return;
        }

        setIsSubmitting(true);
        setError('');

        try {
            const response = await register({
                username: username.trim(),
                email: email.trim(),
                password,
                role
            });

            if (!response.success) {
                showLiquidToast(response.message || 'Đăng ký thất bại', 'error');
                setError(response.message || 'Đăng ký thất bại');
                return;
            }

            // ✅ BẮN TOAST XUYÊN TRANG
            showLiquidToast('Tạo tài khoản thành công! Đang chuyển hướng...', 'success');

            setTimeout(() => {
                navigate('/');
            }, 1500);

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
            className="min-h-screen flex items-center justify-center w-full py-10 bg-[#050B20] bg-[url('/login-bg.png')] bg-cover bg-center bg-no-repeat relative overflow-hidden"
        >
            <div className="absolute inset-0 bg-slate-900/40 z-0"></div>

            <div className="w-full max-w-lg relative z-20 mx-4">
                <div className="bg-white/10 backdrop-blur-3xl rounded-[40px] p-8 md:p-10 border border-white/20 shadow-2xl relative overflow-hidden ring-1 ring-white/30">
                    <div className="absolute inset-0 rounded-[40px] shadow-[inset_0_2px_4px_rgba(255,255,255,0.6)] pointer-events-none z-10"></div>

                    <div className="relative z-30">
                        <div className="flex items-center justify-center gap-3 mb-6">
                            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-400 flex items-center justify-center text-white font-bold">S</div>
                            <h1 className="text-3xl font-extrabold text-white tracking-wide">SRPM</h1>
                        </div>

                        <h2 className="text-2xl font-bold text-white mb-2 text-center">Tạo tài khoản</h2>
                        <p className="text-slate-300 mb-8 text-center">Tham gia quản lý đồ án thông minh hơn.</p>

                        <form onSubmit={handleRegister} className="space-y-4">
                            {error && (
                                <div className="bg-red-500/10 text-red-400 p-4 rounded-2xl border border-red-500/30 text-sm font-medium">
                                    {error}
                                </div>
                            )}

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Tên đăng nhập</label>
                                    <input
                                        type="text" value={username} onChange={(e) => setUsername(e.target.value)}
                                        className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                        placeholder="minhtam123"
                                    />
                                </div>
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Vai trò</label>
                                    <select
                                        value={role} onChange={(e) => setRole(e.target.value)}
                                        className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                    >
                                        <option value="STUDENT" className="text-black">Sinh viên</option>
                                        <option value="LECTURER" className="text-black">Giảng viên</option>
                                    </select>
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <label className="text-sm font-semibold text-slate-300 ml-1">Email</label>
                                <input
                                    type="email" value={email} onChange={(e) => setEmail(e.target.value)}
                                    className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                    placeholder="minhtam@uth.edu.vn"
                                />
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Mật khẩu</label>
                                    <input
                                        type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                                        className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                        placeholder="••••••••"
                                    />
                                </div>
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Nhập lại</label>
                                    <input
                                        type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)}
                                        className="w-full bg-white/5 border border-white/10 rounded-2xl p-3.5 text-white focus:outline-none focus:ring-2 focus:ring-blue-400/50 transition-all backdrop-blur-md"
                                        placeholder="••••••••"
                                    />
                                </div>
                            </div>

                            <button
                                type="submit" disabled={isSubmitting}
                                className="w-full mt-4 bg-linear-to-r from-blue-600 to-cyan-500 text-white p-4 rounded-2xl font-bold shadow-lg hover:-translate-y-0.5 active:translate-y-0.5 transition-all duration-300"
                            >
                                {isSubmitting ? 'Đang xử lý...' : 'Tạo tài khoản ngay'}
                            </button>

                            <p className="text-center text-slate-400 mt-4 text-sm font-medium">
                                Đã có tài khoản? <Link to="/" className="text-blue-400 font-bold hover:underline">Đăng nhập</Link>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </motion.div>
    );
};

export default RegisterPage;