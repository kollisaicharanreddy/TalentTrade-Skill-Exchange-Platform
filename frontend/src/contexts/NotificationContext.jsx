import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';
import { notificationsService } from '../services/notifications.service';

export const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const { isAuthenticated } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchNotifications = async () => {
    if (!isAuthenticated) return;
    try {
      const response = await notificationsService.getNotifications(0, 50);
      if (response && response.success) {
        const list = response.data.content || [];
        setNotifications(list);
        const unreads = list.filter(n => !(n.isRead !== undefined ? n.isRead : n.read)).length;
        setUnreadCount(unreads);
      }
    } catch (error) {
      console.error("Failed to load notifications:", error);
    }
  };

  // Poll notifications every 10 seconds when user is authenticated
  useEffect(() => {
    if (isAuthenticated) {
      fetchNotifications();
      const interval = setInterval(fetchNotifications, 10000);
      return () => clearInterval(interval);
    } else {
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [isAuthenticated]);

  const markAsRead = async (id) => {
    try {
      const response = await notificationsService.markAsRead(id);
      if (response && response.success) {
        setNotifications(prev =>
          prev.map(n => (n.id === id ? { ...n, isRead: true, read: true } : n))
        );
        setUnreadCount(prev => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const response = await notificationsService.markAllAsRead();
      if (response && response.success) {
        setNotifications(prev => prev.map(n => ({ ...n, isRead: true, read: true })));
        setUnreadCount(0);
      }
    } catch (error) {
      console.error("Failed to mark all notifications as read:", error);
    }
  };

  const deleteNotification = async (id) => {
    try {
      const response = await notificationsService.deleteNotification(id);
      if (response && response.success) {
        setNotifications(prev => {
          const filtered = prev.filter(n => n.id !== id);
          const wasUnread = prev.find(n => n.id === id && !(n.isRead !== undefined ? n.isRead : n.read));
          if (wasUnread) {
            setUnreadCount(count => Math.max(0, count - 1));
          }
          return filtered;
        });
      }
    } catch (error) {
      console.error("Failed to delete notification:", error);
    }
  };

  const value = {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};
