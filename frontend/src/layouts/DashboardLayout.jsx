import React, { useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  BookOpen, 
  Sparkles, 
  GitPullRequest, 
  Calendar, 
  Star, 
  Bell, 
  MessageSquare, 
  User, 
  LogOut, 
  Menu, 
  X,
  Compass,
  ShieldAlert,
  Users,
  Wrench,
  BarChart3,
  Activity
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useNotifications } from '../hooks/useNotifications';
import { useChat } from '../hooks/useChat';
import { cn } from '../utils/cn';

export const DashboardLayout = ({ children }) => {
  const { user, logout } = useAuth();
  const { unreadCount } = useNotifications();
  const { conversations } = useChat();
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // Sum up total unread messages from conversations
  const totalUnreadMessages = conversations.reduce((sum, c) => sum + (c.unreadCount || 0), 0);

  const menuItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Skills Registry', path: '/skills', icon: BookOpen },
    { name: 'Mutual Matches', path: '/matches', icon: Sparkles },
    { name: 'Exchange Requests', path: '/requests', icon: GitPullRequest },
    { name: 'Sessions', path: '/sessions', icon: Calendar },
    { name: 'Reviews & Ratings', path: '/reviews', icon: Star },
    { 
      name: 'Notifications', 
      path: '/notifications', 
      icon: Bell,
      badge: unreadCount > 0 ? unreadCount : null
    },
    { 
      name: 'Chat Room', 
      path: '/chat', 
      icon: MessageSquare,
      badge: totalUnreadMessages > 0 ? totalUnreadMessages : null
    },
    { name: 'My Profile', path: '/profile', icon: User },
  ];

  if (user?.role === 'ADMIN') {
    menuItems.push(
      { name: 'Admin Dashboard', path: '/admin', icon: ShieldAlert },
      { name: 'Manage Users', path: '/admin/users', icon: Users },
      { name: 'Manage Skills', path: '/admin/skills', icon: Wrench },
      { name: 'Platform Analytics', path: '/admin/analytics', icon: BarChart3 },
      { name: 'System Statistics', path: '/admin/health', icon: Activity }
    );
  }

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const SidebarContent = () => (
    <div className="flex flex-col h-full bg-zinc-950 text-zinc-100 border-r border-zinc-800">
      {/* Platform Title Logo */}
      <div className="flex items-center space-x-2 px-6 py-5 border-b border-zinc-800">
        <Compass className="h-6 w-6 text-primary-foreground stroke-[2.5]" />
        <span className="font-bold text-lg tracking-tight bg-gradient-to-r from-zinc-100 to-zinc-400 bg-clip-text text-transparent">
          TalentTrade
        </span>
      </div>

      {/* User Info Capsule */}
      <div className="px-6 py-4 flex items-center space-x-3 border-b border-zinc-800 bg-zinc-900/30">
        <div className="h-9 w-9 rounded-full bg-zinc-800 flex items-center justify-center font-bold text-sm text-zinc-300 border border-zinc-700">
          {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold truncate text-zinc-200">{user?.fullName}</p>
          <p className="text-xs text-zinc-400 truncate">@{user?.username}</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname.startsWith(item.path);

          return (
            <NavLink
              key={item.name}
              to={item.path}
              onClick={() => setMobileOpen(false)}
              className={cn(
                'flex items-center justify-between px-3 py-2 text-sm font-medium rounded-md transition-all group',
                isActive 
                  ? 'bg-zinc-800 text-white font-semibold' 
                  : 'text-zinc-400 hover:text-zinc-100 hover:bg-zinc-900/50'
              )}
            >
              <div className="flex items-center space-x-3">
                <Icon className={cn('h-4 w-4 shrink-0 transition-colors', isActive ? 'text-white' : 'text-zinc-500 group-hover:text-zinc-300')} />
                <span>{item.name}</span>
              </div>
              {item.badge ? (
                <span className="inline-flex items-center justify-center px-1.5 py-0.5 rounded-full text-xxs font-bold bg-primary text-primary-foreground min-w-[1.25rem] text-center">
                  {item.badge}
                </span>
              ) : null}
            </NavLink>
          );
        })}
      </nav>

      {/* Logout Action Button */}
      <div className="p-4 border-t border-zinc-800">
        <button
          onClick={handleLogout}
          className="flex items-center space-x-3 w-full px-3 py-2 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-zinc-900/60 rounded-md transition-colors"
        >
          <LogOut className="h-4 w-4 shrink-0" />
          <span>Logout Session</span>
        </button>
      </div>
    </div>
  );

  return (
    <div className="flex h-screen overflow-hidden bg-zinc-50">
      {/* Desktop Sidebar (Left side, fixed width) */}
      <div className="hidden md:flex md:w-64 md:flex-col md:shrink-0">
        <SidebarContent />
      </div>

      {/* Main Workspace (Takes remainder of screen) */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Mobile Header navbar */}
        <header className="flex items-center justify-between md:hidden bg-zinc-950 text-white px-4 py-3 border-b border-zinc-800 shrink-0">
          <div className="flex items-center space-x-2">
            <Compass className="h-5 w-5 text-zinc-300" />
            <span className="font-bold text-md tracking-tight">TalentTrade</span>
          </div>
          <button 
            onClick={() => setMobileOpen(true)}
            className="p-1 rounded-md text-zinc-400 hover:text-zinc-100 focus:outline-none focus:ring-1 focus:ring-zinc-700"
          >
            <Menu className="h-6 w-6" />
          </button>
        </header>

        {/* Content Pane */}
        <main className="flex-1 overflow-y-auto focus:outline-none p-6 md:p-8">
          <div className="max-w-7xl mx-auto space-y-6">
            {children}
          </div>
        </main>
      </div>

      {/* Mobile Drawer (Slide-out navigation overlay) */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 md:hidden flex">
          {/* Backdrop opacity */}
          <div 
            className="fixed inset-0 bg-black/60 transition-opacity" 
            onClick={() => setMobileOpen(false)}
          />
          {/* Drawer container */}
          <div className="relative flex-1 flex flex-col max-w-xs w-full bg-zinc-950 animate-in slide-in-from-left duration-200">
            <div className="absolute top-2 right-2">
              <button 
                onClick={() => setMobileOpen(false)}
                className="p-2 text-zinc-400 hover:text-zinc-100 rounded-md focus:outline-none"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <SidebarContent />
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardLayout;
