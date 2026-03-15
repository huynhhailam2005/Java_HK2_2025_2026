import React, { useState } from 'react';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleLogin = (e: React.FormEvent) => {
        e.preventDefault();
        // Mô phỏng logic kiểm tra lỗi (Display login error)
        if (email === '' || password === '') {
            setError('Vui lòng nhập đầy đủ thông tin đăng nhập.');
        } else {
            setError('');
            alert('Đăng nhập thành công! (Chờ nối API Spring Boot)');
        }
    };

    return (
        <div className="min-h-screen bg-[#0a0f1c] flex font-sans text-slate-300 selection:bg-blue-500/30">
            {/* 🌟 NỬA TRÁI: Hình ảnh trừu tượng & Branding */}
            <div className="hidden lg:flex lg:w-1/2 relative overflow-hidden bg-[#111827] flex-col justify-between p-12 border-r border-slate-800/60">
                {/* Hiệu ứng nền lưới và ánh sáng Neon */}
                <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b_1px,transparent_1px),linear-gradient(to_bottom,#1e293b_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_80%_80%_at_50%_50%,#000_20%,transparent_100%)] opacity-20"></div>
                <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-600/20 rounded-full blur-[120px]"></div>
                <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-emerald-600/10 rounded-full blur-[120px]"></div>

                <div className="relative z-10">
                    <div className="text-3xl font-extrabold text-white tracking-widest flex items-center gap-3">
                        <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-emerald-400 rounded-lg shadow-[0_0_15px_rgba(37,99,235,0.5)]"></div>
                        SRPM
                    </div>
                </div>

                <div className="relative z-10 max-w-md">
                    <h2 className="text-4xl font-bold text-white mb-6 leading-tight">
                        Tối ưu hóa <br />
                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400">quy trình quản lý</span>
                    </h2>
                    <p className="text-slate-400 text-lg leading-relaxed">
                        Hệ thống hỗ trợ quản lý yêu cầu và tiến độ dự án phần mềm. Đồng bộ Jira và GitHub theo thời gian thực.
                    </p>
                </div>
            </div>

            {/* 🔐 NỬA PHẢI: Form đăng nhập */}
            <div className="w-full lg:w-1/2 flex items-center justify-center p-8 relative">
                {/* Ánh sáng hắt nhẹ cho form */}
                <div className="absolute inset-0 bg-gradient-to-b from-blue-900/5 to-transparent pointer-events-none"></div>

                <div className="w-full max-w-md relative z-10">
                    <div className="mb-10 text-center lg:text-left">
                        <h1 className="text-3xl font-bold text-white mb-3">Đăng nhập</h1>
                        <p className="text-slate-400">Vui lòng đăng nhập để truy cập vào hệ thống SRPM.</p>
                    </div>

                    <form onSubmit={handleLogin} className="space-y-6">
                        {/* Hiển thị thông báo lỗi (Dựa theo Use Case Diagram) */}
                        {error && (
                            <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-lg flex items-start gap-3 text-red-400 text-sm">
                                <svg className="w-5 h-5 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                                <p>{error}</p>
                            </div>
                        )}

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-300">Tên đăng nhập / Email</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" /></svg>
                                </div>
                                <input
                                    type="text"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="w-full pl-10 pr-4 py-3 bg-[#111827] border border-slate-700 rounded-lg text-white focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition placeholder-slate-600"
                                    placeholder="admin@uth.edu.vn"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <div className="flex justify-between items-center">
                                <label className="block text-sm font-medium text-slate-300">Mật khẩu</label>
                                <a href="#" className="text-sm text-blue-400 hover:text-blue-300 transition">Quên mật khẩu?</a>
                            </div>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                                </div>
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full pl-10 pr-4 py-3 bg-[#111827] border border-slate-700 rounded-lg text-white focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition placeholder-slate-600"
                                    placeholder="••••••••"
                                />
                            </div>
                        </div>

                        <div className="flex items-center">
                            <input type="checkbox" id="remember" className="w-4 h-4 rounded border-slate-700 bg-[#111827] text-blue-600 focus:ring-blue-500 focus:ring-offset-slate-900" />
                            <label htmlFor="remember" className="ml-2 text-sm text-slate-400">Ghi nhớ đăng nhập</label>
                        </div>

                        <button
                            type="submit"
                            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 px-4 rounded-lg transition duration-300 shadow-[0_0_20px_rgba(37,99,235,0.3)] hover:shadow-[0_0_25px_rgba(37,99,235,0.5)] flex justify-center items-center gap-2"
                        >
                            Đăng nhập hệ thống
                            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;