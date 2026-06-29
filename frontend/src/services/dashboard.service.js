import api from './api';

export const dashboardService = {
  async getDashboard() {
    return api.get('/dashboard');
  }
};
