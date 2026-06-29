import api from './api';

export const requestsService = {
  async createRequest(receiverId, message) {
    return api.post('/requests', { receiverId, message });
  },

  async getAllRequests(page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') {
    return api.get('/requests', {
      params: { page, size, sortBy, direction }
    });
  },

  async getSentRequests(page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') {
    return api.get('/requests/sent', {
      params: { page, size, sortBy, direction }
    });
  },

  async getReceivedRequests(page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') {
    return api.get('/requests/received', {
      params: { page, size, sortBy, direction }
    });
  },

  async acceptRequest(id) {
    return api.put(`/requests/${id}/accept`);
  },

  async rejectRequest(id) {
    return api.put(`/requests/${id}/reject`);
  }
};
