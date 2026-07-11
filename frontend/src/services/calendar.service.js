import api from './api';

export const calendarService = {
  async getAuthUrl(redirectUri) {
    return api.get('/calendar/auth-url', {
      params: { redirectUri }
    });
  },

  async exchangeCode(code, redirectUri) {
    return api.post('/calendar/exchange', { code, redirectUri });
  },

  async getStatus() {
    return api.get('/calendar/status');
  },

  async disconnect() {
    return api.post('/calendar/disconnect');
  }
};
