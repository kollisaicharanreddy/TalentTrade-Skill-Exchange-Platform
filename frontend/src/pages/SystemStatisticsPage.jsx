import React, { useState, useEffect } from 'react';
import { adminService } from '../services/admin.service';
import { Database, Activity, Cpu, Server, CheckCircle2, AlertOctagon } from 'lucide-react';

export const SystemStatisticsPage = () => {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchHealth = async () => {
    try {
      const res = await adminService.getHealth();
      if (res && res.success) {
        setHealth(res.data);
      } else {
        setError(res.message || 'Failed to load system health statistics');
      }
    } catch (err) {
      console.error(err);
      setError('Failed to request application health status');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
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

  const memoryPercentage = ((health.usedMemoryMb / health.totalMemoryMb) * 100).toFixed(1);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-zinc-950">System Statistics</h1>
          <p className="mt-1 text-sm text-zinc-500">
            Monitor real-time system resource allocation, JVM memory profiles, and database operational health.
          </p>
        </div>
        <button
          onClick={fetchHealth}
          className="bg-white border border-zinc-300 text-zinc-700 px-3 py-1.5 rounded-md text-sm font-medium hover:bg-zinc-50 transition-colors"
        >
          Refresh Status
        </button>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Memory Profile Card */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2">
                <Cpu className="h-5 w-5 text-indigo-500" />
                JVM Memory Profile
              </h2>
              <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Resource Allocation</span>
            </div>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-zinc-500">Memory Utilization:</span>
                  <span className="font-semibold text-zinc-950">{memoryPercentage}% ({health.usedMemoryMb} MB / {health.totalMemoryMb} MB)</span>
                </div>
                <div className="w-full bg-zinc-100 h-2.5 rounded-full overflow-hidden">
                  <div 
                    className="bg-indigo-600 h-full rounded-full transition-all duration-500" 
                    style={{ width: `${memoryPercentage}%` }}
                  />
                </div>
              </div>
              <div className="flex justify-between text-sm pt-2">
                <span className="text-zinc-500">Free Allocated Memory:</span>
                <span className="font-semibold text-zinc-950">{health.freeMemoryMb} MB</span>
              </div>
            </div>
          </div>
        </div>

        {/* Database operational health */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2">
                <Database className="h-5 w-5 text-indigo-500" />
                Database Engine Status
              </h2>
              <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-bold ${
                health.databaseStatus === 'UP' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'
              }`}>
                {health.databaseStatus === 'UP' ? <CheckCircle2 className="h-3 w-3 mr-1" /> : <AlertOctagon className="h-3 w-3 mr-1" />}
                {health.databaseStatus}
              </span>
            </div>
            {health.databaseStatus === 'UP' ? (
              <div className="space-y-3 text-sm">
                <div className="flex justify-between pb-1.5 border-b border-zinc-100">
                  <span className="text-zinc-500">Database System:</span>
                  <span className="font-semibold text-zinc-950">{health.databaseProductName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-zinc-500">Engine Version:</span>
                  <span className="font-semibold text-zinc-950 text-right max-w-[12rem] truncate" title={health.databaseProductVersion}>
                    {health.databaseProductVersion}
                  </span>
                </div>
              </div>
            ) : (
              <div className="text-sm text-red-500">
                Connection failure detected: {health.databaseError || 'No response from data source.'}
              </div>
            )}
          </div>
        </div>

        {/* Runtime Environment Card */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2">
                <Server className="h-5 w-5 text-indigo-500" />
                Server Environment
              </h2>
              <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Specs</span>
            </div>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between pb-1.5 border-b border-zinc-100">
                <span className="text-zinc-500">App Name:</span>
                <span className="font-semibold text-zinc-950">{health.appName}</span>
              </div>
              <div className="flex justify-between pb-1.5 border-b border-zinc-100">
                <span className="text-zinc-500">Build Version:</span>
                <span className="font-semibold text-zinc-950">{health.version}</span>
              </div>
              <div className="flex justify-between pb-1.5 border-b border-zinc-100">
                <span className="text-zinc-500">Java JRE Runtime:</span>
                <span className="font-semibold text-zinc-950">{health.javaVersion}</span>
              </div>
              <div className="flex justify-between pb-1.5 border-b border-zinc-100">
                <span className="text-zinc-500">Operating System:</span>
                <span className="font-semibold text-zinc-950">{health.osName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Available Processors:</span>
                <span className="font-semibold text-zinc-950">{health.availableProcessors} Cores</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemStatisticsPage;
