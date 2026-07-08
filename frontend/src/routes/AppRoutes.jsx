import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import DashboardLayout from '../layouts/DashboardLayout';

// Public pages
import LandingPage from '../pages/LandingPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import OAuth2RedirectHandler from '../pages/OAuth2RedirectHandler';
import EmailVerificationPage from '../pages/EmailVerificationPage';

// Protected pages
import DashboardPage from '../pages/DashboardPage';
import ProfilePage from '../pages/ProfilePage';
import SkillsPage from '../pages/SkillsPage';
import MatchesPage from '../pages/MatchesPage';
import RequestsPage from '../pages/RequestsPage';
import SessionsPage from '../pages/SessionsPage';
import ReviewsPage from '../pages/ReviewsPage';
import NotificationsPage from '../pages/NotificationsPage';
import ChatPage from '../pages/ChatPage';

// Admin pages
import AdminDashboardPage from '../pages/AdminDashboardPage';
import ManageUsersPage from '../pages/ManageUsersPage';
import ManageSkillsPage from '../pages/ManageSkillsPage';
import PlatformAnalyticsPage from '../pages/PlatformAnalyticsPage';
import SystemStatisticsPage from '../pages/SystemStatisticsPage';

// Common error pages
const NotFoundPage = () => (
  <div className="flex h-screen flex-col items-center justify-center bg-zinc-50 p-4 text-center">
    <h1 className="text-6xl font-extrabold text-zinc-900">404</h1>
    <h2 className="mt-4 text-2xl font-bold text-zinc-700">Page Not Found</h2>
    <p className="mt-2 text-zinc-500 max-w-sm">The URL you requested does not exist or may have moved.</p>
    <a href="/" className="mt-6 px-4 py-2 bg-primary text-primary-foreground font-semibold rounded-md shadow hover:bg-primary/90 transition-colors">
      Go back home
    </a>
  </div>
);

const UnauthorizedPage = () => (
  <div className="flex h-screen flex-col items-center justify-center bg-zinc-50 p-4 text-center">
    <h1 className="text-6xl font-extrabold text-red-600">403</h1>
    <h2 className="mt-4 text-2xl font-bold text-zinc-700">Access Denied</h2>
    <p className="mt-2 text-zinc-500 max-w-sm">You do not have permission to view this resource.</p>
    <a href="/login" className="mt-6 px-4 py-2 bg-primary text-primary-foreground font-semibold rounded-md shadow hover:bg-primary/90 transition-colors">
      Log In
    </a>
  </div>
);

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify" element={<EmailVerificationPage />} />
      <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Protected Pages */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <DashboardPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <ProfilePage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/skills"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <SkillsPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/matches"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <MatchesPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/requests"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <RequestsPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/sessions"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <SessionsPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/reviews"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <ReviewsPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <NotificationsPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/chat"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <ChatPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/chat/:userId"
        element={
          <ProtectedRoute>
            <DashboardLayout>
              <ChatPage />
            </DashboardLayout>
          </ProtectedRoute>
        }
      />

      {/* Admin Pages */}
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <DashboardLayout>
              <AdminDashboardPage />
            </DashboardLayout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <AdminRoute>
            <DashboardLayout>
              <ManageUsersPage />
            </DashboardLayout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/skills"
        element={
          <AdminRoute>
            <DashboardLayout>
              <ManageSkillsPage />
            </DashboardLayout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/analytics"
        element={
          <AdminRoute>
            <DashboardLayout>
              <PlatformAnalyticsPage />
            </DashboardLayout>
          </AdminRoute>
        }
      />
      <Route
        path="/admin/health"
        element={
          <AdminRoute>
            <DashboardLayout>
              <SystemStatisticsPage />
            </DashboardLayout>
          </AdminRoute>
        }
      />

      {/* Fallback route */}
      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
};

export default AppRoutes;
