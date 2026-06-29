import api from './api';

export const skillsService = {
  // Global Skill Registry
  async getAllSkills(page = 0, size = 100, sortBy = 'name', direction = 'asc') {
    return api.get('/skills', {
      params: { page, size, sortBy, direction }
    });
  },

  async createSkill(skillData) {
    // skillData: { name, category, description }
    return api.post('/skills', skillData);
  },

  async deleteSkill(id) {
    return api.delete(`/skills/${id}`);
  },

  // User Associated Skills (TEACH / LEARN)
  async getUserSkills() {
    return api.get('/users/skills');
  },

  async addUserSkill(skillId, type, level) {
    // type: 'TEACH' | 'LEARN'
    // level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'
    return api.post('/users/skills', { skillId, type, level });
  },

  async removeUserSkill(id) {
    return api.delete(`/users/skills/${id}`);
  }
};
