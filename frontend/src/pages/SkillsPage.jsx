import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { Search, Plus, Trash2, BookOpen, Compass, Award, Tag, Info } from 'lucide-react';
import { skillsService } from '../services/skills.service';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Textarea } from '../components/ui/Textarea';
import { Select } from '../components/ui/Select';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../components/ui/Dialog';
import { Badge } from '../components/ui/Badge';

export const SkillsPage = () => {
  const [userSkills, setUserSkills] = useState([]);
  const [registrySkills, setRegistrySkills] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);

  // Dialog states
  const [assignDialogOpen, setAssignDialogOpen] = useState(false);
  const [selectedRegistrySkill, setSelectedRegistrySkill] = useState(null);
  const [assignType, setAssignType] = useState('TEACH');
  const [assignLevel, setAssignLevel] = useState('INTERMEDIATE');

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [newSkillName, setNewSkillName] = useState('');
  const [newSkillCategory, setNewSkillCategory] = useState('Programming');
  const [newSkillDesc, setNewSkillDesc] = useState('');

  // Load everything
  const loadSkills = async () => {
    setLoading(true);
    try {
      const userRes = await skillsService.getUserSkills();
      const registryRes = await skillsService.getAllSkills(0, 100);
      
      if (userRes && userRes.success) {
        setUserSkills(userRes.data || []);
      }
      if (registryRes && registryRes.success) {
        setRegistrySkills(registryRes.data.content || []);
      }
    } catch (error) {
      console.error("Failed to load skills:", error);
      toast.error("Failed to load skills data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSkills();
  }, []);

  const handleRemoveUserSkill = async (id) => {
    try {
      const response = await skillsService.removeUserSkill(id);
      if (response && response.success) {
        toast.success("Skill removed from your profile");
        setUserSkills(prev => prev.filter(s => s.id !== id));
      }
    } catch (error) {
      toast.error(error.message || "Failed to remove skill");
    }
  };

  const handleOpenAssign = (skill) => {
    setSelectedRegistrySkill(skill);
    setAssignType('TEACH');
    setAssignLevel('INTERMEDIATE');
    setAssignDialogOpen(true);
  };

  const handleAssignSkill = async () => {
    if (!selectedRegistrySkill) return;
    try {
      const response = await skillsService.addUserSkill(
        selectedRegistrySkill.id,
        assignType,
        assignLevel
      );
      if (response && response.success) {
        toast.success(`Successfully added ${selectedRegistrySkill.name} as a ${assignType.toLowerCase()} skill!`);
        setAssignDialogOpen(false);
        // Reload
        const userRes = await skillsService.getUserSkills();
        if (userRes && userRes.success) {
          setUserSkills(userRes.data || []);
        }
      }
    } catch (error) {
      toast.error(error.message || "Failed to assign skill. Check if it is already assigned.");
    }
  };

  const handleCreateRegistrySkill = async (e) => {
    e.preventDefault();
    if (!newSkillName.trim()) return;
    try {
      const response = await skillsService.createSkill({
        name: newSkillName,
        category: newSkillCategory,
        description: newSkillDesc
      });
      if (response && response.success) {
        toast.success(`Skill "${newSkillName}" added to the global registry!`);
        setCreateDialogOpen(false);
        setNewSkillName('');
        setNewSkillDesc('');
        // Reload global registry
        const registryRes = await skillsService.getAllSkills(0, 100);
        if (registryRes && registryRes.success) {
          setRegistrySkills(registryRes.data.content || []);
        }
      }
    } catch (error) {
      toast.error(error.message || "Failed to create skill. Skill name might already exist.");
    }
  };

  // Filter skills in global registry based on query
  const filteredRegistry = registrySkills.filter(skill => {
    const matchesSearch = skill.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      skill.category.toLowerCase().includes(searchQuery.toLowerCase());
    
    // Do not show skills that are already assigned to the user in BOTH Teach and Learn roles
    const alreadyAssignedTeach = userSkills.some(us => us.skill.id === skill.id && us.type === 'TEACH');
    const alreadyAssignedLearn = userSkills.some(us => us.skill.id === skill.id && us.type === 'LEARN');
    return matchesSearch && !(alreadyAssignedTeach && alreadyAssignedLearn);
  });

  const teachSkills = userSkills.filter(s => s.type === 'TEACH');
  const learnSkills = userSkills.filter(s => s.type === 'LEARN');

  const levelColors = {
    BEGINNER: 'secondary',
    INTERMEDIATE: 'default',
    ADVANCED: 'outline',
    EXPERT: 'default' // We style custom Expert badge classes inside tailwind class name
  };

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Skills Portfolio</h1>
          <p className="text-sm text-zinc-500">Manage what skills you can teach and what you want to learn.</p>
        </div>
      </div>

      {/* Global search and registry section */}
      <Card>
        <CardHeader>
          <CardTitle>Search Skill Registry</CardTitle>
          <CardDescription>Search the universal registry. Click on any skill to add it to your profile.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-zinc-400" />
            <Input
              placeholder="Search by skill name or category... (e.g. Java, Programming, Design)"
              className="pl-9"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {searchQuery && (
            <div className="border rounded-md bg-zinc-50 p-2 max-h-48 overflow-y-auto divide-y">
              {filteredRegistry.length > 0 ? (
                filteredRegistry.map(skill => (
                  <div 
                    key={skill.id} 
                    className="flex justify-between items-center py-2 px-3 hover:bg-zinc-100/80 cursor-pointer rounded transition-colors"
                    onClick={() => handleOpenAssign(skill)}
                  >
                    <div>
                      <p className="text-sm font-semibold text-zinc-900">{skill.name}</p>
                      <p className="text-xs text-zinc-500">{skill.category}</p>
                    </div>
                    <Button size="sm" variant="ghost" className="h-7 text-xs font-bold text-zinc-700">
                      Add to Profile
                    </Button>
                  </div>
                ))
              ) : (
                <div className="text-center py-4 text-xs text-zinc-500">
                  No match found in the universal registry.
                </div>
              )}
            </div>
          )}

        </CardContent>
      </Card>

      {/* Splits grid for Teach vs Learn */}
      <div className="grid gap-6 md:grid-cols-2">
        
        {/* TEACH CARD COLUMN */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between border-b pb-4">
            <div>
              <CardTitle className="text-lg flex items-center space-x-2 text-zinc-900">
                <Compass className="h-5 w-5 text-zinc-500" />
                <span>Skills I Can Teach</span>
              </CardTitle>
              <CardDescription>Listed on your profile as expertise</CardDescription>
            </div>
            <Badge variant="secondary">{teachSkills.length} total</Badge>
          </CardHeader>
          <CardContent className="pt-6">
            {teachSkills.length > 0 ? (
              <div className="space-y-3">
                {teachSkills.map(s => (
                  <div key={s.id} className="flex items-center justify-between p-3 rounded-lg border bg-zinc-50/50 hover:bg-zinc-50 transition-colors">
                    <div className="space-y-1">
                      <p className="text-sm font-bold text-zinc-900">{s.skill.name}</p>
                      <div className="flex items-center space-x-2">
                        <Badge 
                          variant={levelColors[s.level]} 
                          className={s.level === 'EXPERT' ? 'bg-zinc-900 text-white border-transparent' : ''}
                        >
                          {s.level}
                        </Badge>
                        <span className="text-xxs text-zinc-400 uppercase tracking-wider">{s.skill.category}</span>
                      </div>
                    </div>
                    <button 
                      onClick={() => handleRemoveUserSkill(s.id)}
                      className="text-zinc-400 hover:text-red-500 transition-colors p-1.5 rounded-md hover:bg-red-50"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-10 border border-dashed rounded-lg text-zinc-400 flex flex-col items-center space-y-2">
                <Award className="h-8 w-8 text-zinc-300" />
                <p className="text-sm">You haven't listed any teaching skills.</p>
                <p className="text-xs text-zinc-500">Search above to add your expertise.</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* LEARN CARD COLUMN */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between border-b pb-4">
            <div>
              <CardTitle className="text-lg flex items-center space-x-2 text-zinc-900">
                <BookOpen className="h-5 w-5 text-zinc-500" />
                <span>Skills I Want to Learn</span>
              </CardTitle>
              <CardDescription>Listed on your profile as targets</CardDescription>
            </div>
            <Badge variant="secondary">{learnSkills.length} total</Badge>
          </CardHeader>
          <CardContent className="pt-6">
            {learnSkills.length > 0 ? (
              <div className="space-y-3">
                {learnSkills.map(s => (
                  <div key={s.id} className="flex items-center justify-between p-3 rounded-lg border bg-zinc-50/50 hover:bg-zinc-50 transition-colors">
                    <div className="space-y-1">
                      <p className="text-sm font-bold text-zinc-900">{s.skill.name}</p>
                      <div className="flex items-center space-x-2">
                        <Badge variant={levelColors[s.level]}>
                          {s.level}
                        </Badge>
                        <span className="text-xxs text-zinc-400 uppercase tracking-wider">{s.skill.category}</span>
                      </div>
                    </div>
                    <button 
                      onClick={() => handleRemoveUserSkill(s.id)}
                      className="text-zinc-400 hover:text-red-500 transition-colors p-1.5 rounded-md hover:bg-red-50"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-10 border border-dashed rounded-lg text-zinc-400 flex flex-col items-center space-y-2">
                <BookOpen className="h-8 w-8 text-zinc-300" />
                <p className="text-sm">You haven't listed any learning skills.</p>
                <p className="text-xs text-zinc-500">Search above to add targets.</p>
              </div>
            )}
          </CardContent>
        </Card>

      </div>

      {/* DIALOG 1: ASSIGN SKILL LEVEL/TYPE */}
      <Dialog open={assignDialogOpen} onOpenChange={setAssignDialogOpen}>
        <DialogContent onOpenChange={setAssignDialogOpen}>
          <DialogHeader>
            <DialogTitle>Add to My Profile</DialogTitle>
            <DialogDescription>
              Assign "{selectedRegistrySkill?.name}" to your profile with a type and level.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            {/* Type selector */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                <Tag className="h-3 w-3" />
                <span>Exchange Role</span>
              </label>
              <Select value={assignType} onChange={(e) => setAssignType(e.target.value)}>
                <option value="TEACH">TEACH (I will tutor others)</option>
                <option value="LEARN">LEARN (I want to acquire this)</option>
              </Select>
            </div>

            {/* Level selector */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                <Award className="h-3 w-3" />
                <span>Expertise Level</span>
              </label>
              <Select value={assignLevel} onChange={(e) => setAssignLevel(e.target.value)}>
                <option value="BEGINNER">BEGINNER</option>
                <option value="INTERMEDIATE">INTERMEDIATE</option>
                <option value="ADVANCED">ADVANCED</option>
                <option value="EXPERT">EXPERT</option>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setAssignDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleAssignSkill} className="font-bold">
              Add to Profile
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>



    </div>
  );
};

export default SkillsPage;
