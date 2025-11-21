// src/component/test/NotificationBell.test.jsx
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";

let mockNotificationsState;
const mockNavigate = vi.fn();
const overlayHideMock = vi.fn();
const overlayToggleMock = vi.fn();

// 🧪 mock useNavigate จาก react-router-dom
vi.mock("react-router-dom", () => ({
  __esModule: true,
  useNavigate: () => mockNavigate,
}));

// 🧪 mock useNotifications จาก NotificationContext
// ⚠️ path นี้อิงตามโครงของคุณ: src/contexts/NotificationContext
vi.mock("../../contexts/NotificationContext", () => ({
  __esModule: true,
  useNotifications: () => mockNotificationsState,
}));

// 🧪 mock component ของ primereact ที่ยุ่งกับ DOM/สไตล์เยอะ ๆ

vi.mock("primereact/tooltip", () => ({
  __esModule: true,
  Tooltip: () => null,
}));

vi.mock("primereact/overlaypanel", () => {
  const OverlayPanel = React.forwardRef((props, ref) => {
    React.useImperativeHandle(ref, () => ({
      toggle: overlayToggleMock,
      hide: overlayHideMock,
    }));
    return (
      <div data-testid="overlay-panel">
        {props.children}
      </div>
    );
  });

  return {
    __esModule: true,
    OverlayPanel,
  };
});

vi.mock("primereact/scrollpanel", () => ({
  __esModule: true,
  ScrollPanel: ({ children, ...rest }) => (
    <div data-testid="scroll-panel" {...rest}>
      {children}
    </div>
  ),
}));

vi.mock("primereact/divider", () => ({
  __esModule: true,
  Divider: (props) => (
    <div data-testid="divider">
      {props.children}
    </div>
  ),
}));

vi.mock("primereact/badge", () => ({
  __esModule: true,
  Badge: (props) => (
    <span data-testid="badge">{props.value}</span>
  ),
}));

vi.mock("primereact/button", () => ({
  __esModule: true,
  Button: (props) => (
    <button {...props}>
      {props.label || null}
    </button>
  ),
}));

// ✅ import component จริง หลังจาก mock ทั้งหมดแล้ว
import NotificationBell from "../NotificationBell";

describe("NotificationBell component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockNotificationsState = {
      notifications: [],
      unreadCount: 0,
      loading: false,
      refreshNotifications: vi.fn(),
      deleteNotification: vi.fn().mockResolvedValue(),
    };
  });

  it("ควรเรียก refreshNotifications ทันทีตอน mount", () => {
    render(<NotificationBell />);
    expect(mockNotificationsState.refreshNotifications).toHaveBeenCalledTimes(1);
  });

  it("เมื่อ unreadCount = 0 และไม่มี notifications ควรขึ้น All caught up และ No notifications และไม่แสดง badge", () => {
    mockNotificationsState = {
      ...mockNotificationsState,
      notifications: [],
      unreadCount: 0,
      loading: false,
    };

    render(<NotificationBell />);

    // มีข้อความ All caught up 🎉 อย่างน้อย 1 อัน (แม้จะ render ซ้ำจาก Strict-like behavior)
    const allCaught = screen.getAllByText("All caught up 🎉");
    expect(allCaught.length).toBeGreaterThanOrEqual(1);

    // ข้อความ No notifications ก็จะถูก render ซ้ำเหมือนกัน → ใช้ getAllByText
    const noNotifList = screen.getAllByText("No notifications");
    expect(noNotifList.length).toBeGreaterThanOrEqual(1);

    // ไม่ควรมี badge
    expect(screen.queryByTestId("badge")).toBeNull();
  });

  it("เมื่อมี unreadCount มากกว่า 0 ควรแสดง badge และจำนวนที่เกิน 99 ให้แสดงเป็น 99+", () => {
    mockNotificationsState = {
      ...mockNotificationsState,
      unreadCount: 120,
    };

    render(<NotificationBell />);

    const badge = screen.getByTestId("badge");
    expect(badge).not.toBeNull();
    expect(badge.textContent).toBe("99+"); // formatBadge
  });

  it("เมื่อ loading = true ควรแสดง Loading...", () => {
    mockNotificationsState = {
      ...mockNotificationsState,
      loading: true,
      notifications: [],
      unreadCount: 5,
    };

    render(<NotificationBell />);

    const loadingEl = screen.getByText("Loading...");
    expect(loadingEl).not.toBeNull();
  });

  it("ควรเรียง notifications ใหม่ → เก่า และกดที่ notification แล้ว navigate ไป path ถูกต้อง พร้อม hide overlay", () => {
    const notifications = [
      {
        scheduleId: 1,
        title: "Older task",
        nextDueDate: "2024-01-01T00:00:00.000Z",
        message: "Old message",
      },
      {
        scheduleId: 2,
        title: "Newer task",
        notifyAt: "2024-01-02T00:00:00.000Z",
        message: "New message",
      },
    ];

    mockNotificationsState = {
      ...mockNotificationsState,
      notifications,
      unreadCount: 2,
      loading: false,
    };

    const { container } = render(<NotificationBell />);

    const items = Array.from(container.querySelectorAll(".notification-item"));
    expect(items.length).toBeGreaterThanOrEqual(2);

    const firstTitle = items[0].querySelector(".notification-title-text")?.textContent;
    const secondTitle = items[1].querySelector(".notification-title-text")?.textContent;

    // ใหม่ควรอยู่บน
    expect(firstTitle).toContain("Newer task");
    expect(secondTitle).toContain("Older task");

    const firstCenter = items[0].querySelector(".notification-center");
    expect(firstCenter).not.toBeNull();

    fireEvent.click(firstCenter);

    expect(mockNavigate).toHaveBeenCalledTimes(1);
    expect(mockNavigate).toHaveBeenCalledWith(
      "/maintenanceschedule?scheduleId=2&due=2024-01-02"
    );
    expect(overlayHideMock).toHaveBeenCalledTimes(1);
  });

  it("กดปุ่ม skip แล้วควรเรียก deleteNotification ด้วย notification นั้น", async () => {
    const notifications = [
      {
        scheduleId: 99,
        title: "Task to skip",
        nextDueDate: "2024-02-10T10:00:00.000Z",
        message: "Skip me",
      },
    ];

    const deleteNotificationMock = vi.fn().mockResolvedValue(undefined);

    mockNotificationsState = {
      ...mockNotificationsState,
      notifications,
      unreadCount: 1,
      loading: false,
      deleteNotification: deleteNotificationMock,
    };

    const { container } = render(<NotificationBell />);

    const item = container.querySelector(".notification-item");
    expect(item).not.toBeNull();

    const skipBtn = item.querySelector('button[icon="pi pi-times"]');
    expect(skipBtn).not.toBeNull();

    fireEvent.click(skipBtn);

    await waitFor(() => {
      expect(deleteNotificationMock).toHaveBeenCalledTimes(1);
      expect(deleteNotificationMock).toHaveBeenCalledWith(notifications[0]);
    });
  });
});
