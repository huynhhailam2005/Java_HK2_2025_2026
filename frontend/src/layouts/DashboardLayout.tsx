import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    LayoutDashboard,
    FolderKanban,
    CheckSquare,
    Github,
    Users,
    Settings,
    LogOut,
    Bell,
    Search,
    Menu
} from 'lucide-react';
import { useState, useEffect } from 'react';

//Tạo khuôn đúc dữ liệu, cấm dùng 'any'
interface UserData {
    username: string;
    role: string;
}

export default function DashboardLayout() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const location = useLocation();
    const navigate = useNavigate();

    // 🛠️ ĐÃ FIX LỖI 2: Đọc localStorage ngay lúc khởi tạo (Lazy Init) để không bị lỗi Cascading Renders
    const [currentUser] = useState<UserData | null>(() => {
        const userDataStr = localStorage.getItem('user');
        if (userDataStr) {
            return JSON.parse(userDataStr) as UserData;
        }
        return null;
    });

    //Rút gọn useEffect: Bây giờ nó chỉ làm đúng 1 việc là đuổi cổ mấy đứa chưa đăng nhập
    useEffect(() => {
        if (!currentUser) {
            navigate('/');
        }
    }, [currentUser, navigate]);

    const handleLogout = () => {
        // 1. Xóa dữ liệu user khỏi kho
        localStorage.removeItem('user');
        // 2. Điều hướng về trang Login
        navigate('/');
    };

    // 1. Khai báo 3 bộ Menu
    const studentMenu = [
        { title: 'Tổng quan Sinh viên', icon: <LayoutDashboard size={20} />, path: '/dashboard/student' },
        { title: 'Dự án của tôi', icon: <FolderKanban size={20} />, path: '/projects' },
        { title: 'Bảng công việc', icon: <CheckSquare size={20} />, path: '/tasks' },
        { title: 'Tài nguyên', icon: <Github size={20} />, path: '/resources' },
        { title: 'Thành viên nhóm', icon: <Users size={20} />, path: '/team' },
    ];

    const lecturerMenu = [
        { title: 'Tổng quan Giảng viên', icon: <LayoutDashboard size={20} />, path: '/dashboard/lecturer' },
        { title: 'Quản lý Lớp & Đồ án', icon: <FolderKanban size={20} />, path: '/manage-projects' },
        { title: 'Chấm điểm', icon: <CheckSquare size={20} />, path: '/grading' },
        { title: 'Danh sách Sinh viên', icon: <Users size={20} />, path: '/students-list' },
    ];

    const adminMenu = [
        { title: 'Tổng quan Admin', icon: <LayoutDashboard size={20} />, path: '/dashboard/admin' },
        { title: 'Quản lý Người dùng', icon: <Users size={20} />, path: '/manage-users' },
        { title: 'Cài đặt Hệ thống', icon: <Settings size={20} />, path: '/system-settings' },
    ];

    // 2. Logic chọn Menu tự động
    let currentMenuItems = studentMenu;

    if (currentUser?.role === 'LECTURER') {
        currentMenuItems = lecturerMenu;
    } else if (currentUser?.role === 'ADMIN') {
        currentMenuItems = adminMenu;
    }

    return (
        // Nền tổng của toàn bộ App (Dark mode)
        <div className="min-h-screen bg-[#050B20] text-white font-sans overflow-hidden flex flex-col">

            {/* 1. TOP HEADER - Trải dài 100% */}
            <header className="h-16 w-full border-b border-white/10 bg-white/5 backdrop-blur-md flex items-center justify-between px-6 z-50 shrink-0">

                {/* Logo & Toggle Button */}
                <div className="flex items-center gap-4 w-64">
                    <button
                        onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                        className="p-2 hover:bg-white/10 rounded-lg transition"
                    >
                        <Menu size={24} />
                    </button>
                    <div className="font-bold text-xl tracking-wider text-blue-400">SRPM<span className="text-white"></span></div>
                </div>

                {/* Search Bar */}
                <div className="flex-1 max-w-xl hidden md:flex items-center px-4 py-2 bg-white/5 border border-white/10 rounded-full focus-within:border-blue-500/50 transition">
                    <Search size={18} className="text-gray-400 mr-3" />
                    <input
                        type="text"
                        placeholder="Tìm kiếm dự án, task..."
                        className="bg-transparent border-none outline-none text-sm w-full text-white placeholder-gray-500"
                    />
                </div>

                {/* User Actions */}
                <div className="flex items-center gap-4">
                    <button className="relative p-2 hover:bg-white/10 rounded-full transition">
                        <Bell size={20} />
                        <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
                    </button>

                    {/* Hiện tên và Role của User */}
                    <div className="flex items-center gap-3 pl-4 border-l border-white/10">
                        <div className="hidden md:flex flex-col items-end">
                            <span className="text-sm font-semibold text-white">{currentUser?.username || 'Đang tải...'}</span>
                            <span className="text-xs text-blue-400 font-medium">{currentUser?.role || 'User'}</span>
                        </div>
                        <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-blue-500 to-purple-500 border border-white/20 cursor-pointer flex items-center justify-center font-bold shadow-lg text-sm">
                            {currentUser?.username?.charAt(0).toUpperCase() || 'U'}
                        </div>
                    </div>
                </div>
            </header>

            {/* 2. BODY KHU VỰC BÊN DƯỚI HEADER */}
            <div className="flex flex-1 overflow-hidden p-4 gap-4">

                {/* SIDEBAR FLOATING */}
                <aside
                    className={`
            ${isSidebarOpen ? 'w-64' : 'w-0 opacity-0 overflow-hidden'} 
            transition-all duration-300 ease-in-out shrink-0
            bg-white/5 border border-white/10 rounded-2xl flex flex-col justify-between backdrop-blur-sm
          `}
                >
                    <div className="p-4 flex flex-col gap-2">
                        <div className="text-xs font-semibold text-gray-400 mb-2 uppercase tracking-wider px-3">Menu</div>
                        {currentMenuItems.map((item, index) => {
                            const isActive = location.pathname.includes(item.path);
                            return (
                                <Link
                                    key={index}
                                    to={item.path}
                                    className={`relative flex items-center gap-3 px-3 py-2.5 rounded-xl transition-colors z-10 ${
                                        isActive
                                            ? 'text-blue-400'
                                            : 'text-gray-300 hover:bg-white/5 hover:text-white'
                                    }`}
                                >
                                    {/* HIỆU ỨNG MORPH LƯỚT CHUỘT */}
                                    {isActive && (
                                        <motion.div
                                            layoutId="active-sidebar-tab"
                                            className="absolute inset-0 bg-blue-500/20 border border-blue-500/30 shadow-[inset_0_0_10px_rgba(59,130,246,0.2)] rounded-xl z-[-1]"
                                            initial={false}
                                            transition={{
                                                type: "spring",
                                                stiffness: 400,
                                                damping: 30
                                            }}
                                        />
                                    )}

                                    <span className="relative z-10 flex items-center gap-3">
                                        {item.icon}
                                        <span className="text-sm font-medium">{item.title}</span>
                                    </span>
                                </Link>
                            );
                        })}
                    </div>

                    <div className="p-4 border-t border-white/10 flex flex-col gap-2">
                        <Link to="/settings" className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-gray-300 hover:bg-white/10 hover:text-white transition-all">
                            <Settings size={20} />
                            <span className="text-sm font-medium">Cài đặt</span>
                        </Link>
                        <button
                            onClick={handleLogout}
                            className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-red-400 hover:bg-red-500/20 transition-all w-full text-left">
                            <LogOut size={20} />
                            <span className="text-sm font-medium">Đăng xuất</span>
                        </button>
                    </div>
                </aside>

                {/* 3. MAIN CONTENT */}
                <main className="flex-1 flex flex-col items-center bg-white/5 border border-white/10 rounded-2xl overflow-y-auto relative backdrop-blur-sm shadow-xl p-6 md:p-10">
                    <div className="max-w-7xl w-full mx-auto flex flex-col flex-1">
                        <Outlet />
                    </div>
                </main>

            </div>
        </div>
    );
}