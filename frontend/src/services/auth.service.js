import api from './api';

export const authService = {
  async login(email, password) {
    return api.post('/auth/login', { email, password });
  },

  async register(fullName, username, email, password) {
    return api.post('/auth/register', { fullName, username, email, password });
  },

  async getCurrentUser() {
    return api.get('/users/me');
  },

  async updateProfile(profileData) {
    // profileData: { fullName, username, bio, location }
    return api.put('/users/me', profileData);
  },

  async getAllUsers(page = 0, size = 10, sortBy = 'fullName', direction = 'asc') {
    return api.get('/users', {
      params: { page, size, sortBy, direction }
    });
  },

  async searchUsersBySkill(skill, page = 0, size = 10, sortBy = 'level', direction = 'desc') {
    return api.get('/users/search', {
      params: { skill, page, size, sortBy, direction }
    });
  },

  async verifyEmail(token) {
    return api.get('/auth/verify', { params: { token } });
  },

  async resendVerification(email) {
    return api.post('/auth/resend-verification', { email });
  }
};
