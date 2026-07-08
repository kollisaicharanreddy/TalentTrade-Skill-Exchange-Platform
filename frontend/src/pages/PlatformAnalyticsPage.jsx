import React, { useState, useEffect } from 'react';
import { adminService } from '../services/admin.service';
import { 
  BarChart3, Calendar, Award, Star, Activity, 
  CheckCircle, ArrowUpRight, TrendingUp 
} from 'lucide-react';

export const PlatformAnalyticsPage = () => {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const res = await adminService.getAnalytics();
        if (res && res.success) {
          setAnalytics(res.data);
        } else {
          setError(res.message || 'Failed to fetch analytics data');
        }
      } catch (err) {
        console.error(err);
        setError('Failed to load platform analytics');
      } finally {
        setLoading(false);
      }
    };
    fetchAnalytics();
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

  // Find max values for progress bar normalization
  const maxPopularVal = Math.max(...Object.values(analytics.mostPopularSkills || { a: 1 }));
  const maxRequestedVal = Math.max(...Object.values(analytics.topRequestedSkills || { a: 1 }));
  const maxActiveLearnerVal = Math.max(...Object.values(analytics.mostActiveLearners || { a: 1 }));

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-950">Platform Analytics</h1>
        <p className="mt-1 text-sm text-zinc-500">
          In-depth reports, user activity trends, rating distributions, and skill popularity indexes.
        </p>
      </div>

      {/* Activity overview */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Daily Active Users (DAU)</span>
            <h3 className="text-3xl font-extrabold text-zinc-900 mt-2">{analytics.dailyActiveUsers}</h3>
            <p className="text-xs text-zinc-500 mt-1">Unique members active in the last 24 hours.</p>
          </div>
          <div className="mt-4 pt-4 border-t border-zinc-100 flex items-center justify-between text-xs text-emerald-600 font-medium">
            <span className="flex items-center"><TrendingUp className="h-3.5 w-3.5 mr-1" /> Healthy Activity</span>
            <span>DAU / WAU ratio</span>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Weekly Active Users (WAU)</span>
            <h3 className="text-3xl font-extrabold text-zinc-900 mt-2">{analytics.weeklyActiveUsers}</h3>
            <p className="text-xs text-zinc-500 mt-1">Unique members active in the last 7 days.</p>
          </div>
          <div className="mt-4 pt-4 border-t border-zinc-100 flex items-center justify-between text-xs text-indigo-600 font-medium">
            <span className="flex items-center"><TrendingUp className="h-3.5 w-3.5 mr-1" /> Dynamic growth</span>
            <span>Monthly active forecast: {analytics.weeklyActiveUsers * 2}</span>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm flex flex-col justify-between">
          <div>
            <span className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Key Conversion Rates</span>
            <div className="mt-3 space-y-2">
              <div className="flex justify-between items-center text-sm">
                <span className="text-zinc-500">Session Completion:</span>
                <span className="font-semibold text-zinc-950">{analytics.sessionCompletionRate}%</span>
              </div>
              <div className="flex justify-between items-center text-sm">
                <span className="text-zinc-500">Request Acceptance:</span>
                <span className="font-semibold text-zinc-950">{analytics.requestAcceptanceRate}%</span>
              </div>
            </div>
          </div>
          <div className="mt-4 pt-4 border-t border-zinc-100 flex items-center justify-between text-xs text-zinc-500">
            <span>Overall completion metrics</span>
            <span className="text-indigo-600 font-medium">High Engagement</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Most Popular Skills (To Teach) */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 mb-4 flex items-center gap-2">
            <Award className="h-5 w-5 text-indigo-500" />
            Most Offered Skills (Supply)
          </h2>
          <div className="space-y-4">
            {Object.entries(analytics.mostPopularSkills || {}).map(([skill, count]) => (
              <div key={skill} className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="font-medium text-zinc-800">{skill}</span>
                  <span className="text-zinc-500">{count} teachers</span>
                </div>
                <div className="w-full bg-zinc-100 h-2 rounded-full overflow-hidden">
                  <div 
                    className="bg-indigo-600 h-full rounded-full transition-all duration-500" 
                    style={{ width: `${(count / maxPopularVal) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Top Requested Skills (To Learn) */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 mb-4 flex items-center gap-2">
            <TrendingUp className="h-5 w-5 text-indigo-500" />
            Top Demanded Skills (Demand)
          </h2>
          <div className="space-y-4">
            {Object.entries(analytics.topRequestedSkills || {}).map(([skill, count]) => (
              <div key={skill} className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="font-medium text-zinc-800">{skill}</span>
                  <span className="text-zinc-500">{count} learners</span>
                </div>
                <div className="w-full bg-zinc-100 h-2 rounded-full overflow-hidden">
                  <div 
                    className="bg-emerald-600 h-full rounded-full transition-all duration-500" 
                    style={{ width: `${(count / maxRequestedVal) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Highest Rated Mentors */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 mb-4 flex items-center gap-2">
            <Star className="h-5 w-5 text-yellow-500" />
            Highest Rated Mentors
          </h2>
          <div className="divide-y divide-zinc-100">
            {Object.entries(analytics.highestRatedMentors || {}).map(([name, rating], idx) => (
              <div key={name} className="py-3 flex items-center justify-between first:pt-0 last:pb-0">
                <div className="flex items-center space-x-3">
                  <span className="text-xs font-bold text-zinc-400">#0{idx + 1}</span>
                  <span className="text-sm font-medium text-zinc-800">{name}</span>
                </div>
                <span className="inline-flex items-center text-xs font-semibold text-yellow-600 bg-yellow-50 px-2 py-0.5 rounded">
                  ★ {rating}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Most Active Learners */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 mb-4 flex items-center gap-2">
            <CheckCircle className="h-5 w-5 text-indigo-500" />
            Most Active Learners
          </h2>
          <div className="space-y-4">
            {Object.entries(analytics.mostActiveLearners || {}).map(([name, count]) => (
              <div key={name} className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="font-medium text-zinc-800">{name}</span>
                  <span className="text-zinc-500 font-semibold">{count} sessions completed</span>
                </div>
                <div className="w-full bg-zinc-100 h-1.5 rounded-full overflow-hidden">
                  <div 
                    className="bg-violet-600 h-full rounded-full transition-all duration-500" 
                    style={{ width: `${(count / maxActiveLearnerVal) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Ratings distribution */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm">
          <h2 className="text-lg font-semibold text-zinc-950 mb-4 flex items-center gap-2">
            <BarChart3 className="h-5 w-5 text-indigo-500" />
            Rating Distributions
          </h2>
          <div className="space-y-3">
            {[5, 4, 3, 2, 1].map((rating) => {
              const count = analytics.averageRatingDistribution[rating] || 0;
              const total = Object.values(analytics.averageRatingDistribution).reduce((a, b) => a + b, 0);
              const percentage = total > 0 ? (count / total) * 100 : 0;
              return (
                <div key={rating} className="flex items-center space-x-2">
                  <span className="text-xs font-semibold text-zinc-500 w-3">{rating}★</span>
                  <div className="flex-1 bg-zinc-100 h-2.5 rounded-full overflow-hidden">
                    <div 
                      className="bg-yellow-400 h-full rounded-full transition-all duration-500" 
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                  <span className="text-xs font-medium text-zinc-600 w-8 text-right">{count}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PlatformAnalyticsPage;
