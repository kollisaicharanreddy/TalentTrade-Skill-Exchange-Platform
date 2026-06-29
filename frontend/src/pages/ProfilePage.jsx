import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { User, MapPin, FileText, Camera, ShieldAlert } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Textarea } from '../components/ui/Textarea';
import { Avatar, AvatarFallback, AvatarImage } from '../components/ui/Avatar';

export const ProfilePage = () => {
  const { user, updateProfile } = useAuth();
  const [loading, setLoading] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    defaultValues: {
      fullName: '',
      username: '',
      bio: '',
      location: ''
    }
  });

  // Pre-populate form data once user is loaded
  useEffect(() => {
    if (user) {
      reset({
        fullName: user.fullName || '',
        username: user.username || '',
        bio: user.bio || '',
        location: user.location || ''
      });
    }
  }, [user, reset]);

  const onSubmit = async (data) => {
    setLoading(true);
    const result = await updateProfile(data);
    setLoading(false);

    if (result.success) {
      toast.success('Profile updated successfully!');
    } else {
      toast.error(result.message || 'Failed to update profile');
    }
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setAvatarPreview(reader.result);
        toast.info('Avatar selected (UI only - backend upload placeholder)');
      };
      reader.readAsDataURL(file);
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">My Profile</h1>
        <p className="text-sm text-zinc-500">Edit details about yourself and manage avatar previews.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        
        {/* Left Side: Avatar Panel */}
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle>Profile Image</CardTitle>
            <CardDescription>Preview and edit your platform avatar.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-center space-y-4">
            
            <div className="relative group">
              <Avatar className="h-28 w-28 border-2 border-zinc-200">
                <AvatarImage src={avatarPreview} alt="User Avatar" />
                <AvatarFallback className="text-2xl font-bold bg-zinc-100 text-zinc-600">
                  {user?.fullName?.charAt(0) || 'U'}
                </AvatarFallback>
              </Avatar>

              {/* Upload Trigger Input Overlay */}
              <label 
                htmlFor="avatar-input" 
                className="absolute inset-0 bg-black/40 rounded-full flex items-center justify-center cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <Camera className="h-6 w-6 text-white" />
              </label>
              <input 
                id="avatar-input" 
                type="file" 
                accept="image/*" 
                className="hidden" 
                onChange={handleAvatarChange} 
              />
            </div>

            <p className="text-xs text-zinc-400 text-center">
              Click photo to select image. Files are previewed locally in the UI.
            </p>

            <div className="w-full text-xs text-zinc-500 bg-zinc-50 border p-3 rounded-lg flex items-start space-x-2">
              <ShieldAlert className="h-4 w-4 shrink-0 text-zinc-400 mt-0.5" />
              <span>Full Name and Username must match validation rules to update.</span>
            </div>

          </CardContent>
        </Card>

        {/* Right Side: Details Form */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Profile Details</CardTitle>
            <CardDescription>Manage credentials, bio details, and location tags.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              
              <div className="grid gap-4 sm:grid-cols-2">
                {/* Full Name */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                    <User className="h-3 w-3" />
                    <span>Full Name</span>
                  </label>
                  <Input
                    type="text"
                    placeholder="Alice Smith"
                    className={errors.fullName ? 'border-destructive focus-visible:ring-destructive' : ''}
                    {...register('fullName', { 
                      required: 'Full name is required',
                      minLength: { value: 2, message: 'Full name must be at least 2 characters' },
                      maxLength: { value: 50, message: 'Full name must not exceed 50 characters' }
                    })}
                  />
                  {errors.fullName && (
                    <p className="text-xs text-destructive mt-1">{errors.fullName.message}</p>
                  )}
                </div>

                {/* Username */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                    <User className="h-3 w-3" />
                    <span>Username</span>
                  </label>
                  <Input
                    type="text"
                    placeholder="alice_smith"
                    className={errors.username ? 'border-destructive focus-visible:ring-destructive' : ''}
                    {...register('username', { 
                      required: 'Username is required',
                      minLength: { value: 3, message: 'Username must be at least 3 characters' },
                      maxLength: { value: 30, message: 'Username must not exceed 30 characters' },
                      pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Only letters, numbers and underscores allowed' }
                    })}
                  />
                  {errors.username && (
                    <p className="text-xs text-destructive mt-1">{errors.username.message}</p>
                  )}
                </div>
              </div>

              {/* Bio details */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                  <FileText className="h-3 w-3" />
                  <span>Bio Description</span>
                </label>
                <Textarea
                  placeholder="Share a short bio with the community. Mention what skills you specialize in and what you are looking to learn..."
                  rows={4}
                  className={errors.bio ? 'border-destructive focus-visible:ring-destructive' : ''}
                  {...register('bio', { 
                    maxLength: { value: 1000, message: 'Bio must not exceed 1000 characters' }
                  })}
                />
                {errors.bio && (
                  <p className="text-xs text-destructive mt-1">{errors.bio.message}</p>
                )}
              </div>

              {/* Location Tag */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                  <MapPin className="h-3 w-3" />
                  <span>Location</span>
                </label>
                <Input
                  type="text"
                  placeholder="San Francisco, CA"
                  className={errors.location ? 'border-destructive focus-visible:ring-destructive' : ''}
                  {...register('location', { 
                    maxLength: { value: 100, message: 'Location must not exceed 100 characters' }
                  })}
                />
                {errors.location && (
                  <p className="text-xs text-destructive mt-1">{errors.location.message}</p>
                )}
              </div>

              {/* Submit trigger */}
              <div className="flex justify-end pt-2 border-t mt-4">
                <Button 
                  type="submit" 
                  className="font-bold px-6"
                  loading={loading}
                >
                  Save Changes
                </Button>
              </div>

            </form>
          </CardContent>
        </Card>

      </div>
    </div>
  );
};

export default ProfilePage;
