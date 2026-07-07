import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authService } from '../services/auth.service';
import { toast } from 'react-toastify';
import { CheckCircle2, XCircle, Loader2, Mail, ArrowRight, Compass } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export const EmailVerificationPage = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('verifying'); // verifying, success, error
  const [errorMessage, setErrorMessage] = useState('');
  const [resendEmail, setResendEmail] = useState('');
  const [resending, setResending] = useState(false);

  const token = searchParams.get('token');

  useEffect(() => {
    const performVerification = async () => {
      if (!token) {
        setStatus('error');
        setErrorMessage('No verification token provided. Please check your verification link.');
        return;
      }

      try {
        const response = await authService.verifyEmail(token);
        if (response && response.success) {
          setStatus('success');
        } else {
          setStatus('error');
          setErrorMessage(response.message || 'Verification failed. The token may be invalid or expired.');
        }
      } catch (error) {
        setStatus('error');
        setErrorMessage(error.message || 'Verification failed. The token may be invalid or expired.');
      }
    };

    performVerification();
  }, [token]);

  const handleResend = async (e) => {
    e.preventDefault();
    if (!resendEmail) {
      toast.error('Please enter your email address');
      return;
    }

    setResending(true);
    try {
      const response = await authService.resendVerification(resendEmail);
      if (response && response.success) {
        toast.success('Verification email resent successfully! Check your inbox.');
      } else {
        toast.error(response.message || 'Failed to resend verification email.');
      }
    } catch (error) {
      toast.error(error.message || 'Failed to resend verification email.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-zinc-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-zinc-200 rounded-xl shadow-lg p-8 space-y-6">
        
        {/* Logo Branding */}
        <div className="flex flex-col items-center space-y-2 text-center">
          <Link to="/" className="flex items-center space-x-2">
            <Compass className="h-8 w-8 text-zinc-900 stroke-[2.5]" />
            <span className="font-black text-2xl tracking-tight text-zinc-900">TalentTrade</span>
          </Link>
        </div>

        {status === 'verifying' && (
          <div className="flex flex-col items-center justify-center py-6 text-center space-y-4">
            <Loader2 className="h-12 w-12 text-indigo-600 animate-spin" />
            <h3 className="text-xl font-bold text-zinc-800">Verifying Account</h3>
            <p className="text-zinc-500 text-sm">We are communicating with the server to activate your account. Just a moment...</p>
          </div>
        )}

        {status === 'success' && (
          <div className="flex flex-col items-center justify-center py-4 text-center space-y-4">
            <div className="h-16 w-16 bg-emerald-50 rounded-full flex items-center justify-center border border-emerald-100">
              <CheckCircle2 className="h-10 w-10 text-emerald-600" />
            </div>
            <h3 className="text-2xl font-bold text-zinc-850">Verification Complete!</h3>
            <p className="text-zinc-500 text-sm">Your account has been successfully verified. You can now log in and explore the platform.</p>
            <Link to="/login" className="w-full">
              <Button className="w-full font-bold flex items-center justify-center space-x-2">
                <span>Go to Log In</span>
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </div>
        )}

        {status === 'error' && (
          <div className="space-y-6">
            <div className="flex flex-col items-center justify-center py-2 text-center space-y-3">
              <div className="h-16 w-16 bg-rose-50 rounded-full flex items-center justify-center border border-rose-100">
                <XCircle className="h-10 w-10 text-rose-600" />
              </div>
              <h3 className="text-xl font-bold text-zinc-855 text-zinc-800">Verification Failed</h3>
              <p className="text-rose-700 bg-rose-50/50 border border-rose-100 p-3 rounded-lg text-xs font-medium w-full text-center">
                {errorMessage}
              </p>
            </div>

            <div className="border-t border-zinc-150 pt-4 space-y-4">
              <div className="text-center">
                <h4 className="text-sm font-bold text-zinc-800">Need a new link?</h4>
                <p className="text-xs text-zinc-500 mt-1">Enter your registration email below, and we will send you a new verification link.</p>
              </div>

              <form onSubmit={handleResend} className="space-y-3">
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider flex items-center space-x-1">
                    <Mail className="h-3 w-3" />
                    <span>Email Address</span>
                  </label>
                  <Input
                    type="email"
                    placeholder="name@example.com"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    required
                  />
                </div>
                <Button type="submit" className="w-full font-bold" loading={resending}>
                  Resend Verification Email
                </Button>
              </form>
            </div>

            <div className="text-center text-xs">
              <Link to="/login" className="text-zinc-500 hover:text-zinc-800 underline font-medium">
                Back to Log In
              </Link>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default EmailVerificationPage;
