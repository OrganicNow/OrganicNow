// src/components/NotificationBell.jsx
import React, { useMemo, useRef, useEffect } from 'react';
import { Button } from 'primereact/button';
import { Badge } from 'primereact/badge';
import { OverlayPanel } from 'primereact/overlaypanel';
import { ScrollPanel } from 'primereact/scrollpanel';
import { Divider } from 'primereact/divider';
import { Tooltip } from 'primereact/tooltip';
import { useNotifications } from '../contexts/NotificationContext';
import '../assets/css/notification.css';
import { useNavigate } from 'react-router-dom';

const BADGE_MAX = 99;
const formatBadge = (n) => (n > BADGE_MAX ? `${BADGE_MAX}+` : `${n}`);

const NotificationBell = () => {
    const {
        notifications,
        unreadCount,
        loading,
        refreshNotifications,
        deleteNotification
    } = useNotifications();

    const op = useRef(null);
    const navigate = useNavigate();

    // ✅ เรียก refreshNotifications ทันทีตอนหน้าโหลดใหม่
    useEffect(() => {
        refreshNotifications();
    }, [refreshNotifications]);

    const formatTime = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins}m ago`;
        if (diffHours < 24) return `${diffHours}h ago`;
        if (diffDays < 7) return `${diffDays}d ago`;
        return date.toLocaleDateString();
    };

    const iconClass = 'pi pi-exclamation-triangle';
    const iconColor = '#ffc107';

    const ordered = useMemo(() => {
        return [...(notifications || [])].sort((a, b) => {
            const ta = new Date(a.notifyAt || a.nextDueDate).getTime();
            const tb = new Date(b.notifyAt || b.nextDueDate).getTime();
            return tb - ta; // ใหม่ → เก่า
        });
    }, [notifications]);

    const goToAction = (n) => {
        op.current?.hide();
        const dueSrc = n.nextDueDate || n.notifyAt;
        const due = dueSrc ? new Date(dueSrc).toISOString().slice(0, 10) : '';
        navigate(`/maintenanceschedule?scheduleId=${n.scheduleId}${due ? `&due=${due}` : ''}`);
    };

    const onSkip = async (n) => {
        await deleteNotification(n);
    };

    return (
        <>
            <Tooltip target=".notification-bell" content="Notifications" position="bottom" />

            <div className="p-overlay-badge bell-badge-wrapper">
                <Button
                    icon="pi pi-bell"
                    className="p-button-rounded p-button-text topbar-btn notification-bell"
                    onClick={(e) => op.current?.toggle(e)}
                    aria-label="Notifications"
                />
                {unreadCount > 0 && (
                    <Badge className="notif-badge" value={formatBadge(unreadCount)} severity="danger" />
                )}
            </div>

            <OverlayPanel
                ref={op}
                style={{
                    width: '460px',
                    maxHeight: '800px',
                    border: 'none',
                    boxShadow: '0 8px 28px rgba(0,0,0,.15)',
                    borderRadius: '12px'
                }}
                className="notification-panel"
                pt={{
                    content: { style: { padding: 0, border: 'none' } },
                    root: { style: { border: 'none' } }
                }}
            >
                <div className="notification-header">
                    <div className="notification-header__left">
                        <h4 className="notification-title">Notifications</h4>
                        <span className="notification-subtitle">
              {unreadCount > 0 ? `${unreadCount} due` : 'All caught up 🎉'}
            </span>
                    </div>
                    {/* 🚫 ปุ่ม Refresh ถูกลบออก */}
                </div>

                <Divider className="notification-divider" />

                <ScrollPanel style={{ width: '100%', height: '380px' }}>
                    {loading ? (
                        <div className="notif-empty">
                            <i className="pi pi-spin" />
                            <p>Loading...</p>
                        </div>
                    ) : ordered.length === 0 ? (
                        <div className="notif-empty">
                            <i className="pi pi-inbox" />
                            <p>No notifications</p>
                        </div>
                    ) : (
                        <div className="notification-list">
                            {ordered.map((n) => (
                                <div
                                    key={`${n.scheduleId}-${n.nextDueDate}`}
                                    className="notification-item is-unread"
                                >
                                    <div className="notification-left">
                    <span className="notification-type-icon" style={{ color: iconColor }}>
                      <i className={iconClass} />
                    </span>
                                    </div>

                                    <div className="notification-center" onClick={() => goToAction(n)}>
                                        <div className="notification-row">
                                            <div className="notification-title-text">
                                                {n.title || 'Maintenance due soon'} <span className="unread-dot" />
                                            </div>
                                        </div>
                                        {n.message && <div className="notification-message">{n.message}</div>}
                                    </div>

                                    <div className="notification-right">
                                        <div className="notification-time">
                                            {formatTime(n.notifyAt || n.nextDueDate)}
                                        </div>
                                        <Button
                                            icon="pi pi-times"
                                            className="p-button-text p-button-rounded p-button-sm p-button-danger"
                                            onClick={() => onSkip(n)}
                                            tooltip="Skip this notification"
                                            tooltipOptions={{ position: 'top' }}
                                        />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </ScrollPanel>
            </OverlayPanel>
        </>
    );
};

export default NotificationBell;
