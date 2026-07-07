import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Compass, User, Mail, Lock, UserCheck } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const RegisterPage = () => {
  const { register: registerAuth } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      fullName: '',
      username: '',
      email: '',
      password: ''
    }
  });

  const onSubmit = async (data) => {
    setLoading(true);
    const result = await registerAuth(data.fullName, data.username, data.email, data.password);
    setLoading(false);

    if (result.success) {
      toast.success(result.message || 'Registration successful! Please log in.');
      navigate('/login');
    } else {
      toast.error(result.message || 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen bg-zinc-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-zinc-200 rounded-xl shadow-lg p-8 space-y-6">
        
        {/* Header Branding */}
        <div className="flex flex-col items-center space-y-2 text-center">
          <Link to="/" className="flex items-center space-x-2">
            <Compass className="h-8 w-8 text-zinc-900 stroke-[2.5]" />
            <span className="font-black text-2xl tracking-tight text-zinc-900">TalentTrade</span>
          </Link>
          <h2 className="text-xl font-bold text-zinc-850 mt-4">Create Account</h2>
          <p className="text-sm text-zinc-500">Sign up to trade skills reciprocally</p>
        </div>

        {/* Register Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          
          {/* Full Name input field */}
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
                minLength: {
                  value: 2,
                  message: 'Full name must be at least 2 characters'
                },
                maxLength: {
                  value: 50,
                  message: 'Full name must not exceed 50 characters'
                }
              })}
            />
            {errors.fullName && (
              <p className="text-xs text-destructive font-medium mt-1">{errors.fullName.message}</p>
            )}
          </div>

          {/* Username input field */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
              <UserCheck className="h-3 w-3" />
              <span>Username</span>
            </label>
            <Input
              type="text"
              placeholder="alice_smith"
              className={errors.username ? 'border-destructive focus-visible:ring-destructive' : ''}
              {...register('username', { 
                required: 'Username is required',
                minLength: {
                  value: 3,
                  message: 'Username must be at least 3 characters'
                },
                maxLength: {
                  value: 30,
                  message: 'Username must not exceed 30 characters'
                },
                pattern: {
                  value: /^[a-zA-Z0-9_]+$/,
                  message: 'Only letters, numbers, and underscores are allowed'
                }
              })}
            />
            {errors.username && (
              <p className="text-xs text-destructive font-medium mt-1">{errors.username.message}</p>
            )}
          </div>

          {/* Email input field */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
              <Mail className="h-3 w-3" />
              <span>Email Address</span>
            </label>
            <Input
              type="email"
              placeholder="name@example.com"
              className={errors.email ? 'border-destructive focus-visible:ring-destructive' : ''}
              {...register('email', { 
                required: 'Email is required',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Invalid email address format'
                }
              })}
            />
            {errors.email && (
              <p className="text-xs text-destructive font-medium mt-1">{errors.email.message}</p>
            )}
          </div>

          {/* Password input field */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
              <Lock className="h-3 w-3" />
              <span>Password</span>
            </label>
            <Input
              type="password"
              placeholder="••••••••"
              className={errors.password ? 'border-destructive focus-visible:ring-destructive' : ''}
              {...register('password', { 
                required: 'Password is required',
                minLength: {
                  value: 6,
                  message: 'Password must be at least 6 characters'
                }
              })}
            />
            {errors.password && (
              <p className="text-xs text-destructive font-medium mt-1">{errors.password.message}</p>
            )}
          </div>

          {/* Submit Action */}
          <Button 
            type="submit" 
            className="w-full font-bold"
            loading={loading}
          >
            Register Account
          </Button>

          {/* OR Divider */}
          <div className="relative flex py-2 items-center">
            <div className="flex-grow border-t border-zinc-200"></div>
            <span className="flex-shrink mx-4 text-zinc-400 text-xs font-semibold uppercase tracking-wider">Or continue with</span>
            <div className="flex-grow border-t border-zinc-200"></div>
          </div>

          {/* Google Login Action */}
          <Button 
            type="button" 
            variant="outline" 
            className="w-full font-bold flex items-center justify-center space-x-2 border-zinc-200 hover:bg-zinc-50"
            onClick={() => {
              const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
              const rootUrl = apiUrl.replace(/\/api$/, '');
              window.location.href = `${rootUrl}/oauth2/authorization/google`;
            }}
          >
            <svg className="h-4 w-4 mr-2" viewBox="0 0 24 24" width="24" height="24" xmlns="http://www.w3.org/2000/svg">
              <path d="M21.35,11.1H12v2.7h5.38C16.88,15.5,15.11,16.8,12,16.8c-3.14,0-5.7-2.56-5.7-5.7s2.56-5.7,5.7-5.7c1.4,0,2.68,0.52,3.68,1.38 l2.02-2.02C16.1,3.28,14.18,2.7,12,2.7C6.86,2.7,2.7,6.86,2.7,12s4.16,9.3,9.3,9.3c5.38,0,9-3.78,9-9.3 C21,11.72,21.35,11.1,21.35,11.1z" fill="#4285F4"/>
            </svg>
            <span>Google</span>
          </Button>

        </form>

        {/* Footer redirection link */}
        <div className="text-center text-sm text-zinc-500 pt-2 border-t border-zinc-150">
          <span>Already have an account? </span>
          <Link to="/login" className="font-semibold text-zinc-900 hover:underline">
            Log In Here
          </Link>
        </div>

      </div>
    </div>
  );
};

export default RegisterPage;
