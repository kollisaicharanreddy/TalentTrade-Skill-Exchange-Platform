import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { calendarService } from '../services/calendar.service';
import { Compass, Loader2 } from 'lucide-react';

export const GoogleCallbackPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  useEffect(() => {
    const code = searchParams.get('code');
    const redirectUri = `${window.location.origin}/google-callback`;

    if (!code) {
      setError('Authorization code is missing');
      toast.error('Google authorization code is missing');
      navigate('/sessions');
      return;
    }

    const exchangeCode = async () => {
      try {
        const response = await calendarService.exchangeCode(code, redirectUri);
        if (response && response.success) {
          toast.success('Successfully connected Google Calendar!');
        } else {
          toast.error(response.message || 'Failed to link Google Calendar');
        }
      } catch (err) {
        console.error('Exchange error:', err);
        toast.error(err.message || 'Error exchanging authorization code');
      } finally {
        navigate('/sessions');
      }
    };

    exchangeCode();
  }, [searchParams, navigate]);

  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-zinc-200 rounded-xl shadow-lg p-8 flex flex-col items-center space-y-6">
        <Compass className="h-12 w-12 text-blue-600 animate-spin" />
        <h2 className="text-xl font-bold text-zinc-800">Connecting Google Workspace</h2>
        <p className="text-sm text-zinc-500 text-center">
          Please wait while we secure your calendar connection tokens...
        </p>
        <Loader2 className="h-5 w-5 text-zinc-400 animate-spin" />
      </div>
    </div>
  );
};

export default GoogleCallbackPage;
