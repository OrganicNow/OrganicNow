import React, { createContext, useContext, useState, useEffect } from 'react';
import {apiPath} from "../config_variable.js";

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true); // เพิ่ม loading state

  // ตรวจสอบ authentication status เมื่อแอปเริ่มต้น
  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        const savedUser = sessionStorage.getItem('user');
        if (savedUser) {
          const userData = JSON.parse(savedUser);
          
            // ตรวจสอบ session กับ server
            try {
              const sessionToken = sessionStorage.getItem('sessionToken');
              const headers = {
                'Content-Type': 'application/json',
              };
              
              // ส่ง session token ใน header ถ้ามี
              if (sessionToken) {
                headers['Authorization'] = `Bearer ${sessionToken}`;
              }
              
              const response = await fetch(`${apiPath}/auth/check`, {
                credentials: 'include',
                headers,
              });            if (response.ok) {
              const data = await response.json();
              if (data.success) {
                setUser(userData);
                setIsAuthenticated(true);
              } else {
                // Session หมดอายุ
                sessionStorage.removeItem('user');
                sessionStorage.removeItem('sessionToken');
                setUser(null);
                setIsAuthenticated(false);
              }
            } else {
              // Server error หรือ session หมดอายุ
              sessionStorage.removeItem('user');
              sessionStorage.removeItem('sessionToken');
              setUser(null);
              setIsAuthenticated(false);
            }
          } catch (serverError) {
            console.error('Server check failed, using local storage data:', serverError);
            // ถ้า server ไม่ตอบสนอง ให้ใช้ข้อมูล localStorage
            setUser(userData);
            setIsAuthenticated(true);
          }
        }
      } catch (error) {
        console.error('Error checking auth status:', error);
        sessionStorage.removeItem('user');
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  const login = async (username, password) => {
    try {
      const response = await fetch(`${apiPath}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // ✅ เพิ่มบรรทัดนี้เพื่อส่ง cookies
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();

      if (data.success) {
        setUser(data.data.admin);
        setIsAuthenticated(true);
        sessionStorage.setItem('user', JSON.stringify(data.data.admin));
        // เก็บ session token ด้วย
        if (data.data.token) {
          sessionStorage.setItem('sessionToken', data.data.token);
        }
        return { success: true };
      } else {
        return { success: false, message: data.message };
      }
    } catch (error) {
      return { success: false, message: 'เกิดข้อผิดพลาดในการเข้าสู่ระบบ' };
    }
  };

  const logout = async (navigateTo = '/dashboard') => {
    try {
      // เรียก logout API
      await fetch(`${apiPath}/auth/logout`, {
        method: 'POST',
        credentials: 'include',
      });
    } catch (error) {
      console.error('Logout API error:', error);
    } finally {
      // ล้างข้อมูล local state
      setUser(null);
      setIsAuthenticated(false);
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('sessionToken');
      
      // Navigate ไปยังหน้าที่กำหนด
      if (navigateTo && window.location.pathname !== navigateTo) {
        window.location.href = navigateTo;
      }
    }
  };

  const hasPermission = (permission) => {
    if (!user) return false;
    
    switch (permission) {
      case 'admin':
        return user.adminRole === 0 || user.adminRole === 1;
      case 'super_admin':
        return user.adminRole === 1;
      case 'manage_packages':
        return user.adminRole === 1;
      default:
        return false;
    }
  };

  const value = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    hasPermission,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
