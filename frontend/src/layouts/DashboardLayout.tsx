import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    LayoutDashboard,
    FolderKanban,
    LogOut,
    Search,
    Menu,
    User,
    Github,
    Trello,
    X,
    Save
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { AUTH_TOKEN_KEY } from '../services/authApi';
import { showLiquidToast } from '../utils/toast';

interface UserData {
    username: string;
    role: string;
}

export default function DashboardLayout() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);

    // State cho Profile
    const [profileData, setProfileData] = useState({
        githubUsername: '',
        jiraEmail: ''
    });

    const location = useLocation();
    const navigate = useNavigate();

    const [currentUser] = useState<UserData | null>(() => {
        const userDataStr = localStorage.getItem('user');
        if (!userDataStr) return null;
        try {
            const parsed = JSON.parse(userDataStr) as Partial<UserData>;
            if (typeof parsed?.username === 'string' && typeof parsed?.role === 'string') {
                return { username: parsed.username, role: parsed.role };
            }
        } catch {
            localStorage.removeItem('user');
        }
        return null;
    });

    useEffect(() => {
        if (!currentUser) navigate('/');
        // Mock load data profile
        setProfileData({
            githubUsername: currentUser?.role === 'STUDENT' ? 'hailam2005' : '',
            jiraEmail: currentUser?.username + '@st.uth.edu.vn'
        });
    }, [currentUser, navigate]);

    const handleLogout = () => {
        localStorage.removeItem('user');
        localStorage.removeItem(AUTH_TOKEN_KEY);
        navigate('/');
    };

    const handleSaveProfile = () => {
        // Sau này sẽ gọi API update profile ở đây
        showLiquidToast('Đã lưu thông tin cá nhân thành công!', 'success');
        setIsProfileModalOpen(false);
    };

    // 🔥 MENU ĐÃ ĐƯỢC DỌN DẸP SẠCH SẼ
    const studentMenu = [
        { title: 'Tổng quan Sinh viên', icon: <LayoutDashboard size={20} />, path: '/dashboard/student' },
        { title: 'Dự án của tôi', icon: <FolderKanban size={20} />, path: '/groups' },
    ];

    const lecturerMenu = [
        { title: 'Tổng quan Giảng viên', icon: <LayoutDashboard size={20} />, path: '/dashboard/lecturer' },
        { title: 'Quản lý Nhóm', icon: <FolderKanban size={20} />, path: '/groups' },
    ];

    const adminMenu = [
        { title: 'Tổng quan Admin', icon: <LayoutDashboard size={20} />, path: '/dashboard/admin' },
    ];

    let currentMenuItems = studentMenu;
    if (currentUser?.role === 'LECTURER') currentMenuItems = lecturerMenu;
    else if (currentUser?.role === 'ADMIN') currentMenuItems = adminMenu;

    return (
        <div className="min-h-screen bg-[#050B20] text-white font-sans overflow-hidden flex flex-col">
            <header className="h-16 w-full border-b border-white/10 bg-white/5 backdrop-blur-md flex items-center justify-between px-6 z-40 shrink-0">
                <div className="flex items-center gap-4 w-64">
                    <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="p-2 hover:bg-white/10 rounded-lg transition">
                        <Menu size={24} />
                    </button>
                    <div className="font-bold text-xl tracking-wider text-blue-400">SRPM</div>
                </div>

                <div className="flex-1 max-w-xl hidden md:flex items-center px-4 py-2 bg-white/5 border border-white/10 rounded-full focus-within:border-blue-500/50 transition">
                    <Search size={18} className="text-gray-400 mr-3" />
                    <input type="text" placeholder="Tìm kiếm..." className="bg-transparent border-none outline-none text-sm w-full text-white placeholder-gray-500" />
                </div>

                <div className="flex items-center gap-4">
                    <div className="flex items-center gap-3 pl-4 border-l border-white/10">
                        <div className="hidden md:flex flex-col items-end">
                            <span className="text-sm font-semibold text-white">{currentUser?.username || 'Đang tải...'}</span>
                            <span className="text-xs text-blue-400 font-medium">{currentUser?.role || 'User'}</span>
                        </div>
                        {/* 🔥 NÚT AVATAR ĐỂ BẬT PROFILE MODAL */}
                        <div
                            onClick={() => setIsProfileModalOpen(true)}
                            className="h-9 w-9 rounded-full bg-linear-to-tr from-blue-500 to-purple-500 border border-white/20 flex items-center justify-center font-bold shadow-lg text-sm cursor-pointer hover:ring-2 hover:ring-blue-400 transition-all"
                        >
                            {currentUser?.username?.charAt(0).toUpperCase() || 'U'}
                        </div>
                    </div>
                </div>
            </header>

            <div className="flex flex-1 overflow-hidden p-4 gap-4">
                <aside className={`${isSidebarOpen ? 'w-64' : 'w-0 opacity-0 overflow-hidden'} transition-all duration-300 ease-in-out shrink-0 bg-white/5 border border-white/10 rounded-2xl flex flex-col justify-between backdrop-blur-sm`}>
                    <div className="p-4 flex flex-col gap-2">
                        <div className="text-xs font-semibold text-gray-400 mb-2 uppercase tracking-wider px-3">Menu</div>
                        {currentMenuItems.map((item, index) => {
                            const isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/');
                            return (
                                <Link key={index} to={item.path} className={`relative flex items-center gap-3 px-3 py-2.5 rounded-xl transition-colors z-10 ${isActive ? 'text-blue-400' : 'text-gray-300 hover:bg-white/5 hover:text-white'}`}>
                                    {isActive && <motion.div layoutId="active-sidebar-tab" className="absolute inset-0 bg-blue-500/20 border border-blue-500/30 shadow-[inset_0_0_10px_rgba(59,130,246,0.2)] rounded-xl z-[-1]" transition={{ type: "spring", stiffness: 400, damping: 30 }} />}
                                    <span className="relative z-10 flex items-center gap-3">{item.icon}<span className="text-sm font-medium">{item.title}</span></span>
                                </Link>
                            );
                        })}
                    </div>

                    <div className="p-4 border-t border-white/10 flex flex-col gap-2">
                        <button onClick={handleLogout} className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-red-400 hover:bg-red-500/20 transition-all w-full text-left">
                            <LogOut size={20} />
                            <span className="text-sm font-medium">Đăng xuất</span>
                        </button>
                    </div>
                </aside>

                <main className="flex-1 flex flex-col items-center bg-white/5 border border-white/10 rounded-2xl overflow-y-auto relative backdrop-blur-sm shadow-xl p-6 md:p-10">
                    <div className="max-w-7xl w-full mx-auto flex flex-col flex-1">
                        <Outlet />
                    </div>
                </main>
            </div>

            {/* 🔥 PROFILE MODAL */}
            <AnimatePresence>
                {isProfileModalOpen && (
                    <>
                        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setIsProfileModalOpen(false)} className="fixed inset-0 bg-black/60 backdrop-blur-sm z-100" />
                        <motion.div initial={{ scale: 0.95, opacity: 0, y: 20 }} animate={{ scale: 1, opacity: 1, y: 0 }} exit={{ scale: 0.95, opacity: 0, y: 20 }} className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md bg-[#0f172a] border border-white/10 rounded-3xl p-8 z-101 shadow-2xl">
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-xl font-bold text-white flex items-center gap-2"><User className="text-blue-400"/> Hồ Sơ Cá Nhân</h2>
                                <button onClick={() => setIsProfileModalOpen(false)} className="text-slate-400 hover:text-white transition-colors"><X size={20}/></button>
                            </div>

                            <div className="space-y-5">
                                <div className="space-y-2">
                                    <label className="text-sm font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                        <Github size={16} /> GitHub Username
                                    </label>
                                    <input
                                        type="text"
                                        value={profileData.githubUsername}
                                        onChange={(e) => setProfileData({...profileData, githubUsername: e.target.value})}
                                        placeholder="VD: huynhhailam2005"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                    <p className="text-xs text-slate-500">Dùng để map đóng góp commit của bạn.</p>
                                </div>

                                <div className="space-y-2">
                                    <label className="text-sm font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                                        <Trello size={16} /> Jira Email Account
                                    </label>
                                    <input
                                        type="email"
                                        value={profileData.jiraEmail}
                                        onChange={(e) => setProfileData({...profileData, jiraEmail: e.target.value})}
                                        placeholder="VD: lam@st.uth.edu.vn"
                                        className="w-full bg-white/5 border border-white/10 rounded-xl p-3 text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    />
                                    <p className="text-xs text-slate-500">Email bạn dùng để đăng nhập Jira Atlassian.</p>
                                </div>

                                <button onClick={handleSaveProfile} className="w-full bg-blue-600 hover:bg-blue-500 text-white p-4 rounded-2xl font-bold transition-all flex justify-center items-center gap-2 mt-4">
                                    <Save size={20} /> LƯU THÔNG TIN
                                </button>
                            </div>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>
        </div>
    );
}