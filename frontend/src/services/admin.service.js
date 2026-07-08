import api from './api';

export const adminService = {
  async getSummary() {
    return api.get('/admin/summary');
  },
  
  async getAnalytics() {
    return api.get('/admin/analytics');
  },
  
  async getUsers(params) {
    // params: { query, role, provider, enabled }
    return api.get('/admin/users', { params });
  },
  
  async setUserStatus(id, enabled) {
    return api.patch(`/admin/users/${id}/status`, null, { params: { enabled } });
  },
  
  async setUserRole(id, role) {
    return api.patch(`/admin/users/${id}/role`, null, { params: { role } });
  },
  
  async deleteUser(id) {
    return api.delete(`/admin/users/${id}`);
  },
  
  async getSkills() {
    return api.get('/admin/skills');
  },
  
  async addSkill(skill) {
    // skill: { name, category, description }
    return api.post('/admin/skills', skill);
  },
  
  async deleteSkill(id) {
    return api.delete(`/admin/skills/${id}`);
  },
  
  async getSkillUsage() {
    return api.get('/admin/skills/usage');
  },
  
  async getHealth() {
    return api.get('/admin/health');
  }
};

export default adminService;
