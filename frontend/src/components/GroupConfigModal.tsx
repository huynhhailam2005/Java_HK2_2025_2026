import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Save, ShieldCheck, Database, Github, Loader2 } from 'lucide-react';
import { groupConfigApi } from '../services/groupConfigApi';
import { showLiquidToast } from '../utils/toast';

export default function GroupConfigModal({ groupId, groupName, isOpen, onClose, onSave }: any) {
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const [config, setConfig] = useState({
        groupName: '',
        jiraUrl: '',
        jiraProjectKey: '',
        jiraApiToken: '',
        jiraAdminEmail: '',
        githubRepoUrl: '',
        githubAccessToken: ''
    });

    useEffect(() => {
        if (isOpen && groupId && groupId > 0) {
            void loadConfig();
        }
    }, [isOpen, groupId]);

    const loadConfig = async () => {
        setLoading(true);
        try {
            const response = await groupConfigApi.getGroupConfig(groupId);

            console.log("Dữ liệu cấu hình từ Backend:", response.data);

            if (response.data.success) {
                const d = response.data.data;

                setConfig({
                    groupName: d.groupName || groupName || '',
                    jiraUrl: d.jiraUrl || '',
                    jiraProjectKey: d.jiraProjectKey || '',
                    jiraApiToken: d.jiraApiToken || '',
                    jiraAdminEmail: d.jiraAdminEmail || '',
                    githubRepoUrl: d.githubRepoUrl || '',
                    githubAccessToken: d.githubAccessToken || ''
                });
            }
        } catch (error: any) {
            console.error('Lỗi tải cấu hình:', error);
            const msg = error.response?.data?.message || 'Không thể lấy thông tin hiện tại';
            showLiquidToast(msg, 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            const res = await groupConfigApi.updateGroupConfig(groupId, config);
            if (res.data.success) {
                showLiquidToast('Đã cập nhật cấu hình hệ thống!', 'success');
                onSave?.(); // Gọi để Dashboard load lại nếu cần
                onClose();
            }
        } catch (error: any) {
            const msg = error.response?.data?.message || 'Lỗi khi lưu dữ liệu mới';
            showLiquidToast(msg, 'error');
        } finally {
            setSaving(false);
        }
    };

    if (!isOpen) return null;

    return (
        <AnimatePresence>
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="fixed inset-0 bg-black/80 backdrop-blur-md z-100 flex items-center justify-center p-4">
                <motion.div initial={{ scale: 0.9, y: 20 }} animate={{ scale: 1, y: 0 }} className="bg-[#0f172a] border border-white/10 rounded-[2.5rem] p-8 w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-[0_0_50px_rgba(0,0,0,0.5)]">

                    {/* Header */}
                    <div className="flex justify-between items-center mb-8">
                        <div className="flex items-center gap-4">
                            <div className="p-3 bg-blue-500/20 rounded-2xl text-blue-400">
                                <Database size={24}/>
                            </div>
                            <div>
                                <h2 className="text-2xl font-bold text-white leading-none">Cấu Hình Nhóm: {groupName}</h2>
                                <p className="text-slate-500 text-sm mt-1">Quản lý định danh Jira và kho GitHub</p>
                            </div>
                        </div>
                        <button onClick={onClose} className="p-2 text-slate-400 hover:text-white transition-colors"><X size={28}/></button>
                    </div>

                    {loading ? (
                        <div className="flex flex-col items-center justify-center py-20 gap-4">
                            <Loader2 className="w-10 h-10 text-blue-500 animate-spin" />
                            <p className="text-slate-400 font-medium animate-pulse">Đang tải cấu hình hiện có...</p>
                        </div>
                    ) : (
                        <div className="space-y-8">
                            {/* JIRA SECTION */}
                            <div className="bg-white/[0.02] border border-white/5 p-6 rounded-3xl space-y-4">
                                <h3 className="text-blue-400 font-black text-xs uppercase tracking-widest flex items-center gap-2 mb-2">
                                    <ShieldCheck size={16}/> THÔNG TIN JIRA
                                </h3>
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1.5">Jira URL</label>
                                    <input
                                        className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-blue-500 outline-none transition-all"
                                        placeholder="https://..."
                                        value={config.jiraUrl}
                                        onChange={e => setConfig({...config, jiraUrl: e.target.value})}
                                    />
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-slate-400 text-xs font-semibold mb-1.5">Project Key</label>
                                        <input className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-blue-500 outline-none" placeholder="VD: PROJ" value={config.jiraProjectKey} onChange={e => setConfig({...config, jiraProjectKey: e.target.value})}/>
                                    </div>
                                    <div>
                                        <label className="block text-slate-400 text-xs font-semibold mb-1.5">API Token</label>
                                        <input className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-blue-500 outline-none" type="password" placeholder="••••••••" value={config.jiraApiToken} onChange={e => setConfig({...config, jiraApiToken: e.target.value})}/>
                                    </div>
                                </div>
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1.5">Email quản trị Jira</label>
                                    <input className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-blue-500 outline-none" placeholder="admin@company.com" value={config.jiraAdminEmail} onChange={e => setConfig({...config, jiraAdminEmail: e.target.value})}/>
                                </div>
                            </div>

                            {/* GITHUB SECTION */}
                            <div className="bg-white/[0.02] border border-white/5 p-6 rounded-3xl space-y-4">
                                <h3 className="text-emerald-400 font-black text-xs uppercase tracking-widest flex items-center gap-2 mb-2">
                                    <Github size={16}/> THÔNG TIN GITHUB
                                </h3>
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1.5">GitHub Repo URL</label>
                                    <input className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-emerald-500 outline-none" placeholder="https://github.com/..." value={config.githubRepoUrl} onChange={e => setConfig({...config, githubRepoUrl: e.target.value})}/>
                                </div>
                                <div>
                                    <label className="block text-slate-400 text-xs font-semibold mb-1.5">GitHub Access Token</label>
                                    <input className="w-full bg-black/40 border border-white/10 rounded-xl p-4 text-white focus:border-emerald-500 outline-none" type="password" placeholder="••••••••" value={config.githubAccessToken} onChange={e => setConfig({...config, githubAccessToken: e.target.value})}/>
                                </div>
                            </div>

                            {/* Action Button */}
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:bg-emerald-600/50 text-white p-5 rounded-2xl font-black shadow-lg shadow-emerald-900/20 transition-all flex justify-center items-center gap-3 uppercase tracking-tighter"
                            >
                                {saving ? <Loader2 className="animate-spin" /> : <Save size={20}/>}
                                {saving ? 'Đang đồng bộ...' : 'Xác nhận lưu cấu hình'}
                            </button>
                        </div>
                    )}
                </motion.div>
            </motion.div>
        </AnimatePresence>
    );
}