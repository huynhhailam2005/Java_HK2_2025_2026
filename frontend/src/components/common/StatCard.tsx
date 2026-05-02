import { type ReactNode } from 'react';

interface StatCardProps {
    title: string;
    value: string | number;
    icon: ReactNode;
    colorClass: string;
}

export default function StatCard({ title, value, icon, colorClass }: StatCardProps) {
    return (
        <div className="bg-white/5 border border-white/10 p-6 rounded-2xl shadow-lg hover:bg-white/10 transition duration-300">
            <div className="flex items-center justify-between mb-4">
                <div className={`p-3 rounded-xl ${colorClass}`}>
                    {icon}
                </div>
            </div>
            <p className="text-sm text-slate-400 font-medium">{title}</p>
            <p className={`text-3xl font-bold mt-1 ${colorClass.includes('red') || colorClass.includes('green') || colorClass.includes('orange') ? colorClass.split(' ')[1] : 'text-white'}`}>
                {value}
            </p>
        </div>
    );
}