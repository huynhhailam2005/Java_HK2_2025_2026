import { Outlet, useNavigate } from 'react-router-dom';
import { LogOut, Menu, Lock, User, ChevronDown } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import ChangePasswordModal from '../components/ChangePasswordModal';
import ProfileModal from '../components/ProfileModal';

export default function DashboardLayout() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();

    const [currentUser] = useState(() => {
        const data = localStorage.getItem('user');
        try {
            return data ? JSON.parse(data) : null;
        } catch (e) {
            return null;
        }
    });

    useEffect(() => {
        if (!currentUser) navigate('/');
    }, [currentUser, navigate]);

    // Click outside để đóng dropdown
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    if (!currentUser) return null;

    const hideSidebar = true;

    return (
        <div className="min-h-screen bg-[#050B20] text-white font-sans overflow-hidden flex flex-col">
            <header className="h-16 w-full border-b border-white/10 bg-white/5 backdrop-blur-md flex items-center justify-between px-6 z-40 shrink-0">
                <div className="flex items-center gap-4 w-64">
                    {!hideSidebar && (
                        <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="p-2 hover:bg-white/10 rounded-lg transition">
                            <Menu size={24} />
                        </button>
                    )}
                    <div className="font-bold text-xl tracking-wider text-blue-400 italic">SRPM</div>
                </div>

                <div className="flex items-center gap-4">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-2 text-slate-400 hover:text-red-400 transition-all font-bold text-xs bg-white/5 px-4 py-2 rounded-xl border border-white/5"
                    >
                        <LogOut size={14}/> Đăng xuất
                    </button>

                    <div className="relative" ref={dropdownRef}>
                        <button
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                            className="flex items-center gap-3 hover:bg-white/5 pr-3 pl-1 py-1.5 rounded-xl transition-all group"
                        >
                            <div className="flex flex-col items-end">
                                <span className="text-sm font-semibold text-white group-hover:text-blue-400 transition-colors">{currentUser.username}</span>
                                <span className="text-[10px] text-blue-400 font-bold uppercase tracking-tighter">{currentUser.role}</span>
                            </div>
                            <ChevronDown size={14} className={`text-slate-500 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} />
                            <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 flex items-center justify-center font-bold border border-white/20">
                                {currentUser.username ? currentUser.username.charAt(0).toUpperCase() : 'U'}
                            </div>
                        </button>

                        {/* Dropdown Menu */}
                        {isDropdownOpen && (
                            <div className="absolute right-0 top-full mt-2 w-56 bg-[#1a1f2e] border border-white/10 rounded-xl shadow-2xl shadow-black/50 backdrop-blur-xl overflow-hidden z-50 animate-in fade-in slide-in-from-top-2 duration-200">
                                {/* Header */}
                                <div className="px-4 py-3 border-b border-white/5">
                                    <p className="text-sm font-bold text-white">{currentUser.username}</p>
                                    <p className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">{currentUser.email || ''}</p>
                                </div>

                                {/* Menu items */}
                                <div className="p-1.5 space-y-1">
                                    <button
                                        onClick={() => {
                                            setIsDropdownOpen(false);
                                            setIsProfileModalOpen(true);
                                        }}
                                        className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-slate-300 hover:text-white hover:bg-blue-600/20 transition-all group"
                                    >
                                        <User size={16} className="text-slate-500 group-hover:text-blue-400 transition-colors" />
                                        <span className="font-semibold">Thông tin cá nhân</span>
                                    </button>
                                    <button
                                        onClick={() => {
                                            setIsDropdownOpen(false);
                                            setIsPasswordModalOpen(true);
                                        }}
                                        className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-slate-300 hover:text-white hover:bg-blue-600/20 transition-all group"
                                    >
                                        <Lock size={16} className="text-slate-500 group-hover:text-blue-400 transition-colors" />
                                        <span className="font-semibold">Đổi mật khẩu</span>
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
            </div>
            </header>

            <div className="flex flex-1 overflow-hidden p-4 gap-4">
                {!hideSidebar && isSidebarOpen && (
                    <aside className="w-64 transition-all shrink-0 bg-white/5 border border-white/10 rounded-2xl flex flex-col backdrop-blur-sm p-4">
                        <div className="text-xs font-semibold text-gray-400 mb-4 uppercase tracking-widest px-2">Menu</div>
                    </aside>
                )}

                <main className="flex-1 flex flex-col bg-white/5 border border-white/10 rounded-2xl overflow-y-auto relative backdrop-blur-sm p-6 md:p-10 shadow-2xl">
                    <div className="max-w-7xl w-full mx-auto flex flex-col flex-1">
                        <Outlet />
                    </div>
                </main>
            </div>

            {/* Profile Modal - Thông tin cá nhân */}
            <ProfileModal
                isOpen={isProfileModalOpen}
                onClose={() => setIsProfileModalOpen(false)}
                userId={currentUser.id}
            />

            {/* Change Password Modal - Đặt ngoài header để không bị che */}
            <ChangePasswordModal
                isOpen={isPasswordModalOpen}
                onClose={() => setIsPasswordModalOpen(false)}
                userId={currentUser.id}
            />
        </div>
    );
}