// src/contexts/AuthContext.jsx
import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

// ใช้ BASE URL จาก Vite env (prod = api.localtest.me, dev = localhost)
const API_BASE_URL =
    import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
    const [isLoading, setIsLoading] = useState(true);

    // ตรวจสอบ authentication status เมื่อแอปเริ่มต้น
    useEffect(() => {
        const checkAuthStatus = async () => {
            try {
                const savedUser = localStorage.getItem('user');

                if (savedUser) {
                    const userData = JSON.parse(savedUser);

                    try {
                        const sessionToken = localStorage.getItem('sessionToken');
                        const headers = {
                            'Content-Type': 'application/json',
                        };

                        if (sessionToken) {
                            headers['Authorization'] = `Bearer ${sessionToken}`;
                        }

                        const response = await fetch(`${API_BASE_URL}/api/auth/check`, {
                            credentials: 'include',
                            headers,
                        });

                        if (response.ok) {
                            const data = await response.json();
                            if (data.success) {
                                setUser(userData);
                                setIsAuthenticated(true);
                            } else {
                                // Session หมดอายุ
                                localStorage.removeItem('user');
                                localStorage.removeItem('sessionToken');
                                setUser(null);
                                setIsAuthenticated(false);
                            }
                        } else {
                            // Server error หรือ session หมดอายุ
                            localStorage.removeItem('user');
                            localStorage.removeItem('sessionToken');
                            setUser(null);
                            setIsAuthenticated(false);
                        }
                    } catch (serverError) {
                        console.error('Server check failed, using local storage data:', serverError);
                        // ถ้า server ไม่ตอบ ให้ใช้ข้อมูล localStorage ชั่วคราว
                        setUser(userData);
                        setIsAuthenticated(true);
                    }
                }
            } catch (error) {
                console.error('Error checking auth status:', error);
                localStorage.removeItem('user');
            } finally {
                setIsLoading(false);
            }
        };

        checkAuthStatus();
    }, []);

    const login = async (username, password) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include', // ส่ง/รับ cookie session
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();

            if (data.success) {
                setUser(data.data.admin);
                setIsAuthenticated(true);
                localStorage.setItem('user', JSON.stringify(data.data.admin));

                if (data.data.token) {
                    localStorage.setItem('sessionToken', data.data.token);
                }

                return { success: true };
            } else {
                return { success: false, message: data.message };
            }
        } catch (error) {
            return { success: false, message: 'เกิดข้อผิดพลาดในการเข้าสู่ระบบ' };
        }
    };

    const logout = async () => {
        try {
            await fetch(`${API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                credentials: 'include',
            });
        } catch (error) {
            console.error('Logout API error:', error);
        } finally {
            setUser(null);
            setIsAuthenticated(false);
            localStorage.removeItem('user');
            localStorage.removeItem('sessionToken');
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
