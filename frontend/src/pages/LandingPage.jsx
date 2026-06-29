import React from 'react';
import { Link } from 'react-router-dom';
import { 
  Compass, 
  Sparkles, 
  Calendar, 
  MessageSquare, 
  Star, 
  ArrowRight,
  ShieldCheck,
  Zap,
  Users
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const LandingPage = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col font-sans">
      {/* Top Header Navbar */}
      <header className="border-b border-zinc-200 bg-white/80 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Compass className="h-6 w-6 text-zinc-900 stroke-[2]" />
            <span className="font-extrabold text-xl tracking-tight text-zinc-900">TalentTrade</span>
          </div>

          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-zinc-600">
            <a href="#features" className="hover:text-zinc-900 transition-colors">Features</a>
            <a href="#how-it-works" className="hover:text-zinc-900 transition-colors">How It Works</a>
            <a href="#community" className="hover:text-zinc-900 transition-colors">Community</a>
          </nav>

          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <Link 
                to="/dashboard" 
                className="px-4 py-2 text-sm font-semibold text-white bg-zinc-900 hover:bg-zinc-800 rounded-md shadow-sm transition-all"
              >
                Go to Dashboard
              </Link>
            ) : (
              <>
                <Link 
                  to="/login" 
                  className="text-sm font-semibold text-zinc-600 hover:text-zinc-900 transition-colors"
                >
                  Log In
                </Link>
                <Link 
                  to="/register" 
                  className="px-4 py-2 text-sm font-semibold text-white bg-zinc-900 hover:bg-zinc-800 rounded-md shadow-sm transition-all"
                >
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-20 lg:py-32 bg-white">
        <div className="max-w-7xl mx-auto px-6 grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-zinc-100 text-zinc-800 text-xs font-semibold">
              <Sparkles className="h-3 w-3 text-zinc-800" />
              <span>Skill-sharing without transaction fees</span>
            </div>
            
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-black text-zinc-900 tracking-tight leading-none">
              Trade your skills, <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-zinc-700 to-zinc-950">
                grow together.
              </span>
            </h1>

            <p className="text-lg text-zinc-600 max-w-lg leading-relaxed">
              TalentTrade connects you with peers who teach what you want to learn, and want to learn what you teach. Empowering reciprocal peer-to-peer knowledge trade.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 pt-2">
              <Link
                to={isAuthenticated ? "/dashboard" : "/register"}
                className="inline-flex items-center justify-center px-6 py-3 font-semibold text-white bg-zinc-900 hover:bg-zinc-800 rounded-md shadow transition-all group"
              >
                <span>Start Trading Talent</span>
                <ArrowRight className="h-4 w-4 ml-2 group-hover:translate-x-1 transition-transform" />
              </Link>
              <a
                href="#how-it-works"
                className="inline-flex items-center justify-center px-6 py-3 font-semibold border border-zinc-300 text-zinc-700 hover:bg-zinc-50 rounded-md transition-colors"
              >
                Learn How It Works
              </a>
            </div>
          </div>

          {/* Visual card demonstration */}
          <div className="relative mx-auto lg:ml-auto w-full max-w-md">
            <div className="absolute inset-0 bg-gradient-to-tr from-zinc-200 to-zinc-50 rounded-2xl transform rotate-3 scale-105 filter blur-xs -z-10" />
            <div className="bg-zinc-950 text-white rounded-2xl p-6 shadow-xl border border-zinc-800 space-y-6">
              <div className="flex items-center justify-between border-b border-zinc-800 pb-4">
                <span className="text-xs font-bold text-zinc-500 uppercase tracking-widest">Active Match</span>
                <span className="inline-flex items-center space-x-1 text-xs text-green-400 font-semibold bg-green-500/10 px-2 py-0.5 rounded-full border border-green-500/20">
                  <span className="h-1.5 w-1.5 rounded-full bg-green-400" />
                  <span>100% Reciprocal</span>
                </span>
              </div>

              {/* Match Example Card */}
              <div className="flex justify-between items-center bg-zinc-900 p-4 rounded-xl border border-zinc-800">
                <div className="space-y-1">
                  <p className="text-sm font-bold text-zinc-200">Alice Smith</p>
                  <p className="text-xs text-zinc-400">Teaches: UI Design</p>
                  <p className="text-xs text-zinc-400">Wants: Spring Boot</p>
                </div>
                <div className="text-zinc-400 text-xl font-black">🤝</div>
                <div className="space-y-1 text-right">
                  <p className="text-sm font-bold text-zinc-200">Bob Miller</p>
                  <p className="text-xs text-zinc-400">Teaches: Spring Boot</p>
                  <p className="text-xs text-zinc-400">Wants: UI Design</p>
                </div>
              </div>

              <div className="space-y-2">
                <div className="h-1 bg-zinc-800 rounded-full">
                  <div className="h-full bg-zinc-200 w-full rounded-full animate-pulse" />
                </div>
                <p className="text-center text-xs text-zinc-400">Connecting Alice & Bob in real-time chat...</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section id="features" className="py-20 bg-zinc-50 border-t border-zinc-200">
        <div className="max-w-7xl mx-auto px-6 space-y-12">
          <div className="text-center space-y-3 max-w-xl mx-auto">
            <h2 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Standard Features for Modern Sharing</h2>
            <p className="text-zinc-600">Built to make peer learning efficient, direct, and collaborative.</p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white p-6 rounded-xl border border-zinc-200 space-y-3 shadow-sm hover:shadow transition-shadow">
              <div className="h-10 w-10 bg-zinc-100 rounded-lg flex items-center justify-center text-zinc-900">
                <Sparkles className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-zinc-900">Reciprocal Matching</h3>
              <p className="text-sm text-zinc-600">Our engine detects dual matches automatically based on reciprocal teaching and learning needs.</p>
            </div>

            <div className="bg-white p-6 rounded-xl border border-zinc-200 space-y-3 shadow-sm hover:shadow transition-shadow">
              <div className="h-10 w-10 bg-zinc-100 rounded-lg flex items-center justify-center text-zinc-900">
                <MessageSquare className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-zinc-900">Real-Time Chat</h3>
              <p className="text-sm text-zinc-600">WebSocket-driven STOMP broker enables instantaneous messaging with active partners.</p>
            </div>

            <div className="bg-white p-6 rounded-xl border border-zinc-200 space-y-3 shadow-sm hover:shadow transition-shadow">
              <div className="h-10 w-10 bg-zinc-100 rounded-lg flex items-center justify-center text-zinc-900">
                <Calendar className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-zinc-900">Virtual Calendar</h3>
              <p className="text-sm text-zinc-600">Schedule meetings, track upcoming events, and access direct meet links with ease.</p>
            </div>

            <div className="bg-white p-6 rounded-xl border border-zinc-200 space-y-3 shadow-sm hover:shadow transition-shadow">
              <div className="h-10 w-10 bg-zinc-100 rounded-lg flex items-center justify-center text-zinc-900">
                <Star className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-zinc-900">Reviews & Ratings</h3>
              <p className="text-sm text-zinc-600">Accumulate credentials and improve rankings via validated post-session feedback loops.</p>
            </div>
          </div>
        </div>
      </section>

      {/* How it works workflow */}
      <section id="how-it-works" className="py-20 bg-white border-t border-zinc-200">
        <div className="max-w-7xl mx-auto px-6 space-y-12">
          <div className="text-center space-y-3 max-w-xl mx-auto">
            <h2 className="text-3xl font-extrabold text-zinc-900 tracking-tight">How It Works</h2>
            <p className="text-zinc-600">A simple, non-monetary handshake model in four phases.</p>
          </div>

          <div className="grid md:grid-cols-4 gap-8 relative">
            <div className="space-y-3">
              <span className="text-4xl font-black text-zinc-200">01</span>
              <h4 className="font-bold text-lg text-zinc-900">Register Profile</h4>
              <p className="text-sm text-zinc-600">Create an account and list the skills you can **TEACH** and what you want to **LEARN**.</p>
            </div>
            <div className="space-y-3">
              <span className="text-4xl font-black text-zinc-200">02</span>
              <h4 className="font-bold text-lg text-zinc-900">Get Reciprocal Matches</h4>
              <p className="text-sm text-zinc-600">Review users automatically calculated by our matching engine and send handshakes.</p>
            </div>
            <div className="space-y-3">
              <span className="text-4xl font-black text-zinc-200">03</span>
              <h4 className="font-bold text-lg text-zinc-900">Live Connect</h4>
              <p className="text-sm text-zinc-600">Once accepted, chat instantly to discuss scope, session guidelines, and schedules.</p>
            </div>
            <div className="space-y-3">
              <span className="text-4xl font-black text-zinc-200">04</span>
              <h4 className="font-bold text-lg text-zinc-900">Schedule & Learn</h4>
              <p className="text-sm text-zinc-600">Book virtual sessions inside the platform. Complete them and write validated feedback.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Core values trust badges */}
      <section id="community" className="py-16 bg-zinc-50 border-t border-zinc-200 text-center">
        <div className="max-w-7xl mx-auto px-6 grid md:grid-cols-3 gap-8">
          <div className="flex flex-col items-center space-y-2">
            <ShieldCheck className="h-8 w-8 text-zinc-900" />
            <h5 className="font-bold text-md text-zinc-900">Validated Users</h5>
            <p className="text-xs text-zinc-500">Self-reviews are strictly barred, enforcing genuine, helpful feedback.</p>
          </div>
          <div className="flex flex-col items-center space-y-2">
            <Zap className="h-8 w-8 text-zinc-900" />
            <h5 className="font-bold text-md text-zinc-900">No Cost Ever</h5>
            <p className="text-xs text-zinc-500">Pure peer trade model. Learn software, design, or languages through exchange.</p>
          </div>
          <div className="flex flex-col items-center space-y-2">
            <Users className="h-8 w-8 text-zinc-900" />
            <h5 className="font-bold text-md text-zinc-900">Empowering Connections</h5>
            <p className="text-xs text-zinc-500">Grow professional relationships, expand portfolios, and build a network.</p>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-zinc-950 text-zinc-400 py-12 border-t border-zinc-800 mt-auto">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-2">
            <Compass className="h-5 w-5 text-zinc-100" />
            <span className="font-bold text-white text-md tracking-tight">TalentTrade</span>
          </div>
          <p className="text-xs text-zinc-500">© 2026 TalentTrade Peer Platform. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default LandingPage;
