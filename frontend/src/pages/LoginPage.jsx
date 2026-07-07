import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { Compass, Key, Mail, AlertCircle, RefreshCw } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { authService } from '../services/auth.service';

export const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [unverifiedEmail, setUnverifiedEmail] = useState('');
  const [resending, setResending] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      email: '',
      password: ''
    }
  });

  const onSubmit = async (data) => {
    setLoading(true);
    setUnverifiedEmail('');
    const result = await login(data.email, data.password);
    setLoading(false);

    if (result.success) {
      toast.success('Successfully logged in!');
      navigate('/dashboard');
    } else {
      toast.error(result.message || 'Login failed');
      if (result.message && (result.message.toLowerCase().includes('verify') || result.message.toLowerCase().includes('verified'))) {
        setUnverifiedEmail(data.email);
      }
    }
  };

  const handleResendVerification = async () => {
    if (!unverifiedEmail) return;
    setResending(true);
    try {
      const response = await authService.resendVerification(unverifiedEmail);
      if (response && response.success) {
        toast.success('Verification email resent successfully! Check your inbox.');
        setUnverifiedEmail('');
      } else {
        toast.error(response.message || 'Failed to resend verification email.');
      }
    } catch (err) {
      toast.error(err.message || 'Failed to resend verification email.');
    } finally {
      setResending(false);
    }
  };

  const handleGoogleLogin = () => {
    const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
    const rootUrl = apiUrl.replace(/\/api$/, '');
    window.location.href = `${rootUrl}/oauth2/authorization/google`;
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

        {/* Unverified Account Warning */}
        {unverifiedEmail && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-md space-y-2">
            <div className="flex items-center space-x-2 text-xs font-semibold">
              <AlertCircle className="h-4 w-4 shrink-0" />
              <span>Verify Your Email</span>
            </div>
            <p className="text-[11px] text-rose-700">You need to confirm your email before logging in. Click below to resend the activation link.</p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleResendVerification}
              loading={resending}
              className="w-full text-rose-800 border-rose-200 hover:bg-rose-100/50 flex items-center justify-center space-x-1 text-xs font-bold"
            >
              <RefreshCw className="h-3 w-3 mr-1" />
              <span>Resend Verification Link</span>
            </Button>
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
            onClick={handleGoogleLogin}
          >
            <svg className="h-4 w-4 mr-2" viewBox="0 0 24 24" width="24" height="24" xmlns="http://www.w3.org/2000/svg">
              <path d="M21.35,11.1H12v2.7h5.38C16.88,15.5,15.11,16.8,12,16.8c-3.14,0-5.7-2.56-5.7-5.7s2.56-5.7,5.7-5.7c1.4,0,2.68,0.52,3.68,1.38 l2.02-2.02C16.1,3.28,14.18,2.7,12,2.7C6.86,2.7,2.7,6.86,2.7,12s4.16,9.3,9.3,9.3c5.38,0,9-3.78,9-9.3 C21,11.72,21.35,11.1,21.35,11.1z" fill="#4285F4"/>
            </svg>
            <span>Google</span>
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
