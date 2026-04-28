import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Github, Link as LinkIcon, RefreshCw, GitCommit, GitBranch, ExternalLink } from 'lucide-react';
import { githubApi } from '../services/githubApi';
import { showLiquidToast } from '../utils/toast';

interface CommitItem {
    id: string; // SHA
    message: string;
    author: string;
    date: string;
    url: string;
}

const ResourcesPage = () => {
    const [repoUrl, setRepoUrl] = useState('');
    const [commits, setCommits] = useState<CommitItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isSyncing, setIsSyncing] = useState(false);

    const MOCK_GROUP_ID = 1; // Tạm fix cứng ID nhóm để test
    const IS_TEAM_LEADER = true; // Chỉ Leader mới được phép Link Repo

    useEffect(() => {
        void fetchCommits();
    }, []);

    const fetchCommits = async () => {
        setLoading(true);
        try {
            const res = await githubApi.getCommits(MOCK_GROUP_ID);
            setCommits((res.data.data as unknown as CommitItem[]) || []);
        } catch (error) {
            console.error("Lỗi fetch commits:", error);
            // MOCK DATA: Giả lập dữ liệu nếu API chưa sẵn sàng
            setCommits([
                { id: 'a1b2c3d', message: 'feat: Hoàn thành API Login và JWT', author: 'lam_pro', date: '2024-03-20 14:30', url: '#' },
                { id: 'f4e5d6c', message: 'fix: Lỗi văng trang khi chưa có token', author: 'lam_pro', date: '2024-03-19 09:15', url: '#' },
                { id: '9a8b7c6', message: 'chore: Setup project structure', author: 'thanh_dev', date: '2024-03-18 10:00', url: '#' },
            ]);
            setRepoUrl('https://github.com/huynhhailam2005/Java_HK2_2025_2026');
        } finally {
            setLoading(false);
        }
    };

    const handleSyncGithub = async () => {
        if (!repoUrl) {
            showLiquidToast('Vui lòng nhập link GitHub Repository!', 'error');
            return;
        }

        setIsSyncing(true);
        try {
            await githubApi.syncRepo(MOCK_GROUP_ID, repoUrl);
            showLiquidToast('Đồng bộ GitHub thành công!', 'success');
            void fetchCommits();
        } catch (error: unknown) {
            const err = error as { response?: { data?: { message?: string } } };
            showLiquidToast(err.response?.data?.message || 'Lỗi khi đồng bộ GitHub!', 'error');
        } finally {
            setIsSyncing(false);
        }
    };

    return (
        <div className="space-y-8 max-w-5xl mx-auto w-full">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-3xl font-black text-white tracking-tight flex items-center gap-3">
                        <Github className="text-slate-300" /> Tài nguyên & Mã nguồn
                    </h1>
                    <p className="text-slate-400 mt-2">Đồng bộ kho lưu trữ GitHub để tự động chấm điểm đóng góp.</p>
                </div>
            </div>

            {/* Khu vực Nhập Link Repo */}
            {IS_TEAM_LEADER && (
                <div className="bg-white/5 border border-white/10 rounded-3xl p-6 shadow-sm">
                    <h3 className="text-white font-bold mb-4 flex items-center gap-2">
                        <LinkIcon className="w-5 h-5 text-blue-400" /> Liên kết Repository
                    </h3>
                    <div className="flex flex-col md:flex-row gap-4">
                        <input
                            type="text"
                            placeholder="Nhập link (VD: https://github.com/username/repo)"
                            value={repoUrl}
                            onChange={(e) => setRepoUrl(e.target.value)}
                            className="flex-1 bg-black/40 border border-white/10 rounded-2xl py-3 px-5 text-white focus:outline-none focus:ring-2 focus:ring-blue-500/50"
                        />
                        <button
                            onClick={handleSyncGithub}
                            disabled={isSyncing}
                            className="bg-blue-600 hover:bg-blue-500 text-white px-8 py-3 rounded-2xl font-bold transition-all shadow-lg shadow-blue-500/25 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed min-w-[160px]"
                        >
                            <RefreshCw className={`w-5 h-5 ${isSyncing ? 'animate-spin' : ''}`} />
                            {isSyncing ? 'Đang kéo...' : 'Đồng bộ'}
                        </button>
                    </div>
                </div>
            )}

            {/* Khu vực Lịch sử Commits */}
            <div className="bg-black/20 border border-white/5 rounded-4xl p-6 shadow-xl relative overflow-hidden">
                {/* Decoration */}
                <div className="absolute top-0 right-0 p-10 opacity-5 pointer-events-none">
                    <GitBranch className="w-40 h-40" />
                </div>

                <div className="flex justify-between items-center mb-8 relative z-10">
                    <h3 className="text-xl font-bold text-white flex items-center gap-2">
                        <GitCommit className="text-emerald-400" /> Lịch sử Commits
                    </h3>
                    <span className="bg-white/10 text-slate-300 text-xs px-3 py-1.5 rounded-full font-mono">
                        {commits.length} commits
                    </span>
                </div>

                <div className="space-y-4 relative z-10 max-h-[500px] overflow-y-auto custom-scrollbar pr-2">
                    {loading ? (
                        <p className="text-center text-slate-500 py-10">Đang quét kho lưu trữ...</p>
                    ) : commits.length === 0 ? (
                        <div className="text-center py-10 text-slate-500">
                            <Github className="w-12 h-12 mx-auto mb-3 opacity-20" />
                            <p>Chưa có dữ liệu commit. Hãy liên kết repo và đồng bộ.</p>
                        </div>
                    ) : (
                        commits.map((commit, index) => (
                            <motion.div
                                key={commit.id}
                                initial={{ opacity: 0, x: -20 }}
                                animate={{ opacity: 1, x: 0 }}
                                transition={{ delay: index * 0.1 }}
                                className="group flex gap-4 bg-white/5 hover:bg-white/10 border border-white/5 hover:border-white/10 p-4 rounded-2xl transition-all"
                            >
                                <div className="flex flex-col items-center gap-1 mt-1 shrink-0">
                                    <div className="w-3 h-3 rounded-full bg-emerald-500 shadow-[0_0_10px_rgba(16,185,129,0.5)]"></div>
                                    {index !== commits.length - 1 && <div className="w-0.5 h-full bg-white/10 mt-1"></div>}
                                </div>
                                <div className="flex-1">
                                    <div className="flex items-center gap-2 mb-1">
                                        <span className="text-white font-bold text-sm">{commit.message}</span>
                                    </div>
                                    <div className="flex items-center gap-4 text-xs text-slate-400 font-mono">
                                        <span className="flex items-center gap-1 text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded-md">
                                            <img src={`https://github.com/${commit.author}.png`} alt="avt" className="w-4 h-4 rounded-full bg-black/50" onError={(e) => (e.currentTarget.style.display = 'none')} />
                                            {commit.author}
                                        </span>
                                        <span>{commit.date}</span>
                                        <span className="bg-white/5 px-2 py-0.5 rounded-md border border-white/10">{commit.id.substring(0, 7)}</span>
                                        <a href={commit.url} target="_blank" rel="noreferrer" className="ml-auto opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1 text-slate-300 hover:text-white">
                                            Xem chi tiết <ExternalLink className="w-3 h-3" />
                                        </a>
                                    </div>
                                </div>
                            </motion.div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResourcesPage;