import React, { useState, useEffect } from 'react';
import { adminService } from '../services/admin.service';
import { 
  Users, BookOpen, Sparkles, GitPullRequest, Calendar, Star, 
  ShieldAlert, UserCheck, ShieldCheck, Mail, ArrowUpRight, ShieldX, 
  Database, HardDrive, Cpu, Server 
} from 'lucide-react';

export const AdminDashboardPage = () => {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const res = await adminService.getSummary();
        if (res && res.success) {
          setSummary(res.data);
        } else {
          setError(res.message || 'Failed to fetch platform summary');
        }
      } catch (err) {
        console.error(err);
        setError('Failed to fetch platform summary');
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <svg className="animate-spin h-8 w-8 text-indigo-600" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-md bg-red-50 p-4 border border-red-200">
        <p className="text-sm font-medium text-red-800">{error}</p>
      </div>
    );
  }

  const statCards = [
    { title: 'Total Users', value: summary.totalUsers, icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
    { title: 'Active Users', value: summary.activeUsers, icon: UserCheck, color: 'text-emerald-600', bg: 'bg-emerald-50' },
    { title: 'Verified Accounts', value: summary.verifiedUsers, icon: ShieldCheck, color: 'text-indigo-600', bg: 'bg-indigo-50' },
    { title: 'Google Logins', value: summary.googleUsers, icon: Mail, color: 'text-orange-600', bg: 'bg-orange-50' },
    { title: 'Local Credentials', value: summary.localUsers, icon: Server, color: 'text-violet-600', bg: 'bg-violet-50' },
    { title: 'Administrators', value: summary.admins, icon: ShieldAlert, color: 'text-rose-600', bg: 'bg-rose-50' },
    { title: 'Regular Members', value: summary.normalUsers, icon: Users, color: 'text-slate-600', bg: 'bg-slate-50' },
    { title: 'Skills Catalog', value: summary.skills, icon: BookOpen, color: 'text-amber-600', bg: 'bg-amber-50' },
    { title: 'Mutual Matches', value: summary.matches, icon: Sparkles, color: 'text-pink-600', bg: 'bg-pink-50' },
    { title: 'Platform Rating', value: `${summary.averagePlatformRating} / 5`, icon: Star, color: 'text-yellow-600', bg: 'bg-yellow-50' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-950">Platform Overview</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Real-time metrics, platform usage summary, and activity dashboards.
        </p>
      </div>

      {/* Grid of basic summary counts */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.title} className="bg-white p-5 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">{card.title}</span>
                <div className={`p-2 rounded-lg ${card.bg}`}>
                  <Icon className={`h-4 w-4 ${card.color}`} />
                </div>
              </div>
              <div className="mt-4">
                <span className="text-2xl font-bold text-zinc-900">{card.value}</span>
              </div>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Exchange Request Breakdown */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2 mb-4">
            <GitPullRequest className="h-5 w-5 text-indigo-500" />
            Exchange Requests
          </h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600">Total Proposals</span>
              <span className="font-bold text-zinc-950">{summary.exchangeRequests}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-amber-500"></span> Pending Approval
              </span>
              <span className="font-semibold text-zinc-900">{summary.pendingRequests}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-500"></span> Accepted / Active
              </span>
              <span className="font-semibold text-zinc-900">{summary.acceptedRequests}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-red-500"></span> Declined Requests
              </span>
              <span className="font-semibold text-zinc-900">{summary.rejectedRequests}</span>
            </div>
          </div>
        </div>

        {/* Sessions Activity */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2 mb-4">
            <Calendar className="h-5 w-5 text-indigo-500" />
            Learning Sessions
          </h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600">Total Scheduled Sessions</span>
              <span className="font-bold text-zinc-950">{summary.sessions}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-indigo-500"></span> Upcoming
              </span>
              <span className="font-semibold text-zinc-900">{summary.upcomingSessions}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-500"></span> Completed
              </span>
              <span className="font-semibold text-zinc-900">{summary.completedSessions}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-zinc-600 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-rose-500"></span> Cancelled
              </span>
              <span className="font-semibold text-zinc-900">{summary.cancelledSessions}</span>
            </div>
          </div>
        </div>

        {/* Review Feed */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2 mb-4">
            <Star className="h-5 w-5 text-indigo-500" />
            Quality Assurance
          </h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600">Total Peer Reviews</span>
              <span className="font-bold text-zinc-950">{summary.reviews}</span>
            </div>
            <div className="flex justify-between items-center pb-2 border-b border-zinc-100">
              <span className="text-sm text-zinc-600">Unread System Notifications</span>
              <span className="font-semibold text-zinc-900">{summary.unreadNotifications}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-zinc-600">Overall Rating Score</span>
              <span className="font-bold text-indigo-600">{summary.averagePlatformRating} / 5.00</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
