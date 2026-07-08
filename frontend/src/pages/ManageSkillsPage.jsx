import React, { useState, useEffect } from 'react';
import { adminService } from '../services/admin.service';
import { BookOpen, Plus, Trash2, Library, Award } from 'lucide-react';

export const ManageSkillsPage = () => {
  const [skills, setSkills] = useState([]);
  const [skillUsage, setSkillUsage] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Form fields
  const [newSkillName, setNewSkillName] = useState('');
  const [newSkillCategory, setNewSkillCategory] = useState('');
  const [newSkillDescription, setNewSkillDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const skillsRes = await adminService.getSkills();
      const usageRes = await adminService.getSkillUsage();
      
      if (skillsRes && skillsRes.success) {
        setSkills(skillsRes.data);
      }
      if (usageRes && usageRes.success) {
        setSkillUsage(usageRes.data);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch skill catalog and usage data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!newSkillName || !newSkillCategory) {
      alert('Skill name and category are required.');
      return;
    }
    
    setSubmitting(true);
    try {
      const res = await adminService.addSkill({
        name: newSkillName.trim(),
        category: newSkillCategory.trim(),
        description: newSkillDescription.trim()
      });
      if (res && res.success) {
        setSkills([...skills, res.data]);
        setNewSkillName('');
        setNewSkillCategory('');
        setNewSkillDescription('');
        alert('Skill added successfully!');
      } else {
        alert(res.message || 'Failed to add skill');
      }
    } catch (err) {
      alert('Error adding skill: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteSkill = async (skillId) => {
    if (!window.confirm('Are you sure you want to delete this skill registry entry?')) return;
    try {
      const res = await adminService.deleteSkill(skillId);
      if (res && res.success) {
        setSkills(skills.filter(s => s.id !== skillId));
      }
    } catch (err) {
      alert('Error deleting skill');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-zinc-950">Skill Registry Management</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Add new skill taxonomy entries, remove outdated skill labels, and view teaching/learning skill usage statistics.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Add Skill Form */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm self-start">
          <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2 mb-4">
            <Plus className="h-5 w-5 text-indigo-500" />
            Add New Taxonomy Skill
          </h2>
          <form onSubmit={handleAddSkill} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-zinc-600 mb-1 uppercase">Skill Name</label>
              <input
                type="text"
                placeholder="e.g. Kotlin, Docker, Figma..."
                value={newSkillName}
                onChange={(e) => setNewSkillName(e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                required
              />
            </div>
            
            <div>
              <label className="block text-xs font-semibold text-zinc-600 mb-1 uppercase">Category</label>
              <input
                type="text"
                placeholder="e.g. Programming, Design, Languages..."
                value={newSkillCategory}
                onChange={(e) => setNewSkillCategory(e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-zinc-600 mb-1 uppercase">Description</label>
              <textarea
                placeholder="Brief description of the skill competency..."
                rows="3"
                value={newSkillDescription}
                onChange={(e) => setNewSkillDescription(e.target.value)}
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-zinc-900 text-white py-2 rounded-md text-sm font-medium hover:bg-zinc-800 transition-colors disabled:opacity-50"
            >
              {submitting ? 'Creating...' : 'Register Skill'}
            </button>
          </form>
        </div>

        {/* Skill registry list */}
        <div className="bg-white p-6 rounded-xl border border-zinc-200 shadow-sm lg:col-span-2">
          <h2 className="text-lg font-semibold text-zinc-950 flex items-center gap-2 mb-4">
            <Library className="h-5 w-5 text-indigo-500" />
            Standard Registry & Usage
          </h2>

          {loading ? (
            <div className="flex h-32 items-center justify-center">
              <svg className="animate-spin h-6 w-6 text-indigo-600" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
              </svg>
            </div>
          ) : error ? (
            <div className="text-red-600 text-sm">{error}</div>
          ) : skills.length === 0 ? (
            <div className="text-zinc-400 text-sm text-center py-6">No skills registered yet.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-zinc-200 text-left">
                <thead className="bg-zinc-50">
                  <tr>
                    <th className="px-4 py-2.5 text-xs font-semibold text-zinc-500 uppercase">Skill Name</th>
                    <th className="px-4 py-2.5 text-xs font-semibold text-zinc-500 uppercase">Category</th>
                    <th className="px-4 py-2.5 text-xs font-semibold text-zinc-500 uppercase">Members Teaching/Learning</th>
                    <th className="px-4 py-2.5 text-xs font-semibold text-zinc-500 uppercase text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-200 text-sm">
                  {skills.map((s) => {
                    const usage = skillUsage[s.name] || 0;
                    return (
                      <tr key={s.id} className="hover:bg-zinc-50/50">
                        <td className="px-4 py-3 font-medium text-zinc-900">{s.name}</td>
                        <td className="px-4 py-3 text-zinc-500">
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-indigo-50 text-indigo-700 font-medium">
                            {s.category}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-zinc-500">
                          <span className="font-semibold text-zinc-900">{usage}</span> profile associations
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => handleDeleteSkill(s.id)}
                            className="text-red-400 hover:text-red-600"
                            title="Delete Skill"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ManageSkillsPage;
