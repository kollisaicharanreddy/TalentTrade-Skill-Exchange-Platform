import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { 
  Sparkles, 
  BookOpen, 
  Calendar, 
  Star, 
  ArrowRight,
  GitPullRequest,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  MessageSquare,
  User
} from 'lucide-react';
import { dashboardService } from '../services/dashboard.service';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Skeleton } from '../components/ui/Skeleton';
import { Button } from '../components/ui/Button';

export const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      const response = await dashboardService.getDashboard();
      if (response && response.success) {
        setStats(response.data);
      }
    } catch (error) {
      console.error("Failed to load dashboard statistics:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-4 w-64 mt-2" />
        </div>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          <Skeleton className="h-64 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      </div>
    );
  }

  const ratingStars = stats ? Math.round(stats.averageRating) : 0;

  return (
    <div className="space-y-6">
      {/* Title Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Overview</h1>
        <p className="text-sm text-zinc-500">Welcome to your TalentTrade center. Track your exchanges and connections.</p>
      </div>

      {/* Stats Widgets Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        
        {/* Skills Offered Card */}
        <Card className="hover:shadow transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Skills Offered</CardTitle>
            <BookOpen className="h-4 w-4 text-zinc-400" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-zinc-900">{stats?.skillsOffered || 0}</div>
            <p className="text-xs text-zinc-500 mt-1">Skills you list to teach</p>
          </CardContent>
        </Card>

        {/* Skills Wanted Card */}
        <Card className="hover:shadow transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Skills Wanted</CardTitle>
            <BookOpen className="h-4 w-4 text-zinc-400" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-zinc-900">{stats?.skillsWanted || 0}</div>
            <p className="text-xs text-zinc-500 mt-1">Skills you list to learn</p>
          </CardContent>
        </Card>

        {/* Reciprocal Matches Card */}
        <Card className="hover:shadow transition-shadow bg-zinc-950 text-white border-zinc-800">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold text-zinc-400 uppercase tracking-wider">Reciprocal Matches</CardTitle>
            <Sparkles className="h-4 w-4 text-zinc-300" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{stats?.matches || 0}</div>
            <p className="text-xs text-zinc-400 mt-1">Mutual teach/learn partners</p>
          </CardContent>
        </Card>

        {/* Avg Rating Card */}
        <Card className="hover:shadow transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-xs font-semibold text-zinc-500 uppercase tracking-wider">Average Rating</CardTitle>
            <Star className="h-4 w-4 text-zinc-400" />
          </CardHeader>
          <CardContent>
            <div className="flex items-baseline space-x-2">
              <span className="text-3xl font-bold text-zinc-900">{stats?.averageRating ? stats.averageRating.toFixed(1) : 'N/A'}</span>
              {stats?.averageRating ? (
                <div className="flex text-amber-500 text-xs">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <span key={i} className={i < ratingStars ? 'text-amber-500' : 'text-zinc-200'}>★</span>
                  ))}
                </div>
              ) : null}
            </div>
            <p className="text-xs text-zinc-500 mt-1">From {stats?.totalReviews || 0} reviews</p>
          </CardContent>
        </Card>

      </div>

      {/* Main Splits Content */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        
        {/* Exchange Requests Overview */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Exchange Handshakes</CardTitle>
            <CardDescription>Overview of your exchange request counts and status metrics.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div className="bg-zinc-50 p-4 rounded-lg border text-center">
                <p className="text-xs text-zinc-500 font-medium">Sent</p>
                <p className="text-2xl font-bold mt-1 text-zinc-950">{stats?.requestsSent || 0}</p>
              </div>
              <div className="bg-zinc-50 p-4 rounded-lg border text-center">
                <p className="text-xs text-zinc-500 font-medium">Received</p>
                <p className="text-2xl font-bold mt-1 text-zinc-950">{stats?.requestsReceived || 0}</p>
              </div>
              <div className="bg-green-50/50 p-4 rounded-lg border border-green-100 text-center">
                <p className="text-xs text-green-700 font-medium">Accepted</p>
                <p className="text-2xl font-bold mt-1 text-green-950">{stats?.acceptedRequests || 0}</p>
              </div>
              <div className="bg-red-50/50 p-4 rounded-lg border border-red-100 text-center">
                <p className="text-xs text-red-700 font-medium">Rejected</p>
                <p className="text-2xl font-bold mt-1 text-red-950">{stats?.rejectedRequests || 0}</p>
              </div>
            </div>

            <div className="flex items-center space-x-2 text-xs text-zinc-500 bg-zinc-50 p-3 rounded-lg border">
              <GitPullRequest className="h-4 w-4 shrink-0 text-zinc-400" />
              <span>You must have an **ACCEPTED** exchange request or active session to begin chatting.</span>
            </div>
          </CardContent>
        </Card>

        {/* Sessions Calendar Overview */}
        <Card>
          <CardHeader>
            <CardTitle>Sessions Booked</CardTitle>
            <CardDescription>Upcoming and completed virtual meet logs.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-3">
              <div className="flex justify-between items-center border-b pb-2">
                <div className="flex items-center space-x-2">
                  <Calendar className="h-4 w-4 text-zinc-400" />
                  <span className="text-sm text-zinc-650">Upcoming Sessions</span>
                </div>
                <span className="font-bold text-zinc-900">{stats?.upcomingSessions || 0}</span>
              </div>
              
              <div className="flex justify-between items-center border-b pb-2">
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="h-4 w-4 text-zinc-400" />
                  <span className="text-sm text-zinc-650">Completed Session Logs</span>
                </div>
                <span className="font-bold text-zinc-900">{stats?.completedSessions || 0}</span>
              </div>
            </div>

            <Link to="/sessions" className="w-full">
              <Button variant="outline" className="w-full text-xs font-semibold flex items-center justify-center space-x-1 mt-4">
                <span>View Session Calendar</span>
                <ArrowRight className="h-3 w-3" />
              </Button>
            </Link>
          </CardContent>
        </Card>

      </div>

      {/* Quick shortcuts action strip */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Navigation Shortcuts</CardTitle>
          <CardDescription>Quick actions to get started on TalentTrade.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-wrap gap-4">
          <Link to="/skills">
            <Button className="text-xs font-bold flex items-center space-x-1">
              <BookOpen className="h-3 w-3" />
              <span>Manage My Skills</span>
            </Button>
          </Link>
          
          <Link to="/matches">
            <Button variant="outline" className="text-xs font-bold flex items-center space-x-1">
              <Sparkles className="h-3 w-3" />
              <span>Explore Matches</span>
            </Button>
          </Link>

          <Link to="/chat">
            <Button variant="outline" className="text-xs font-bold flex items-center space-x-1">
              <MessageSquare className="h-3 w-3" />
              <span>Open Chat Room</span>
            </Button>
          </Link>

          <Link to="/profile">
            <Button variant="outline" className="text-xs font-bold flex items-center space-x-1">
              <User className="h-3 w-3" />
              <span>Edit Profile</span>
            </Button>
          </Link>
        </CardContent>
      </Card>
    </div>
  );
};

export default DashboardPage;
