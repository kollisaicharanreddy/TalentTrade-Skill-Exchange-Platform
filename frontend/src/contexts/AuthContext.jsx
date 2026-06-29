import React, { createContext, useState, useEffect } from 'react';
import { authService } from '../services/auth.service';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // Validate or fetch user details on mount if token exists
  useEffect(() => {
    const initializeAuth = async () => {
      if (token) {
        try {
          const response = await authService.getCurrentUser();
          if (response && response.success) {
            setUser(response.data);
            localStorage.setItem('user', JSON.stringify(response.data));
          } else {
            logout();
          }
        } catch (error) {
          console.error("Token verification failed:", error);
          logout();
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, [token]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await authService.login(email, password);
      if (response && response.success) {
        const { token: jwtToken, user: userData } = response.data;
        localStorage.setItem('token', jwtToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setToken(jwtToken);
        setUser(userData);
        return { success: true };
      }
      return { success: false, message: response.message || 'Login failed' };
    } catch (error) {
      console.error("Login failed:", error);
      return { success: false, message: error.message || 'Invalid credentials' };
    } finally {
      setLoading(false);
    }
  };

  const register = async (fullName, username, email, password) => {
    setLoading(true);
    try {
      const response = await authService.register(fullName, username, email, password);
      if (response && response.success) {
        // Automatically log in the user upon registration?
        // Wait, AuthController /register returns the registered user profile but NOT a JWT token directly.
        // So they will need to log in after registering. That is standard backend behavior!
        return { success: true, message: response.message || 'Registration successful. Please log in.' };
      }
      return { success: false, message: response.message || 'Registration failed' };
    } catch (error) {
      console.error("Registration failed:", error);
      return { success: false, message: error.message || 'Registration failed' };
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await authService.updateProfile(profileData);
      if (response && response.success) {
        setUser(response.data);
        localStorage.setItem('user', JSON.stringify(response.data));
        return { success: true };
      }
      return { success: false, message: response.message || 'Update failed' };
    } catch (error) {
      console.error("Profile update failed:", error);
      return { success: false, message: error.message || 'Update failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    isAuthenticated: !!token,
    loading,
    login,
    register,
    updateProfile,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
