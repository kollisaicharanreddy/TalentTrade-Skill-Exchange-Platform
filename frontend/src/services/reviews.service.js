import api from './api';

export const reviewsService = {
  async submitReview(reviewData) {
    // reviewData: { sessionId, rating, comment }
    return api.post('/reviews', reviewData);
  },

  async getReviewsForUser(userId, page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') {
    return api.get(`/reviews/user/${userId}`, {
      params: { page, size, sortBy, direction }
    });
  },

  async getMyReviews(page = 0, size = 10, sortBy = 'createdAt', direction = 'desc') {
    return api.get('/reviews/me', {
      params: { page, size, sortBy, direction }
    });
  },

  async deleteReview(id) {
    return api.delete(`/reviews/${id}`);
  }
};
