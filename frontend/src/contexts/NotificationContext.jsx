import React, { createContext, useContext, useState, useCallback, useMemo } from "react";
import {apiPath} from "../config_variable.js";

const NotificationContext = createContext(null);

export function NotificationProvider({ children }) {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(false);

    const refreshNotifications = useCallback(async () => {
        setLoading(true);
        try {
            const res = await fetch(`${apiPath}/api/notifications/due`, { credentials: "include" });
            const data = await res.json();
            setNotifications(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error("refreshNotifications error", e);
        } finally {
            setLoading(false);
        }
    }, []);

    // กดกากบาท = skip รอบ due นี้ (key = scheduleId + nextDueDate)
    const skipNotification = useCallback(async (n) => {
        await fetch(`${apiPath}/api/notifications/schedule/${n.scheduleId}/due/${n.nextDueDate}/skip`, {
            method: "DELETE",
            credentials: "include",
        });
        await refreshNotifications();
    }, [refreshNotifications]);

    const unreadCount = useMemo(() => notifications.length, [notifications]);

    // API ที่ยังใช้อยู่ใน Bell แต่จะ no-op หรือไม่ใช้แล้ว
    const markAsRead = useCallback(() => {}, []);
    const markAllAsRead = useCallback(() => {}, []);
    const deleteNotification = skipNotification;

    return (
        <NotificationContext.Provider
            value={{
                notifications,
                loading,
                unreadCount,
                refreshNotifications,
                markAsRead,
                markAllAsRead,
                deleteNotification,
            }}
        >
            {children}
        </NotificationContext.Provider>
    );
}

export const useNotifications = () => useContext(NotificationContext);
