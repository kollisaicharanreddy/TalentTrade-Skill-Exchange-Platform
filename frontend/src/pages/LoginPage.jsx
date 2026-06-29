import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Compass, Key, Mail, AlertCircle } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      email: '',
      password: ''
    }
  });

  const onSubmit = async (data) => {
    setLoading(true);
    const result = await login(data.email, data.password);
    setLoading(false);

    if (result.success) {
      toast.success('Successfully logged in!');
      navigate('/dashboard');
    } else {
      toast.error(result.message || 'Login failed');
    }
  };

  const expiredSession = searchParams.get('expired');

  return (
    <div className="min-h-screen bg-zinc-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-zinc-200 rounded-xl shadow-lg p-8 space-y-6">
        
        {/* Header Branding */}
        <div className="flex flex-col items-center space-y-2 text-center">
          <Link to="/" className="flex items-center space-x-2">
            <Compass className="h-8 w-8 text-zinc-900 stroke-[2.5]" />
            <span className="font-black text-2xl tracking-tight text-zinc-900">TalentTrade</span>
          </Link>
          <h2 className="text-xl font-bold text-zinc-850 mt-4">Welcome Back</h2>
          <p className="text-sm text-zinc-500">Access your skill exchange space</p>
        </div>

        {/* Session Expired Prompt */}
        {expiredSession && (
          <div className="flex items-center space-x-2 p-3 bg-amber-50 border border-amber-200 text-amber-800 text-xs rounded-md">
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>Your session has expired. Please log in again.</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          
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
            <div className="flex justify-between items-center">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                <Key className="h-3 w-3" />
                <span>Password</span>
              </label>
            </div>
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
            Log In
          </Button>

        </form>

        {/* Footer redirection link */}
        <div className="text-center text-sm text-zinc-500 pt-2 border-t border-zinc-150">
          <span>Don't have an account? </span>
          <Link to="/register" className="font-semibold text-zinc-900 hover:underline">
            Register Here
          </Link>
        </div>

      </div>
    </div>
  );
};

export default LoginPage;
