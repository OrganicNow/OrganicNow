import React, { useState, useEffect, createContext, useContext } from 'react';
import ToastNotification from '../component/ToastNotification';

const ToastContext = createContext();

export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within a ToastProvider');
    }
    return context;
};

export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const showToast = (title, message, type = 'notification', duration = 8000) => {
        const id = Date.now() + Math.random();
        const toast = { id, title, message, type, duration };
        
        setToasts(prev => [...prev, toast]);

        // Auto remove after duration
        setTimeout(() => {
            removeToast(id);
        }, duration + 500); // Extra time for animation
    };

    const removeToast = (id) => {
        setToasts(prev => prev.filter(toast => toast.id !== id));
    };

    // Special methods for different types
    const showMaintenanceDue = (schedule, daysUntil) => {
        if (daysUntil === 0) {
            showToast(
                '🚨 Maintenance Due Today!',
                `"${schedule.scheduleTitle}" is due TODAY! Please complete as soon as possible.`,
                'urgent',
                10000
            );
        } else if (daysUntil === 1) {
            showToast(
                '⚠️ Maintenance Due Tomorrow',
                `"${schedule.scheduleTitle}" is due tomorrow. Please prepare for completion.`,
                'warning',
                8000
            );
        }
    };

    const showMaintenanceCreated = (schedule) => {
        showToast(
            'Maintenance Schedule Created',
            `New maintenance schedule "${schedule.scheduleTitle}" has been created successfully.`,
            'success',
            6000
        );
    };

    const showGeneralNotification = (notification) => {
        // แสดง toast เฉพาะเมื่อได้รับคำสั่งให้แสดง
        const typeMap = {
            'MAINTENANCE_DUE': 'due',
            'MAINTENANCE_SCHEDULE_CREATED': 'notification',
            'URGENT': 'urgent'
        };

        showToast(
            notification.title,
            notification.message,
            typeMap[notification.type] || 'notification',
            8000
        );
    };

    const value = {
        showToast,
        showMaintenanceDue,
        showMaintenanceCreated,
        showGeneralNotification,
        removeToast
    };

    return (
        <ToastContext.Provider value={value}>
            {children}
            <div className="toast-container">
                {toasts.map(toast => (
                    <ToastNotification
                        key={toast.id}
                        title={toast.title}
                        message={toast.message}
                        type={toast.type}
                        duration={toast.duration}
                        onClose={() => removeToast(toast.id)}
                    />
                ))}
            </div>
        </ToastContext.Provider>
    );
};