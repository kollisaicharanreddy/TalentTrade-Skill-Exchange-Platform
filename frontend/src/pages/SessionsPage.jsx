import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { Calendar, Video, CheckCircle2, AlertTriangle, Clock, Plus, HelpCircle, User, Star, Trash2 } from 'lucide-react';
import { sessionsService } from '../services/sessions.service';
import { requestsService } from '../services/requests.service';
import { reviewsService } from '../services/reviews.service';
import { useAuth } from '../hooks/useAuth';
import { calendarService } from '../services/calendar.service';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/Tabs';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../components/ui/Dialog';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Textarea } from '../components/ui/Textarea';
import { Select } from '../components/ui/Select';
import { Skeleton } from '../components/ui/Skeleton';

export const SessionsPage = () => {
  const { user } = useAuth();
  const [sessions, setSessions] = useState([]);
  const [acceptedRequests, setAcceptedRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('UPCOMING');

  // Google Calendar integration state
  const [isCalendarConnected, setIsCalendarConnected] = useState(false);
  const [isCalendarLoading, setIsCalendarLoading] = useState(true);
  const [autoCreateMeet, setAutoCreateMeet] = useState(true);

  // Schedule modal state
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [selectedReqId, setSelectedReqId] = useState('');
  const [teachOption, setTeachOption] = useState('MYSELF'); // MYSELF | PARTNER
  const [schedDate, setSchedDate] = useState('');
  const [schedStart, setSchedStart] = useState('');
  const [schedEnd, setSchedEnd] = useState('');
  const [schedLink, setSchedLink] = useState('https://meet.jit.si/TalentTradeSession');
  const [schedNotes, setSchedNotes] = useState('');
  const [scheduleLoading, setScheduleLoading] = useState(false);

  const checkCalendarStatus = async () => {
    try {
      const res = await calendarService.getStatus();
      setIsCalendarConnected(res.connected);
    } catch (error) {
      console.error("Failed to check calendar status:", error);
    } finally {
      setIsCalendarLoading(false);
    }
  };

  const handleConnectCalendar = async () => {
    try {
      const redirectUri = `${window.location.origin}/google-callback`;
      const res = await calendarService.getAuthUrl(redirectUri);
      if (res && res.url) {
        window.location.href = res.url;
      }
    } catch (error) {
      toast.error("Failed to generate Google connection URL");
    }
  };

  const handleDisconnectCalendar = async () => {
    if (!window.confirm("Are you sure you want to disconnect Google Calendar integration?")) return;
    try {
      const res = await calendarService.disconnect();
      if (res && res.success) {
        toast.success("Google Calendar disconnected");
        setIsCalendarConnected(false);
      }
    } catch (error) {
      toast.error("Failed to disconnect calendar");
    }
  };

  // Review modal state
  const [reviewOpen, setReviewOpen] = useState(false);
  const [reviewSessionId, setReviewSessionId] = useState(null);
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewLoading, setReviewLoading] = useState(false);

  const fetchSessions = async () => {
    try {
      let response;
      if (activeTab === 'UPCOMING') {
        response = await sessionsService.getUpcomingSessions(0, 50);
      } else if (activeTab === 'COMPLETED') {
        response = await sessionsService.getCompletedSessions(0, 50);
      } else {
        response = await sessionsService.getSessionHistory(0, 50);
      }

      if (response && response.success) {
        setSessions(response.data.content || []);
      }
    } catch (error) {
      console.error("Failed to load sessions:", error);
      toast.error("Failed to load sessions data");
    } finally {
      setLoading(false);
    }
  };

  const fetchAcceptedRequests = async () => {
    try {
      const response = await requestsService.getAllRequests(0, 100);
      if (response && response.success) {
        const list = response.data.content || [];
        const accepted = list.filter(r => r.status === 'ACCEPTED');
        setAcceptedRequests(accepted);
        if (accepted.length > 0) {
          setSelectedReqId(accepted[0].id.toString());
        }
      }
    } catch (error) {
      console.error("Failed to fetch accepted requests:", error);
    }
  };

  useEffect(() => {
    fetchSessions();
  }, [activeTab]);

  useEffect(() => {
    fetchAcceptedRequests();
    checkCalendarStatus();
  }, []);

  const handleCreateSession = async (e) => {
    e.preventDefault();
    const isMeet = autoCreateMeet && isCalendarConnected && teachOption === 'MYSELF';
    if (!selectedReqId || !schedDate || !schedStart || !schedEnd || (!schedLink && !isMeet)) {
      toast.error("Please fill in all required fields");
      return;
    }

    const req = acceptedRequests.find(r => r.id.toString() === selectedReqId);
    if (!req) return;

    const partner = req.sender.id === user.id ? req.receiver : req.sender;
    
    // Set mentor/learner based on teaching option
    const mentorId = teachOption === 'MYSELF' ? user.id : partner.id;
    const learnerId = teachOption === 'MYSELF' ? partner.id : user.id;

    setScheduleLoading(true);
    try {
      const payload = {
        exchangeRequestId: req.id,
        mentorId,
        learnerId,
        scheduledDate: schedDate,
        startTime: `${schedStart}:00`, // Append seconds to match LocalTime format (HH:MM:SS)
        endTime: `${schedEnd}:00`,
        meetingLink: isMeet ? '' : schedLink,
        notes: schedNotes
      };

      const response = await sessionsService.createSession(payload);
      if (response && response.success) {
        toast.success("Virtual session scheduled successfully!");
        setScheduleOpen(false);
        // Clear inputs
        setSchedDate('');
        setSchedStart('');
        setSchedEnd('');
        setSchedNotes('');
        await fetchSessions();
      }
    } catch (error) {
      toast.error(error.message || "Failed to schedule session. Check for schedule overlaps.");
    } finally {
      setScheduleLoading(false);
    }
  };

  const handleComplete = async (id) => {
    try {
      const response = await sessionsService.completeSession(id);
      if (response && response.success) {
        toast.success("Session marked as completed!");
        setReviewSessionId(id);
        setReviewRating(5);
        setReviewComment('');
        setReviewOpen(true); // Open review prompt immediately
        await fetchSessions();
      }
    } catch (error) {
      toast.error(error.message || "Failed to complete session");
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this session? This will notify your partner.")) return;
    try {
      const response = await sessionsService.cancelSession(id);
      if (response && response.success) {
        toast.success("Session has been cancelled");
        await fetchSessions();
      }
    } catch (error) {
      toast.error(error.message || "Failed to cancel session");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this session entirely from history?")) return;
    try {
      const response = await sessionsService.deleteSession(id);
      if (response && response.success) {
        toast.success("Session deleted successfully");
        setSessions(prev => prev.filter(s => s.id !== id));
      }
    } catch (error) {
      toast.error(error.message || "Failed to delete session");
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    setReviewLoading(true);
    try {
      const response = await reviewsService.submitReview({
        sessionId: reviewSessionId,
        rating: Number(reviewRating),
        comment: reviewComment
      });
      if (response && response.success) {
        toast.success("Feedback submitted successfully!");
        setReviewOpen(false);
      }
    } catch (error) {
      toast.error(error.message || "Failed to submit review");
    } finally {
      setReviewLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      SCHEDULED: 'bg-blue-50 text-blue-700 border-blue-150',
      COMPLETED: 'bg-green-50 text-green-700 border-green-150',
      CANCELLED: 'bg-red-50 text-red-700 border-red-150'
    };
    return <Badge className={styles[status]}>{status}</Badge>;
  };

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Virtual Sessions</h1>
          <p className="text-sm text-zinc-500">Schedule meetings, join video link paths, and leave feedback.</p>
        </div>
        <Button 
          onClick={() => {
            if (acceptedRequests.length === 0) {
              toast.warn("You need an ACCEPTED exchange request to schedule a session.");
              return;
            }
            setScheduleOpen(true);
          }}
          className="font-bold flex items-center space-x-1 sm:self-end"
        >
          <Plus className="h-4 w-4" />
          <span>Schedule Session</span>
        </Button>
      </div>

      {/* Google Calendar Connection Status Banner */}
      <Card className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100">
        <CardContent className="p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 bg-blue-600 rounded-lg text-white shrink-0">
              <Calendar className="h-5.5 w-5.5" />
            </div>
            <div>
              <h3 className="font-bold text-zinc-900 text-sm">Google Calendar & Meet Integration</h3>
              <p className="text-xs text-zinc-500 max-w-lg mt-0.5 leading-relaxed">
                Link your Google Calendar to automatically generate Google Meet URLs and send out calendar invitations to your exchange partners when scheduling sessions.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {isCalendarLoading ? (
              <span className="text-xs text-zinc-400 font-medium">Loading connection...</span>
            ) : isCalendarConnected ? (
              <>
                <Badge className="bg-green-50 text-green-700 border-green-150">Connected</Badge>
                <Button size="sm" variant="outline" onClick={handleDisconnectCalendar} className="text-xs border-red-200 text-red-500 hover:bg-red-50">
                  Disconnect
                </Button>
              </>
            ) : (
              <Button size="sm" onClick={handleConnectCalendar} className="text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white">
                Connect Google Calendar
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full max-w-md grid-cols-3">
          <TabsTrigger value="UPCOMING">Upcoming</TabsTrigger>
          <TabsTrigger value="COMPLETED">Completed</TabsTrigger>
          <TabsTrigger value="ALL">All (History)</TabsTrigger>
        </TabsList>

        <TabsContent value={activeTab} className="mt-6">
          {loading ? (
            <div className="space-y-4">
              <Skeleton className="h-32 w-full" />
              <Skeleton className="h-32 w-full" />
            </div>
          ) : sessions.length > 0 ? (
            <div className="space-y-4">
              {sessions.map((s) => {
                const isMentor = s.mentor.id === user.id;
                const partner = isMentor ? s.learner : s.mentor;
                
                return (
                  <Card key={s.id} className="hover:shadow-sm transition-shadow">
                    <CardContent className="p-5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                      
                      {/* Left Side: Session Meta details */}
                      <div className="space-y-3 min-w-0">
                        <div className="flex items-center space-x-2 flex-wrap">
                          <span className="font-bold text-sm text-zinc-900">
                            {isMentor ? `Teaching ${partner.fullName}` : `Learning from ${partner.fullName}`}
                          </span>
                          <span className="text-xs text-zinc-400">@{partner.username}</span>
                          {getStatusBadge(s.status)}
                        </div>

                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-y-2 gap-x-4 text-xs text-zinc-500">
                          <div className="flex items-center space-x-1">
                            <Calendar className="h-3.5 w-3.5 text-zinc-400" />
                            <span>{s.scheduledDate}</span>
                          </div>
                          <div className="flex items-center space-x-1">
                            <Clock className="h-3.5 w-3.5 text-zinc-400" />
                            <span>{s.startTime.substring(0, 5)} - {s.endTime.substring(0, 5)}</span>
                          </div>
                          <div className="flex items-center space-x-1 col-span-2 sm:col-span-1">
                            <Badge variant="outline">
                              {isMentor ? 'Role: Mentor' : 'Role: Learner'}
                            </Badge>
                          </div>
                        </div>

                        {s.notes && (
                          <p className="text-xs text-zinc-500 italic max-w-xl truncate">
                            Notes: "{s.notes}"
                          </p>
                        )}

                        {s.googleEventId && (
                          <div className="flex items-center space-x-1.5 text-xxs text-blue-600 bg-blue-50 px-2 py-0.5 rounded border border-blue-100 font-semibold max-w-fit mt-1">
                            <Video className="h-3 w-3 animate-pulse" />
                            <span>Synced to Google Calendar (Meet link generated & invitations sent via calendar)</span>
                          </div>
                        )}
                      </div>

                      {/* Right Side: Join, Complete, Cancel actions */}
                      <div className="flex flex-wrap items-center gap-2 shrink-0 self-end md:self-center">
                        
                        {/* SCHEDULED Sessions */}
                        {s.status === 'SCHEDULED' && (
                          <>
                            <a 
                              href={s.meetingLink} 
                              target="_blank" 
                              rel="noopener noreferrer"
                            >
                              <Button 
                                size="sm" 
                                className="text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white flex items-center space-x-1"
                              >
                                <Video className="h-3.5 w-3.5" />
                                <span>Join Session</span>
                              </Button>
                            </a>
                            
                            <Button 
                              size="sm"
                              onClick={() => handleComplete(s.id)}
                              className="text-xs font-bold bg-green-600 hover:bg-green-700 text-white flex items-center space-x-0.5"
                            >
                              <CheckCircle2 className="h-3.5 w-3.5" />
                              <span>Complete</span>
                            </Button>

                            <Button 
                              size="sm"
                              variant="outline"
                              onClick={() => handleCancel(s.id)}
                              className="text-xs font-semibold text-red-500 hover:bg-red-50 hover:text-red-600 border-red-200"
                            >
                              Cancel
                            </Button>
                          </>
                        )}

                        {/* COMPLETED Sessions: Allow reviewing if not already done */}
                        {s.status === 'COMPLETED' && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              setReviewSessionId(s.id);
                              setReviewRating(5);
                              setReviewComment('');
                              setReviewOpen(true);
                            }}
                            className="text-xs font-semibold flex items-center space-x-1"
                          >
                            <Star className="h-3.5 w-3.5 text-amber-500" />
                            <span>Leave Review</span>
                          </Button>
                        )}

                        {/* CANCELLED or older sessions can be deleted from logs */}
                        {s.status !== 'SCHEDULED' && (
                          <button
                            onClick={() => handleDelete(s.id)}
                            className="text-zinc-400 hover:text-red-500 transition-colors p-1.5 rounded-md hover:bg-red-50"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        )}

                      </div>

                    </CardContent>
                  </Card>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-20 border border-dashed rounded-xl bg-white text-zinc-400 flex flex-col items-center space-y-2">
              <Calendar className="h-8 w-8 text-zinc-300" />
              <p className="text-sm font-semibold">No sessions found in this category.</p>
              <p className="text-xs text-zinc-500">Go to **Mutual Matches** to request exchanges and schedule events.</p>
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* SCHEDULE SESSION MODAL */}
      <Dialog open={scheduleOpen} onOpenChange={setScheduleOpen}>
        <DialogContent onOpenChange={setScheduleOpen}>
          <DialogHeader>
            <DialogTitle>Schedule Exchange Session</DialogTitle>
            <DialogDescription>
              Organize a virtual meeting with an accepted exchange partner. Date and conflict checks will run.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCreateSession} className="space-y-4 py-2">
            
            {/* Multi-Session coordination info */}
            <div className="p-3 bg-zinc-50 border border-zinc-200 text-zinc-650 rounded-lg text-xxs leading-relaxed space-y-1">
              <span className="font-bold text-zinc-800 block">🔄 Multi-Session Scheduling Supported:</span>
              <p>
                You can now schedule multiple exchange sessions under a single accepted request handshake (e.g., coordinate separate slots for both teaching and learning reciprocal sessions).
              </p>
            </div>

            {/* Accepted Partner selector */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Exchange Partner</label>
              <Select 
                value={selectedReqId} 
                onChange={(e) => setSelectedReqId(e.target.value)}
                required
              >
                {acceptedRequests.map(r => {
                  const partner = r.sender.id === user.id ? r.receiver : r.sender;
                  return (
                    <option key={r.id} value={r.id.toString()}>
                      {partner.fullName} (@{partner.username})
                    </option>
                  );
                })}
              </Select>
            </div>

            {/* Mentor vs Learner selector option */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider flex items-center space-x-1">
                <User className="h-3.5 w-3.5" />
                <span>Who is teaching (Mentor)?</span>
              </label>
              <Select value={teachOption} onChange={(e) => setTeachOption(e.target.value)}>
                <option value="MYSELF">Myself (I will mentor this session)</option>
                <option value="PARTNER">Partner (I will learn this session)</option>
              </Select>
            </div>

            {/* Date selector */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Scheduled Date</label>
              <Input
                type="date"
                value={schedDate}
                onChange={(e) => setSchedDate(e.target.value)}
                min={new Date().toISOString().split('T')[0]} // Block yesterday
                required
              />
            </div>

            {/* Time windows */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Start Time</label>
                <Input
                  type="time"
                  value={schedStart}
                  onChange={(e) => setSchedStart(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">End Time</label>
                <Input
                  type="time"
                  value={schedEnd}
                  onChange={(e) => setSchedEnd(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Google Meet & Calendar notice */}
            <div className="p-3 bg-blue-50 border border-blue-150 text-blue-700 rounded-lg text-xs leading-relaxed space-y-1">
              <div className="font-bold flex items-center space-x-1.5 text-blue-800">
                <Calendar className="h-4 w-4" />
                <span>Google Calendar Sync details:</span>
              </div>
              <p className="text-zinc-650 text-xxs leading-normal">
                {isCalendarConnected ? (
                  teachOption === 'MYSELF' ? (
                    <span className="text-green-700 font-medium">✓ You are teaching this session and have connected Google Calendar. A Google Meet link will be generated automatically, and invites will be emailed.</span>
                  ) : (
                    <span>Note: Since your partner is teaching, Google Calendar sync and Google Meet link generation will only occur if your partner has connected their Google Calendar account.</span>
                  )
                ) : (
                  <span>You have not connected Google Calendar. Fall back to manual meeting links, or close this and click 'Connect Google Calendar' first.</span>
                )}
              </p>
            </div>

            {isCalendarConnected && teachOption === 'MYSELF' && (
              <div className="flex items-center space-x-2 p-2 bg-zinc-50 border border-zinc-200 rounded-lg">
                <input
                  type="checkbox"
                  id="autoCreateMeet"
                  checked={autoCreateMeet}
                  onChange={(e) => setAutoCreateMeet(e.target.checked)}
                  className="rounded border-zinc-300 text-blue-600 focus:ring-blue-500 h-4 w-4"
                />
                <label htmlFor="autoCreateMeet" className="text-xs font-bold text-zinc-700 cursor-pointer">
                  Automatically generate Google Meet link & email invites
                </label>
              </div>
            )}

            {/* Meet link */}
            {!(isCalendarConnected && teachOption === 'MYSELF' && autoCreateMeet) && (
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Meeting Link</label>
                <Input
                  type="url"
                  placeholder="https://meet.google.com/..."
                  value={schedLink}
                  onChange={(e) => setSchedLink(e.target.value)}
                  required={!(isCalendarConnected && teachOption === 'MYSELF' && autoCreateMeet)}
                />
              </div>
            )}

            {/* Notes */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Session Agenda / Notes</label>
              <Textarea
                placeholder="Mention what will be covered during this meeting..."
                rows={2}
                value={schedNotes}
                onChange={(e) => setSchedNotes(e.target.value)}
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setScheduleOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="font-bold" loading={scheduleLoading}>
                Schedule Session
              </Button>
            </DialogFooter>

          </form>
        </DialogContent>
      </Dialog>

      {/* LEAVE REVIEW DIALOG */}
      <Dialog open={reviewOpen} onOpenChange={setReviewOpen}>
        <DialogContent onOpenChange={setReviewOpen}>
          <DialogHeader>
            <DialogTitle>Leave Session Review</DialogTitle>
            <DialogDescription>
              Submit feedback for your partner. Self-reviews are strictly blocked.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmitReview} className="space-y-4 py-2">
            
            {/* Rating Stars */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider block">Rating (1-5 Stars)</label>
              <Select value={reviewRating} onChange={(e) => setReviewRating(Number(e.target.value))}>
                <option value={5}>★★★★★ (5 Stars - Excellent)</option>
                <option value={4}>★★★★☆ (4 Stars - Good)</option>
                <option value={3}>★★★☆☆ (3 Stars - Average)</option>
                <option value={2}>★★☆☆☆ (2 Stars - Poor)</option>
                <option value={1}>★☆☆☆☆ (1 Star - Bad)</option>
              </Select>
            </div>

            {/* Comment */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-600 uppercase tracking-wider">Comment Note</label>
              <Textarea
                placeholder="Write your feedback details here..."
                rows={4}
                value={reviewComment}
                onChange={(e) => setReviewComment(e.target.value)}
                required
              />
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setReviewOpen(false)}>
                Skip Review
              </Button>
              <Button type="submit" className="font-bold bg-amber-500 hover:bg-amber-600 text-white" loading={reviewLoading}>
                Submit Review
              </Button>
            </DialogFooter>

          </form>
        </DialogContent>
      </Dialog>

    </div>
  );
};

export default SessionsPage;
