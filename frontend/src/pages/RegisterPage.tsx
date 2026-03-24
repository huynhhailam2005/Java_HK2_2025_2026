import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { getApiErrorMessage, register } from '../services/authApi';


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
            setError('Mật khẩu và Nhập lại mật khẩu không khớp.');
            return;
        }

        setIsSubmitting(true);
        setError('');

        try {
            const response = await register({
                username: username.trim(),
                email: email.trim(),
                password,
                role,
            });

            if (!response.success) {
                setError(response.message || 'Dang ky that bai');
                return;
            }

            alert(response.message || 'Dang ky thanh cong');
            navigate('/');
        } catch (err) {
            setError(getApiErrorMessage(err, 'Khong the ket noi den Backend.'));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        //Hiệu ứng chuyển trang
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 1.05 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
            // 🛠️ ĐÃ FIX: Chèn class bg- image và bg-[#050B20] vào dòng dưới này nè
            className="min-h-screen flex items-center justify-center w-full py-10 bg-[#050B20] bg-[url('/login-bg.png')] bg-cover bg-center bg-no-repeat relative"
        >



            {/* TEXT TRANG TRÍ BÊN TRÁI */}
            <div className="absolute left-16 bottom-16 w-96 text-white hidden xl:block z-10">
                <h3 className="text-4xl font-extrabold mb-4 tracking-tight drop-shadow-sm">Tham gia cùng<br/>đội ngũ SRPM</h3>
                <p className="text-base text-slate-300 font-medium leading-relaxed">
                    Tối ưu hóa quy trình quản lý yêu cầu và tiến độ dự án phần mềm. Đồng bộ Jira và GitHub theo thời gian thực.
                </p>
            </div>

            {/*LIQUID GLASS CARD */}
            <div className="w-full max-w-lg relative z-20 mx-4">
                <div className="bg-white/10 backdrop-blur-3xl rounded-[40px] p-8 md:p-10 border border-white/20 shadow-[0_30px_60px_-15px_rgba(30,58,138,0.25)] relative overflow-hidden ring-1 ring-white/30">

                    {/* Inner Shadow Highlight */}
                    <div className="absolute inset-0 rounded-[40px] shadow-[inset_0_2px_4px_rgba(255,255,255,0.6),inset_0_-2px_4px_rgba(0,0,0,0.02)] pointer-events-none z-10"></div>

                    <div className="relative z-30">
                        {/* Header Form */}
                        <div className="flex items-center justify-center gap-3 mb-6">
                            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-cyan-400 shadow-lg shadow-blue-500/30 flex items-center justify-center text-white font-bold">
                                S
                            </div>
                            <h1 className="text-3xl font-extrabold text-white tracking-wide">SRPM</h1>
                        </div>

                        <h2 className="text-2xl font-bold text-white mb-2 text-center">Tạo tài khoản</h2>
                        <p className="text-slate-300 mb-6 font-medium text-center">Điền thông tin của bạn để bắt đầu.</p>

                        <form onSubmit={handleRegister} className="space-y-4">

                            {/* Box Báo Lỗi */}
                            {error && (
                                <div className="bg-red-500/10 backdrop-blur-sm text-red-400 p-4 rounded-2xl border border-red-500/30 flex items-start gap-3 shadow-sm">
                                    <svg className="w-5 h-5 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                                    <span className="text-sm font-medium">{error}</span>
                                </div>
                            )}

                            {/* Input Username */}
                            <div className="space-y-1.5">
                                <label className="text-sm font-semibold text-slate-300 ml-1">Tên đăng nhập</label>
                                <div className="relative group">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
                                    </div>
                                    <input
                                        type="text"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 pl-11 text-white placeholder-slate-500 focus:bg-white/15 focus:outline-none focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 transition-all backdrop-blur-md shadow-[0_2px_10px_rgba(0,0,0,0.02)]"
                                        placeholder="minhtam123"
                                    />
                                </div>
                            </div>

                            {/* Input Role */}
                            <div className="space-y-1.5">
                                <label className="text-sm font-semibold text-slate-300 ml-1">Vai trò</label>
                                <select
                                    value={role}
                                    onChange={(e) => setRole(e.target.value)}
                                    className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 text-white focus:bg-white/15 focus:outline-none focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 transition-all backdrop-blur-md shadow-[0_2px_10px_rgba(0,0,0,0.02)]"
                                >
                                    <option value="STUDENT" className="text-black">Sinh viên</option>
                                    <option value="LECTURER" className="text-black">Giảng viên</option>
                                </select>
                            </div>

                            {/* Input Email */}
                            <div className="space-y-1.5">
                                <label className="text-sm font-semibold text-slate-300 ml-1">Email</label>
                                <div className="relative group">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" /></svg>
                                    </div>
                                    <input
                                        type="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 pl-11 text-white placeholder-slate-500 focus:bg-white/15 focus:outline-none focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 transition-all backdrop-blur-md shadow-[0_2px_10px_rgba(0,0,0,0.02)]"
                                        placeholder="minhtam@uht.edu.vn"
                                    />
                                </div>
                            </div>

                            {/* Grid 2 cột cho Passwords */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {/* Input Mật khẩu */}
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Mật khẩu</label>
                                    <div className="relative group">
                                        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                                        </div>
                                        <input
                                            type="password"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 pl-11 text-white placeholder-slate-500 focus:bg-white/15 focus:outline-none focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 transition-all backdrop-blur-md shadow-[0_2px_10px_rgba(0,0,0,0.02)]"
                                            placeholder="••••••••"
                                        />
                                    </div>
                                </div>

                                {/* Input Xác nhận Mật khẩu */}
                                <div className="space-y-1.5">
                                    <label className="text-sm font-semibold text-slate-300 ml-1">Nhập lại mật khẩu</label>
                                    <div className="relative group">
                                        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500 group-focus-within:text-blue-400 transition-colors">
                                            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
                                        </div>
                                        <input
                                            type="password"
                                            value={confirmPassword}
                                            onChange={(e) => setConfirmPassword(e.target.value)}
                                            className="w-full bg-white/5 hover:bg-white/10 border border-white/10 rounded-2xl p-3.5 pl-11 text-white placeholder-slate-500 focus:bg-white/15 focus:outline-none focus:ring-2 focus:ring-blue-400/50 focus:border-blue-400 transition-all backdrop-blur-md shadow-[0_2px_10px_rgba(0,0,0,0.02)]"
                                            placeholder="••••••••"
                                        />
                                    </div>
                                </div>
                            </div>

                            {/* Điều khoản */}
                            <div className="flex items-start pt-2 ml-1">
                                <input type="checkbox" id="terms" className="w-4 h-4 mt-0.5 rounded border-slate-700 bg-white/5 text-blue-500 focus:ring-blue-500 focus:ring-offset-slate-900" />
                                <label htmlFor="terms" className="ml-2 text-sm text-slate-400 leading-relaxed">
                                    Tôi đồng ý với <a href="#" className="text-blue-400 hover:text-blue-300 transition hover:underline">Điều khoản sử dụng</a> và <a href="#" className="text-blue-400 hover:text-blue-300 transition hover:underline">Chính sách bảo mật</a>.
                                </label>
                            </div>

                            {/* Nút Submit */}
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className="w-full mt-4 bg-gradient-to-r from-blue-600 to-cyan-500 text-white p-4 rounded-2xl font-bold shadow-[0_10px_20px_-10px_rgba(37,99,235,0.6)] hover:shadow-[0_15px_25px_-10px_rgba(37,99,235,0.7)] hover:-translate-y-0.5 active:translate-y-0.5 flex items-center justify-center transition-all duration-300"
                            >
                                {isSubmitting ? 'Dang xu ly...' : 'Tạo tài khoản'}
                            </button>

                            <div className="text-center pt-3">
                                <p className="text-sm text-slate-400 font-medium">
                                    Đã có tài khoản?{' '}
                                    <Link to="/" className="text-blue-400 hover:text-blue-300 transition font-bold hover:underline">
                                        Đăng nhập ngay
                                    </Link>
                                </p>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </motion.div>
    );
};

export default RegisterPage;