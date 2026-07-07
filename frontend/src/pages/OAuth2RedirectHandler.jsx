import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { authService } from '../services/auth.service';
import { toast } from 'react-toastify';
import { Loader2 } from 'lucide-react';

export const OAuth2RedirectHandler = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithToken } = useAuth();

  useEffect(() => {
    const handleRedirect = async () => {
      const token = searchParams.get('token');
      if (token) {
        try {
          // Set token in localStorage temporarily so getCurrentUser request interceptor picks it up
          localStorage.setItem('token', token);
          const response = await authService.getCurrentUser();
          
          if (response && response.success) {
            loginWithToken(token, response.data);
            toast.success('Successfully logged in with Google!');
            navigate('/dashboard');
          } else {
            throw new Error('Could not retrieve user details');
          }
        } catch (error) {
          console.error('OAuth2 login callback error:', error);
          localStorage.removeItem('token');
          toast.error('Google login failed. Please try again.');
          navigate('/login');
        }
      } else {
        toast.error('Invalid login session.');
        navigate('/login');
      }
    };

    handleRedirect();
  }, [searchParams, loginWithToken, navigate]);

  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col items-center justify-center p-6 text-center">
      <div className="space-y-4">
        <Loader2 className="h-10 w-10 text-zinc-900 animate-spin mx-auto" />
        <h2 className="text-xl font-bold text-zinc-800">Authenticating...</h2>
        <p className="text-zinc-500 text-sm">Please wait while we set up your Google session.</p>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
