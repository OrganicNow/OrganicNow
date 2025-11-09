import React, { createContext, useContext, useState, useEffect } from 'react';

const NotificationContext = createContext();

export const useNotifications = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotifications must be used within a NotificationProvider');
    }
    return context;
};

const API_BASE = import.meta.env?.VITE_API_URL ?? "http://localhost:8080";

export const NotificationProvider = ({ children }) => {
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [previousCount, setPreviousCount] = useState(0);
    const [lastToastTime, setLastToastTime] = useState(0); // เก็บเวลาแสดง toast ครั้งสุดท้าย

    // โหลดจำนวน unread notifications
    const loadUnreadCount = async () => {
        try {
            console.log('🔔 Loading unread count from:', `${API_BASE}/notifications/count/unread`);
            const response = await fetch(`${API_BASE}/notifications/count/unread`, {
                credentials: 'include'
            });
            console.log('🔔 Unread count response:', response.status, response.ok);
            if (response.ok) {
                const data = await response.json();
                console.log('🔔 Unread count data:', data);
                const newCount = data.result || 0;
                
                // ถ้ามี notification ใหม่ (count เพิ่มขึ้น) ให้แสดง toast ครั้งเดียว
                if (newCount > previousCount && previousCount >= 0) {
                    console.log('🎯 New notification detected! Checking if should show toast...');
                    console.log(`Previous count: ${previousCount}, New count: ${newCount}`);
                    
                    // ตรวจสอบว่าผ่านมา 1 นาทีแล้วหรือไม่นับจากการแสดง toast ครั้งสุดท้าย
                    const now = Date.now();
                    const oneMinuteAgo = now - 60000;
                    
                    if (lastToastTime === 0 || lastToastTime < oneMinuteAgo) {
                        console.log('🎯 OK to show toast - enough time passed');
                        setLastToastTime(now);
                        setTimeout(() => {
                            loadLatestNotificationForToast();
                        }, 500);
                    } else {
                        console.log('🎯 Skip toast - too soon since last toast');
                    }
                }
                
                setPreviousCount(newCount);
                setUnreadCount(newCount);
            } else {
                console.error('🔔 Failed to load unread count:', response.status);
            }
        } catch (error) {
            console.error('🔔 Error loading unread count:', error);
        }
    };

    // โหลด notification ล่าสุดเพื่อแสดง toast ครั้งเดียว (เฉพาะที่สร้างใหม่จริงๆ)
    const loadLatestNotificationForToast = async () => {
        try {
            console.log('🎯 Loading latest notifications for toast (only new ones)...');
            const response = await fetch(`${API_BASE}/notifications`, {
                credentials: 'include'
            });
            if (response.ok) {
                const data = await response.json();
                const latestNotifications = data.result || [];
                console.log('🎯 Latest notifications loaded:', latestNotifications.length);
                
                // แสดง toast เฉพาะ notification ที่สร้างใหม่มาก (ภายใน 30 วินาที)
                const thirtySecondsAgo = new Date(Date.now() - 30000);
                const brandNewNotifications = latestNotifications.filter(n => 
                    !n.isRead && 
                    new Date(n.createdAt) > thirtySecondsAgo &&
                    n.type === 'MAINTENANCE_DUE'
                );
                
                console.log('🎯 Brand new notifications (30 sec):', brandNewNotifications.length);
                
                // แสดง toast เฉพาะ notification ที่สร้างใหม่จริงๆ (1 อันเท่านั้น)
                if (brandNewNotifications.length > 0) {
                    const newestNotification = brandNewNotifications[0];
                    console.log('🎯 New notification found (toast disabled):', newestNotification.title);
                    // showGeneralNotification(newestNotification); // ปิดการแสดง toast
                } else {
                    console.log('🎯 No brand new notifications');
                }
            } else {
                console.error('🎯 Failed to load notifications for toast:', response.status);
            }
        } catch (error) {
            console.error('📄 Error loading latest notification for toast:', error);
        }
    };

    // โหลด notifications ทั้งหมด
    const loadNotifications = async () => {
        try {
            setLoading(true);
            console.log('📄 Loading notifications from:', `${API_BASE}/notifications`);
            const response = await fetch(`${API_BASE}/notifications`, {
                credentials: 'include'
            });
            console.log('📄 Notifications response:', response.status, response.ok);
            if (response.ok) {
                const data = await response.json();
                console.log('📄 Notifications data:', data);
                setNotifications(data.result || []);
            } else {
                console.error('📄 Failed to load notifications:', response.status);
            }
        } catch (error) {
            console.error('📄 Error loading notifications:', error);
        } finally {
            setLoading(false);
        }
    };

    // Refresh ข้อมูล notifications
    const refreshNotifications = async () => {
        console.log('🔄 Refreshing notifications (both count and list)...');
        try {
            await Promise.all([loadUnreadCount(), loadNotifications()]);
            console.log('✅ Notification refresh completed');
        } catch (error) {
            console.error('❌ Error refreshing notifications:', error);
        }
    };

    // เครื่องหมายว่าอ่านแล้ว
    const markAsRead = async (notificationId) => {
        try {
            const response = await fetch(`${API_BASE}/notifications/${notificationId}/read`, {
                method: 'PUT',
                credentials: 'include'
            });

            if (response.ok) {
                setNotifications(prev => prev.map(notif => 
                    notif.id === notificationId 
                        ? { ...notif, isRead: true, readAt: new Date().toISOString() }
                        : notif
                ));
                setUnreadCount(prev => Math.max(0, prev - 1));
                return true;
            }
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
        return false;
    };

    // เครื่องหมายทั้งหมดว่าอ่านแล้ว
    const markAllAsRead = async () => {
        try {
            const response = await fetch(`${API_BASE}/notifications/read-all`, {
                method: 'PUT',
                credentials: 'include'
            });

            if (response.ok) {
                setNotifications(prev => prev.map(notif => ({
                    ...notif,
                    isRead: true,
                    readAt: new Date().toISOString()
                })));
                setUnreadCount(0);
                return true;
            }
        } catch (error) {
            console.error('Error marking all notifications as read:', error);
        }
        return false;
    };

    // ลบ notification
    const deleteNotification = async (notificationId) => {
        try {
            const response = await fetch(`${API_BASE}/notifications/${notificationId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (response.ok) {
                const notificationToDelete = notifications.find(n => n.id === notificationId);
                setNotifications(prev => prev.filter(notif => notif.id !== notificationId));
                
                if (notificationToDelete && !notificationToDelete.isRead) {
                    setUnreadCount(prev => Math.max(0, prev - 1));
                }
                return true;
            }
        } catch (error) {
            console.error('Error deleting notification:', error);
        }
        return false;
    };

    useEffect(() => {
        loadUnreadCount();
        loadNotifications();
        
        // ตั้งค่า previousCount เป็น current count ในครั้งแรก
        setTimeout(() => {
            setPreviousCount(unreadCount);
        }, 1000);
        
        // Auto-refresh ทุก 2 นาที (ช้าลงเพื่อลดการเช็คบ่อย)
        const interval = setInterval(() => {
            console.log('🔄 Auto-refreshing notifications...');
            loadUnreadCount();
            loadNotifications();
        }, 120000); // 2 นาที
        
        // Page Visibility API - refresh เมื่อเปิดแท็บใหม่
        const handleVisibilityChange = () => {
            if (!document.hidden) {
                console.log('👀 Tab became visible - refreshing notifications');
                loadUnreadCount();
                loadNotifications();
            }
        };
        
        document.addEventListener('visibilitychange', handleVisibilityChange);
        
        return () => {
            console.log('🧹 Cleaning up notification auto-refresh interval');
            clearInterval(interval);
            document.removeEventListener('visibilitychange', handleVisibilityChange);
        };
    }, []);

    const value = {
        unreadCount,
        notifications,
        loading,
        refreshNotifications,
        markAsRead,
        markAllAsRead,
        deleteNotification,
        loadUnreadCount,
        loadNotifications
    };

    return (
        <NotificationContext.Provider value={value}>
            {children}
        </NotificationContext.Provider>
    );
};