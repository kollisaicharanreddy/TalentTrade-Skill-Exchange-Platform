import api from './api';

export const matchesService = {
  async getMatches(page = 0, size = 10, sortBy = 'id', direction = 'desc') {
    return api.get('/matches', {
      params: { page, size, sortBy, direction }
    });
  },

  async getMatchById(id) {
    return api.get(`/matches/${id}`);
  },

  async refreshMatches() {
    return api.post('/matches/refresh');
  }
};
