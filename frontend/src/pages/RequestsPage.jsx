import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { GitPullRequest, ArrowUpRight, ArrowDownLeft, Check, X, FileText, Info, Calendar, MessageSquare } from 'lucide-react';
import { requestsService } from '../services/requests.service';
import { useAuth } from '../hooks/useAuth';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/Tabs';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '../components/ui/Dialog';
import { Badge } from '../components/ui/Badge';
import { Skeleton } from '../components/ui/Skeleton';

export const RequestsPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('PENDING');

  // Details dialog states
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);

  const fetchRequests = async () => {
    try {
      const response = await requestsService.getAllRequests(0, 100);
      if (response && response.success) {
        setRequests(response.data.content || []);
      }
    } catch (error) {
      console.error("Failed to load requests:", error);
      toast.error("Failed to load exchange requests");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleAccept = async (id, partnerName) => {
    try {
      const response = await requestsService.acceptRequest(id);
      if (response && response.success) {
        toast.success(`Exchange request with ${partnerName} accepted!`);
        await fetchRequests();
      }
    } catch (error) {
      toast.error(error.message || "Failed to accept request");
    }
  };

  const handleReject = async (id, partnerName) => {
    try {
      const response = await requestsService.rejectRequest(id);
      if (response && response.success) {
        toast.success(`Exchange request with ${partnerName} rejected`);
        await fetchRequests();
      }
    } catch (error) {
      toast.error(error.message || "Failed to reject request");
    }
  };

  const handleOpenDetails = (req) => {
    setSelectedRequest(req);
    setDetailsOpen(true);
  };

  const filteredRequests = requests.filter(r => r.status === activeTab);

  if (loading) {
    return (
      <div className="space-y-6">
        <div>
          <Skeleton className="h-8 w-48" />
          <Skeleton className="h-4 w-64 mt-2" />
        </div>
        <Skeleton className="h-10 w-64" />
        <div className="space-y-4">
          <Skeleton className="h-28 w-full" />
          <Skeleton className="h-28 w-full" />
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Exchange Requests</h1>
        <p className="text-sm text-zinc-500">Manage handshakes and invitations for reciprocal skill tutoring.</p>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full max-w-md grid-cols-3">
          <TabsTrigger value="PENDING">Pending</TabsTrigger>
          <TabsTrigger value="ACCEPTED">Accepted</TabsTrigger>
          <TabsTrigger value="REJECTED">Rejected</TabsTrigger>
        </TabsList>

        <TabsContent value={activeTab} className="mt-6">
          {filteredRequests.length > 0 ? (
            <div className="space-y-4">
              {filteredRequests.map((r) => {
                const isSentByMe = r.sender.id === user.id;
                const partner = isSentByMe ? r.receiver : r.sender;

                return (
                  <Card key={r.id} className="hover:shadow-sm transition-shadow">
                    <CardContent className="p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                      
                      {/* Left: Direction & User details */}
                      <div className="flex items-start space-x-3 min-w-0">
                        <div className={`p-2 rounded-lg shrink-0 mt-0.5 ${isSentByMe ? 'bg-amber-50 text-amber-600' : 'bg-blue-50 text-blue-600'}`}>
                          {isSentByMe ? (
                            <ArrowUpRight className="h-5 w-5" />
                          ) : (
                            <ArrowDownLeft className="h-5 w-5" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center space-x-2 flex-wrap">
                            <span className="font-bold text-zinc-900 text-sm">{partner.fullName}</span>
                            <span className="text-xs text-zinc-400">@{partner.username}</span>
                            <Badge variant="outline" className="text-xxs">
                              {isSentByMe ? 'Sent Request' : 'Received Request'}
                            </Badge>
                          </div>
                          
                          <p className="text-xs text-zinc-500 mt-1">
                            Sent on {new Date(r.createdAt).toLocaleDateString(undefined, {
                              month: 'short',
                              day: 'numeric',
                              year: 'numeric'
                            })}
                          </p>

                          {/* Message snippet */}
                          <p className="text-sm text-zinc-600 mt-2 line-clamp-1 italic max-w-xl">
                            "{r.message}"
                          </p>
                        </div>
                      </div>

                      {/* Right: Actions */}
                      <div className="flex items-center space-x-2 shrink-0 self-end sm:self-center">
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => handleOpenDetails(r)}
                          className="text-xs font-semibold flex items-center space-x-1"
                        >
                          <FileText className="h-3.5 w-3.5" />
                          <span>Details</span>
                        </Button>

                        {/* If received and pending, show Accept/Reject triggers */}
                        {!isSentByMe && r.status === 'PENDING' && (
                          <>
                            <Button 
                              size="sm"
                              onClick={() => handleAccept(r.id, partner.fullName)}
                              className="text-xs font-bold bg-green-600 text-white hover:bg-green-700 flex items-center space-x-0.5"
                            >
                              <Check className="h-3.5 w-3.5" />
                              <span>Accept</span>
                            </Button>
                            
                            <Button 
                              size="sm"
                              variant="destructive"
                              onClick={() => handleReject(r.id, partner.fullName)}
                              className="text-xs font-bold flex items-center space-x-0.5"
                            >
                              <X className="h-3.5 w-3.5" />
                              <span>Reject</span>
                            </Button>
                          </>
                        )}
                      </div>

                    </CardContent>
                  </Card>
                );
              })}
            </div>
          ) : (
            <div className="text-center py-20 border border-dashed rounded-xl bg-white text-zinc-400 flex flex-col items-center space-y-2">
              <GitPullRequest className="h-8 w-8 text-zinc-300" />
              <p className="text-sm font-semibold">No {activeTab.toLowerCase()} requests.</p>
              <p className="text-xs text-zinc-500">Matches can be explored in the Mutual Matches page.</p>
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* VIEW DETAILS DIALOG */}
      <Dialog open={detailsOpen} onOpenChange={setDetailsOpen}>
        <DialogContent onOpenChange={setDetailsOpen}>
          {selectedRequest && (
            <>
              <DialogHeader>
                <DialogTitle>Request Details</DialogTitle>
                <DialogDescription>
                  Review the full message note and details.
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-4 py-2">
                <div className="grid grid-cols-2 gap-4 border-b pb-4 text-sm">
                  <div>
                    <span className="text-xs font-semibold text-zinc-400 uppercase tracking-wider block">Sender</span>
                    <span className="font-semibold text-zinc-900">{selectedRequest.sender.fullName}</span>
                    <span className="text-xs text-zinc-500 block">@{selectedRequest.sender.username}</span>
                  </div>
                  <div>
                    <span className="text-xs font-semibold text-zinc-400 uppercase tracking-wider block">Receiver</span>
                    <span className="font-semibold text-zinc-900">{selectedRequest.receiver.fullName}</span>
                    <span className="text-xs text-zinc-500 block">@{selectedRequest.receiver.username}</span>
                  </div>
                </div>

                {(() => {
                  const msg = selectedRequest.message || '';
                  const availIndex = msg.indexOf('\n\n[Availability: ');
                  let parsedMessage = msg;
                  let parsedAvailability = '';
                  
                  if (availIndex !== -1) {
                    parsedMessage = msg.substring(0, availIndex);
                    parsedAvailability = msg.substring(availIndex + 18, msg.length - 1);
                  }

                  return (
                    <>
                      <div className="space-y-1.5">
                        <span className="text-xs font-semibold text-zinc-400 uppercase tracking-wider block">Request Message</span>
                        <div className="p-3 bg-zinc-50 border rounded-lg text-sm text-zinc-700 italic leading-relaxed">
                          "{parsedMessage}"
                        </div>
                      </div>

                      {parsedAvailability && (
                        <div className="space-y-1.5">
                          <span className="text-xs font-semibold text-zinc-800 uppercase tracking-wider block">📅 Proposing Teacher Availability</span>
                          <div className="p-3 bg-amber-50/50 border border-amber-200 text-amber-950 font-medium rounded-lg text-sm leading-relaxed">
                            {parsedAvailability}
                          </div>
                        </div>
                      )}
                    </>
                  );
                })()}

                <div className="flex items-center space-x-2 text-xxs text-zinc-500 bg-zinc-50 border p-3 rounded-lg">
                  <Info className="h-4 w-4 text-zinc-400 shrink-0" />
                  <span>
                    {selectedRequest.status === 'ACCEPTED' ? (
                      <span className="text-green-700 font-medium">Accepted request. Real-time chat is unlocked. You can now schedule meetings.</span>
                    ) : selectedRequest.status === 'REJECTED' ? (
                      <span className="text-red-700 font-medium">Rejected request. Exchange is inactive.</span>
                    ) : (
                      <span>Pending request. Awaiting recipient handshake action.</span>
                    )}
                  </span>
                </div>

                {selectedRequest.status === 'ACCEPTED' && (
                  <Button 
                    onClick={() => {
                      setDetailsOpen(false);
                      const partner = selectedRequest.sender.id === user.id ? selectedRequest.receiver : selectedRequest.sender;
                      navigate(`/chat/${partner.id}`);
                    }}
                    className="w-full font-bold flex items-center justify-center space-x-1.5 text-xs bg-zinc-950 text-white hover:bg-zinc-800"
                  >
                    <MessageSquare className="h-4 w-4" />
                    <span>Start Live Chat Now</span>
                  </Button>
                )}
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setDetailsOpen(false)}>
                  Close
                </Button>

                {/* If received and pending, add Accept/Reject actions in dialog too */}
                {selectedRequest.receiver.id === user.id && selectedRequest.status === 'PENDING' && (
                  <>
                    <Button 
                      variant="destructive"
                      onClick={() => {
                        handleReject(selectedRequest.id, selectedRequest.sender.fullName);
                        setDetailsOpen(false);
                      }}
                      className="font-bold flex items-center space-x-0.5"
                    >
                      <X className="h-3 w-3" />
                      <span>Reject</span>
                    </Button>
                    <Button 
                      onClick={() => {
                        handleAccept(selectedRequest.id, selectedRequest.sender.fullName);
                        setDetailsOpen(false);
                      }}
                      className="font-bold bg-green-600 hover:bg-green-700 text-white flex items-center space-x-0.5"
                    >
                      <Check className="h-3 w-3" />
                      <span>Accept</span>
                    </Button>
                  </>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

    </div>
  );
};

export default RequestsPage;
