import api from './api';

export const chatService = {
  async getChatHistory(userId, page = 0, size = 50, sortBy = 'sentAt', direction = 'asc') {
    return api.get(`/chat/history/${userId}`, {
      params: { page, size, sortBy, direction }
    });
  },

  async getConversations(page = 0, size = 20, sortBy = 'id', direction = 'desc') {
    return api.get('/chat/conversations', {
      params: { page, size, sortBy, direction }
    });
  },

  async markAsRead(messageId) {
    return api.put(`/chat/read/${messageId}`);
  },

  async deleteMessage(messageId) {
    return api.delete(`/chat/${messageId}`);
  }
};
