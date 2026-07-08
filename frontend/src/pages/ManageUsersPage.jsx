import React, { useState, useEffect } from 'react';
import { adminService } from '../services/admin.service';
import { Search, ShieldAlert, ShieldCheck, ToggleLeft, ToggleRight, Trash2, Mail, User } from 'lucide-react';

export const ManageUsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [providerFilter, setProviderFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search) params.query = search;
      if (roleFilter) params.role = roleFilter;
      if (providerFilter) params.provider = providerFilter;
      if (statusFilter !== '') params.enabled = statusFilter === 'true';

      const res = await adminService.getUsers(params);
      if (res && res.success) {
        setUsers(res.data);
      } else {
        setError(res.message || 'Failed to load users');
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch users list');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, providerFilter, statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchUsers();
  };

  const toggleUserStatus = async (userId, currentStatus) => {
    try {
      const res = await adminService.setUserStatus(userId, !currentStatus);
      if (res && res.success) {
        setUsers(users.map(u => u.id === userId ? { ...u, enabled: !currentStatus } : u));
      }
    } catch (err) {
      alert('Error updating user status');
    }
  };

  const toggleUserRole = async (userId, currentRole) => {
    const nextRole = currentRole === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!window.confirm(`Are you sure you want to change role to ${nextRole}?`)) return;
    try {
      const res = await adminService.setUserRole(userId, nextRole);
      if (res && res.success) {
        setUsers(users.map(u => u.id === userId ? { ...u, role: nextRole } : u));
      }
    } catch (err) {
      alert('Error changing user role');
    }
  };

  const deleteUser = async (userId) => {
    if (!window.confirm('WARNING: Are you sure you want to delete this user profile permanently?')) return;
    try {
      const res = await adminService.deleteUser(userId);
      if (res && res.success) {
        setUsers(users.filter(u => u.id !== userId));
      }
    } catch (err) {
      alert('Error deleting user');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-950">User Registry Management</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Modify permissions, change roles, deactivate profiles, and delete users from the database.
        </p>
      </div>

      {/* Filters & search controls */}
      <div className="bg-white p-5 rounded-xl border border-zinc-200 shadow-sm space-y-4">
        <form onSubmit={handleSearchSubmit} className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-2.5 h-4.5 w-4.5 text-zinc-400" />
            <input
              type="text"
              placeholder="Search by full name, email, or username..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10 w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>
          <button type="submit" className="bg-zinc-900 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-zinc-800 transition-colors">
            Search
          </button>
        </form>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <label className="block text-xs font-semibold text-zinc-500 mb-1 uppercase">Filter Role</label>
            <select
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">All Roles</option>
              <option value="USER">User</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-zinc-500 mb-1 uppercase">Filter Provider</label>
            <select
              value={providerFilter}
              onChange={(e) => setProviderFilter(e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">All Providers</option>
              <option value="LOCAL">Local Credentials</option>
              <option value="GOOGLE">Google OAuth2</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-zinc-500 mb-1 uppercase">Filter Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">All Statuses</option>
              <option value="true">Active (Enabled)</option>
              <option value="false">Inactive (Disabled)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Users table */}
      <div className="bg-white rounded-xl border border-zinc-200 shadow-sm overflow-hidden">
        {loading ? (
          <div className="flex h-48 items-center justify-center">
            <svg className="animate-spin h-8 w-8 text-indigo-600" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
          </div>
        ) : error ? (
          <div className="p-6 text-center text-red-600">{error}</div>
        ) : users.length === 0 ? (
          <div className="p-12 text-center text-zinc-400 text-sm">No users match the search criteria.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-zinc-200 text-left">
              <thead className="bg-zinc-50">
                <tr>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Full Name</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Username</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Email Address</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Role</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Provider</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Verified</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wider text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-200 bg-white">
                {users.map((u) => (
                  <tr key={u.id}>
                    <td className="whitespace-nowrap px-6 py-4">
                      <div className="flex items-center space-x-3">
                        <div className="h-8 w-8 rounded-full bg-zinc-100 flex items-center justify-center font-bold text-xs text-zinc-500 border border-zinc-200">
                          {u.fullName?.charAt(0) || 'U'}
                        </div>
                        <span className="text-sm font-medium text-zinc-900">{u.fullName}</span>
                      </div>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-zinc-500">@{u.username}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-zinc-500">{u.email}</td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <button
                        onClick={() => toggleUserRole(u.id, u.role)}
                        className={`inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-xs font-medium cursor-pointer transition-colors ${
                          u.role === 'ADMIN' ? 'bg-rose-100 text-rose-800 hover:bg-rose-200' : 'bg-zinc-100 text-zinc-800 hover:bg-zinc-200'
                        }`}
                      >
                        {u.role === 'ADMIN' ? <ShieldAlert className="h-3 w-3 mr-1" /> : <User className="h-3 w-3 mr-1" />}
                        {u.role}
                      </button>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-zinc-500">
                      <span className={`inline-flex px-2 py-0.5 rounded text-xs font-medium ${u.provider === 'GOOGLE' ? 'bg-orange-50 text-orange-700 border border-orange-200' : 'bg-slate-50 text-slate-700 border border-slate-200'}`}>
                        {u.provider}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-zinc-500">
                      <span className={`inline-flex items-center justify-center h-5 w-5 rounded-full ${u.emailVerified ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-800'}`}>
                        {u.emailVerified ? '✓' : '✗'}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <button
                        onClick={() => toggleUserStatus(u.id, u.enabled)}
                        className="text-zinc-500 hover:text-zinc-700 cursor-pointer"
                      >
                        {u.enabled ? (
                          <span className="inline-flex items-center text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded">
                            Active
                          </span>
                        ) : (
                          <span className="inline-flex items-center text-xs font-medium text-red-700 bg-red-50 border border-red-200 px-2 py-0.5 rounded">
                            Inactive
                          </span>
                        )}
                      </button>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => toggleUserStatus(u.id, u.enabled)}
                          title={u.enabled ? 'Deactivate' : 'Activate'}
                          className="text-zinc-400 hover:text-zinc-600"
                        >
                          {u.enabled ? <ToggleRight className="h-5 w-5 text-indigo-600" /> : <ToggleLeft className="h-5 w-5" />}
                        </button>
                        <button
                          onClick={() => deleteUser(u.id)}
                          className="text-red-400 hover:text-red-600"
                          title="Delete User"
                        >
                          <Trash2 className="h-4.5 w-4.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManageUsersPage;
