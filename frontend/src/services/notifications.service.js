import api from './api';

export const notificationsService = {
  async getNotifications(page = 0, size = 15, sortBy = 'createdAt', direction = 'desc') {
    return api.get('/notifications', {
      params: { page, size, sortBy, direction }
    });
  },

  async markAsRead(id) {
    return api.put(`/notifications/${id}/read`);
  },

  async markAllAsRead() {
    return api.put('/notifications/read-all');
  },

  async deleteNotification(id) {
    return api.delete(`/notifications/${id}`);
  }
};
