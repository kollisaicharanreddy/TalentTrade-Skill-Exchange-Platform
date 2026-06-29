import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Check, Trash2, Calendar, CheckSquare } from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';
import { Card, CardContent } from '../components/ui/Card';
import { Button } from '../components/ui/Button';

export const NotificationsPage = () => {
  const navigate = useNavigate();
  const { 
    notifications, 
    unreadCount, 
    markAsRead, 
    markAllAsRead, 
    deleteNotification 
  } = useNotifications();

  const handleNotificationClick = (n, isRead) => {
    if (!isRead) {
      markAsRead(n.id);
    }
    
    // Direct redirect to appropriate dashboard pages
    switch (n.type) {
      case 'REQUEST_RECEIVED':
      case 'REQUEST_REJECTED':
        navigate('/requests');
        break;
      case 'REQUEST_ACCEPTED':
        navigate('/chat');
        break;
      case 'SESSION_CREATED':
      case 'SESSION_CANCELLED':
        navigate('/sessions');
        break;
      case 'REVIEW_RECEIVED':
        navigate('/reviews');
        break;
      default:
        break;
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-zinc-900 tracking-tight">Notifications</h1>
          <p className="text-sm text-zinc-500">Stay up to date with exchange requests, schedules, and cancellations.</p>
        </div>
        
        {unreadCount > 0 && (
          <Button 
            variant="outline"
            onClick={markAllAsRead}
            className="font-bold flex items-center space-x-1 sm:self-end text-xs"
          >
            <CheckSquare className="h-4 w-4" />
            <span>Mark all as read</span>
          </Button>
        )}
      </div>

      {/* Inbox List */}
      <div className="space-y-3">
        {notifications.length > 0 ? (
          notifications.map((n) => {
            const isRead = n.isRead !== undefined ? n.isRead : n.read;

            return (
              <Card 
                key={n.id} 
                className={`transition-colors border-l-4 ${isRead ? 'border-l-zinc-200' : 'border-l-zinc-800 bg-zinc-900/[0.01]'}`}
              >
                <CardContent className="p-4 flex items-start justify-between gap-4">
                  
                  {/* Details (clickable to redirect) */}
                  <div 
                    onClick={() => handleNotificationClick(n, isRead)}
                    className="space-y-1.5 min-w-0 flex-1 cursor-pointer hover:opacity-80 transition-opacity"
                  >
                    <div className="flex items-center space-x-2">
                      <span className={`text-sm font-bold text-zinc-900 ${isRead ? '' : 'font-extrabold'}`}>
                        {n.title}
                      </span>
                      {!isRead && (
                        <span className="inline-block h-2 w-2 rounded-full bg-zinc-900 shrink-0" />
                      )}
                    </div>
                    
                    <p className="text-sm text-zinc-650 leading-relaxed max-w-2xl">
                      {n.message}
                    </p>

                    <div className="flex items-center space-x-1 text-xxs text-zinc-400">
                      <Calendar className="h-3.5 w-3.5" />
                      <span>
                        {new Date(n.createdAt).toLocaleDateString(undefined, {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit'
                        })}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center space-x-1 shrink-0 mt-0.5">
                    {!isRead && (
                      <button
                        onClick={() => markAsRead(n.id)}
                        title="Mark as read"
                        className="text-zinc-400 hover:text-green-600 transition-colors p-1.5 rounded-md hover:bg-green-50"
                      >
                        <Check className="h-4 w-4" />
                      </button>
                    )}
                    <button
                      onClick={() => deleteNotification(n.id)}
                      title="Delete notification"
                      className="text-zinc-400 hover:text-red-500 transition-colors p-1.5 rounded-md hover:bg-red-50"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>

                </CardContent>
              </Card>
            );
          })
        ) : (
          <div className="text-center py-20 border border-dashed rounded-xl bg-white text-zinc-400 flex flex-col items-center space-y-2">
            <Bell className="h-8 w-8 text-zinc-300 animate-pulse" />
            <p className="text-sm font-semibold">Your notification inbox is empty.</p>
            <p className="text-xs text-zinc-500">System announcements and handshakes appear here.</p>
          </div>
        )}
      </div>

    </div>
  );
};

export default NotificationsPage;
