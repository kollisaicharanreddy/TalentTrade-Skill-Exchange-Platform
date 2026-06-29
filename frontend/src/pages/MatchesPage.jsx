import React, { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { Sparkles, RefreshCw, Send, MapPin, User, CheckCircle, Info } from 'lucide-react';
import { matchesService } from '../services/matches.service';
import { requestsService } from '../services/requests.service';
import { useAuth } from '../hooks/useAuth';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Textarea } from '../components/ui/Textarea';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../components/ui/Dialog';
import { Skeleton } from '../components/ui/Skeleton';
import { Badge } from '../components/ui/Badge';

export const MatchesPage = () => {
  const { user } = useAuth();
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // Exchange Dialog state
  const [requestDialogOpen, setRequestDialogOpen] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState(null);
  const [customMessage, setCustomMessage] = useState('');
  const [availability, setAvailability] = useState('');
  const [requestSending, setRequestSending] = useState(false);

  const fetchMatches = async () => {
    try {
      const response = await matchesService.getMatches(0, 50);
      if (response && response.success) {
        setMatches(response.data.content || []);
      }
    } catch (error) {
      console.error("Failed to fetch matches:", error);
      toast.error("Failed to load matches list");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMatches();
  }, []);

  const handleRefreshMatches = async () => {
    setRefreshing(true);
    try {
      const response = await matchesService.refreshMatches();
      if (response && response.success) {
        toast.success("Reciprocal matches recalculated successfully!");
        await fetchMatches();
      }
    } catch (error) {
      toast.error(error.message || "Failed to recalculate matches");
    } finally {
      setRefreshing(false);
    }
  };

  const handleOpenRequest = (match, matchedUser) => {
    setSelectedMatch({ match, matchedUser });
    setCustomMessage(`Hi ${matchedUser.fullName || matchedUser.username}, I saw we have matching reciprocal skills on TalentTrade! I'd love to set up an exchange to trade tutoring sessions. Let me know if you'd be interested!`);
    setAvailability('');
    setRequestDialogOpen(true);
  };

  const handleSendRequest = async () => {
    if (!selectedMatch) return;
    setRequestSending(true);
    try {
      const finalMessage = availability.trim() 
        ? `${customMessage.trim()}\n\n[Availability: ${availability.trim()}]`
        : customMessage.trim();

      const response = await requestsService.createRequest(
        selectedMatch.matchedUser.id,
        finalMessage
      );
      if (response && response.success) {
        toast.success(`Exchange request sent to ${selectedMatch.matchedUser.fullName}!`);
        setRequestDialogOpen(false);
        setCustomMessage('');
        setAvailability('');
      }
    } catch (error) {
      toast.error(error.message || "Failed to send request. Check if a request is already pending.");
    } finally {
      setRequestSending(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-9 w-32" />
        </div>
        <div className="grid gap-6 md:grid-cols-2">
          <Skeleton className="h-44 w-full" />
          <Skeleton className="h-44 w-full" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Reciprocal Matches</h1>
          <p className="text-sm text-zinc-500">
            Peers calculated by the matching engine who teach what you want to learn, and learn what you teach.
          </p>
        </div>
        <Button 
          variant="outline"
          onClick={handleRefreshMatches}
          disabled={refreshing}
          className="font-bold flex items-center space-x-1 sm:self-end"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          <span>{refreshing ? 'Calculating...' : 'Recalculate Matches'}</span>
        </Button>
      </div>

      {/* Match Cards list */}
      {matches.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2">
          {matches.map((m) => {
            // Determine who the other matched user is
            const otherUser = m.user1.id === user.id ? m.user2 : m.user1;
            
            return (
              <Card key={m.id} className="flex flex-col hover:shadow-md transition-shadow">
                
                {/* Card Header with Score */}
                <CardHeader className="flex flex-row items-start justify-between pb-2 border-b">
                  <div className="space-y-1">
                    <CardTitle className="text-lg font-bold text-zinc-900 flex items-center space-x-2">
                      <User className="h-4 w-4 text-zinc-500" />
                      <span>{otherUser.fullName}</span>
                    </CardTitle>
                    <CardDescription>@{otherUser.username}</CardDescription>
                  </div>
                  
                  {/* Circular / Pill Match Score */}
                  <Badge variant="secondary" className="px-2.5 py-1 bg-green-50 text-green-700 border-green-150 text-xs font-black flex items-center space-x-1 shrink-0">
                    <Sparkles className="h-3 w-3" />
                    <span>{m.matchScore}% Match</span>
                  </Badge>
                </CardHeader>

                {/* Card Content with user profile */}
                <CardContent className="pt-4 flex-1 space-y-3">
                  {otherUser.location && (
                    <div className="flex items-center space-x-1 text-xs text-zinc-500">
                      <MapPin className="h-3.5 w-3.5" />
                      <span>{otherUser.location}</span>
                    </div>
                  )}

                  <p className="text-sm text-zinc-650 line-clamp-3 italic leading-relaxed">
                    "{otherUser.bio || 'This user has not written a bio description yet.'}"
                  </p>
                </CardContent>

                {/* Card Action */}
                <CardFooter className="pt-2 border-t flex justify-end">
                  <Button 
                    onClick={() => handleOpenRequest(m, otherUser)}
                    className="text-xs font-bold flex items-center space-x-1"
                  >
                    <Send className="h-3.5 w-3.5" />
                    <span>Request Exchange</span>
                  </Button>
                </CardFooter>

              </Card>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-20 border border-dashed rounded-xl text-zinc-400 bg-white flex flex-col items-center space-y-3">
          <Sparkles className="h-10 w-10 text-zinc-300 animate-pulse" />
          <h3 className="font-bold text-lg text-zinc-800">No reciprocal matches found.</h3>
          <p className="text-sm text-zinc-500 max-w-sm px-6">
            Matches are calculated based on reciprocal Teach vs Learn skills. Try listing more skills in your **Skills Portfolio** or click **Recalculate Matches** to re-evaluate matches.
          </p>
          <Button variant="outline" size="sm" onClick={handleRefreshMatches} disabled={refreshing}>
            Recalculate Matches Now
          </Button>
        </div>
      )}

      {/* EXCHANGE REQUEST DIALOG */}
      <Dialog open={requestDialogOpen} onOpenChange={setRequestDialogOpen}>
        <DialogContent onOpenChange={setRequestDialogOpen}>
          <DialogHeader>
            <DialogTitle>Request Skill Exchange</DialogTitle>
            <DialogDescription>
              Send an exchange request handshake to **{selectedMatch?.matchedUser?.fullName}**. If accepted, you will unlock direct chat and scheduled sessions.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-650 uppercase tracking-wider">Custom Message Note</label>
              <Textarea
                placeholder="Write a friendly introduction note..."
                rows={4}
                value={customMessage}
                onChange={(e) => setCustomMessage(e.target.value)}
                required
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-zinc-650 uppercase tracking-wider">My Teaching Availability</label>
              <Textarea
                placeholder="Specify dates/times you are available to teach (e.g., Weekdays after 5pm, Saturdays 10am-2pm)"
                rows={2}
                value={availability}
                onChange={(e) => setAvailability(e.target.value)}
                required
              />
              <p className="text-[10px] text-zinc-400 leading-tight">Propose dates early so the match partner can schedule a reciprocal session easily.</p>
            </div>

            <div className="flex items-center space-x-2 text-xxs text-zinc-500 bg-zinc-50 border p-3 rounded-lg">
              <Info className="h-4 w-4 text-zinc-400 shrink-0" />
              <span>Duplicate exchange requests to the same partner are automatically blocked.</span>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setRequestDialogOpen(false)}>
              Cancel
            </Button>
            <Button 
              onClick={handleSendRequest} 
              className="font-bold flex items-center space-x-1"
              loading={requestSending}
            >
              <Send className="h-3 w-3" />
              <span>Send Request</span>
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

    </div>
  );
};

export default MatchesPage;
