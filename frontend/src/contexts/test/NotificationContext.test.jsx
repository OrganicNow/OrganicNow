import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, waitFor } from "@testing-library/react";
import {
  NotificationProvider,
  useNotifications,
} from "../NotificationContext";

// ตัวแปรไว้เก็บค่าจาก useNotifications
let hookResult = null;

// component เล็ก ๆ ไว้ดึง context ออกมา
function TestComponent() {
  hookResult = useNotifications();
  return null;
}

// helper สำหรับ render พร้อม Provider
function renderWithProvider() {
  render(
    <NotificationProvider>
      <TestComponent />
    </NotificationProvider>
  );
}

describe("NotificationContext & useNotifications", () => {
  beforeEach(() => {
    hookResult = null;
    vi.clearAllMocks();
    // mock fetch ให้เป็น spy ทุกเทส
    global.fetch = vi.fn();
  });

  it("refreshNotifications: ควรเรียก API และตั้ง notifications + unreadCount ให้ถูกต้อง", async () => {
    const fakeNotifications = [
      { id: 1, scheduleId: 10, nextDueDate: "2025-01-01" },
      { id: 2, scheduleId: 11, nextDueDate: "2025-01-02" },
    ];

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeNotifications,
    });

    renderWithProvider();

    // รอให้ hookResult ถูกเซ็ตจาก TestComponent
    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    // เรียก refresh
    await hookResult.refreshNotifications();

    // ตรวจว่า fetch ถูกเรียก
    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect(global.fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/notifications/due",
      expect.objectContaining({
        credentials: "include",
      })
    );

    // 🔧 ใช้ waitFor รอให้ React อัปเดต state ให้เสร็จ
    await waitFor(() => {
      expect(hookResult.notifications).toEqual(fakeNotifications);
      expect(hookResult.unreadCount).toBe(2);
      expect(hookResult.loading).toBe(false);
    });
  });

  it("refreshNotifications: ถ้า response ไม่ใช่ array ควรตั้ง notifications เป็น [] และ unreadCount = 0", async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ foo: "bar" }), // ไม่ใช่ array
    });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    await hookResult.refreshNotifications();

    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect(hookResult.notifications).toEqual([]);
    expect(hookResult.unreadCount).toBe(0);
    expect(hookResult.loading).toBe(false);
  });

  it("skipNotification/deleteNotification: ควรเรียก DELETE และ refreshNotifications ตามลำดับ", async () => {
    const n = { scheduleId: 99, nextDueDate: "2025-12-31" };

    // ลำดับ fetch:
    // 1) DELETE /skip
    // 2) GET /due (จาก refreshNotifications)
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({}), // สำหรับ DELETE
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [], // สำหรับ refreshNotifications
      });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    // deleteNotification alias ของ skipNotification
    await hookResult.deleteNotification(n);

    expect(global.fetch).toHaveBeenCalledTimes(2);

    // call แรก: DELETE
    expect(global.fetch).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/api/notifications/schedule/99/due/2025-12-31/skip",
      expect.objectContaining({
        method: "DELETE",
        credentials: "include",
      })
    );

    // call ที่สอง: refreshNotifications
    expect(global.fetch).toHaveBeenNthCalledWith(
      2,
      "http://localhost:8080/api/notifications/due",
      expect.objectContaining({
        credentials: "include",
      })
    );
  });

  it("markAsRead และ markAllAsRead: ควรเป็นฟังก์ชันที่เรียกได้โดยไม่ error และไม่เรียก fetch", async () => {
    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    expect(typeof hookResult.markAsRead).toBe("function");
    expect(typeof hookResult.markAllAsRead).toBe("function");

    hookResult.markAsRead();
    hookResult.markAllAsRead();

    // ไม่ควรไปเรียก API ใด ๆ
    expect(global.fetch).not.toHaveBeenCalled();
  });
});
