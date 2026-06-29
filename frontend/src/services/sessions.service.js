import api from './api';

export const sessionsService = {
  async createSession(sessionData) {
    // sessionData: { exchangeRequestId, mentorId, learnerId, scheduledDate, startTime, endTime, meetingLink, notes }
    return api.post('/sessions', sessionData);
  },

  async getSessions(page = 0, size = 10, sortBy = 'scheduledDate', direction = 'desc') {
    return api.get('/sessions', {
      params: { page, size, sortBy, direction }
    });
  },

  async getSessionDetails(id) {
    return api.get(`/sessions/${id}`);
  },

  async updateSession(id, sessionData) {
    return api.put(`/sessions/${id}`, sessionData);
  },

  async completeSession(id) {
    return api.put(`/sessions/${id}/complete`);
  },

  async cancelSession(id) {
    return api.put(`/sessions/${id}/cancel`);
  },

  async deleteSession(id) {
    return api.delete(`/sessions/${id}`);
  },

  async getUpcomingSessions(page = 0, size = 10, sortBy = 'scheduledDate', direction = 'asc') {
    return api.get('/sessions/upcoming', {
      params: { page, size, sortBy, direction }
    });
  },

  async getCompletedSessions(page = 0, size = 10, sortBy = 'scheduledDate', direction = 'desc') {
    return api.get('/sessions/completed', {
      params: { page, size, sortBy, direction }
    });
  },

  async getSessionHistory(page = 0, size = 10, sortBy = 'scheduledDate', direction = 'desc') {
    return api.get('/sessions/history', {
      params: { page, size, sortBy, direction }
    });
  }
};
