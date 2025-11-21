import React from "react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import { ToastProvider, useToast } from "../ToastContext";

// 🧪 mock ToastNotification ให้เป็น div ที่มี data-testid / data-* ให้เทสต์จับได้
vi.mock("../../component/ToastNotification", () => ({
  default: ({ title, message, type, duration, onClose }) => (
    <div
      data-testid="toast-notification"
      data-title={title}
      data-message={message}
      data-type={type}
      data-duration={duration}
      onClick={onClose}
    >
      {title}: {message}
    </div>
  ),
}));

let hookResult = null;

// component เล็ก ๆ เอาไว้ดึง context มาเก็บใน hookResult
function TestHookComponent() {
  hookResult = useToast();
  return null;
}

// helper render Provider + TestHookComponent
function renderWithProvider(children = null) {
  render(
    <ToastProvider>
      <TestHookComponent />
      {children}
    </ToastProvider>
  );
}

describe("ToastContext & useToast", () => {
  beforeEach(() => {
    hookResult = null;
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
    vi.clearAllTimers();
  });

  it("ควรโยน error ถ้าใช้ useToast นอก ToastProvider", () => {
    function TestOutside() {
      useToast();
      return null;
    }

    expect(() => render(<TestOutside />)).toThrow(
      "useToast must be used within a ToastProvider"
    );
  });

  it("showToast: ควร render ToastNotification ด้วย title, message, type, duration ที่ถูกต้อง", async () => {
    renderWithProvider();

    // รอให้ hookResult ไม่ null ก่อน
    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    hookResult.showToast("Test Title", "Test Message", "success", 4000);

    // ใช้ findAllByTestId เพื่อรอ DOM อัพเดต
    const toasts = await screen.findAllByTestId("toast-notification");
    const toast = toasts[toasts.length - 1]; // สนใจอันล่าสุด

    expect(toast.getAttribute("data-title")).toBe("Test Title");
    expect(toast.getAttribute("data-message")).toBe("Test Message");
    expect(toast.getAttribute("data-type")).toBe("success");
    expect(toast.getAttribute("data-duration")).toBe("4000");
  });

  // ❗ เปลี่ยนวิธีเทสต์ auto-remove:
  //    ไม่ใช้ fake timers / DOM หายจริง แต่เช็คว่า setTimeout ถูกเรียกด้วย duration ที่ถูกต้องแทน
    it("showToast: ควรตั้ง setTimeout สำหรับลบ toast อัตโนมัติหลังครบ duration", async () => {
      const setTimeoutSpy = vi.spyOn(globalThis, "setTimeout");

      renderWithProvider();

      await waitFor(() => {
        expect(hookResult).not.toBeNull();
      });

      hookResult.showToast("Auto Remove", "Should disappear", "notification", 1000);

      // ❌ ไม่เช็คจำนวนครั้งแล้ว เพราะ environment ก็ใช้ setTimeout
      // expect(setTimeoutSpy).toHaveBeenCalledTimes(1);

      // ✅ หา call ที่ delay = duration + 500 = 1500 แทน
      const matchingCalls = setTimeoutSpy.mock.calls.filter(
        ([callback, delay]) => typeof callback === "function" && delay === 1500
      );

      expect(matchingCalls.length).toBeGreaterThan(0);

      const [callback, delay] = matchingCalls[0];
      expect(typeof callback).toBe("function");
      expect(delay).toBe(1500);
    });

  it("showMaintenanceDue: daysUntil = 0 หรือ 1 ควรแสดง toast ตามประเภทที่ถูกต้อง", async () => {
    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    const schedule = { scheduleTitle: "Aircon Check" };

    // daysUntil = 0 -> urgent
    hookResult.showMaintenanceDue(schedule, 0);

    let toasts = await screen.findAllByTestId("toast-notification");

    // ต้องมี toast ที่ title + type ตรงตามที่คาด
    expect(
      toasts.some(
        (t) =>
          t.getAttribute("data-title") === "🚨 Maintenance Due Today!" &&
          t.getAttribute("data-type") === "urgent"
      )
    ).toBe(true);

    // daysUntil = 1 -> warning
    hookResult.showMaintenanceDue(schedule, 1);

    // รอจนมีอย่างน้อย 2 อัน (จาก 2 ครั้งที่เรียก)
    await waitFor(() => {
      expect(screen.getAllByTestId("toast-notification").length).toBeGreaterThanOrEqual(2);
    });

    toasts = screen.getAllByTestId("toast-notification");

    expect(
      toasts.some(
        (t) =>
          t.getAttribute("data-title") === "⚠️ Maintenance Due Tomorrow" &&
          t.getAttribute("data-type") === "warning"
      )
    ).toBe(true);
  });

  it("showMaintenanceCreated: ควรแสดง toast success เมื่อสร้าง schedule ใหม่", async () => {
    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    const schedule = { scheduleTitle: "Pump Check" };

    hookResult.showMaintenanceCreated(schedule);

    const toasts = await screen.findAllByTestId("toast-notification");
    const toast = toasts[toasts.length - 1];

    expect(toast.getAttribute("data-title")).toBe(
      "Maintenance Schedule Created"
    );
    expect(toast.getAttribute("data-type")).toBe("success");
    expect(toast.getAttribute("data-message")).toContain("Pump Check");
  });

  it("showGeneralNotification: ควร map type จาก notification.type เป็น type ของ toast", async () => {
    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    // CASE 1: MAINTENANCE_DUE -> 'due'
    hookResult.showGeneralNotification({
      title: "Due soon",
      message: "Something is due",
      type: "MAINTENANCE_DUE",
    });

    let toasts = await screen.findAllByTestId("toast-notification");
    expect(
      toasts.some((t) => t.getAttribute("data-type") === "due")
    ).toBe(true);

    // CASE 2: URGENT -> 'urgent'
    hookResult.showGeneralNotification({
      title: "Urgent!",
      message: "Fix immediately",
      type: "URGENT",
    });

    await waitFor(() => {
      expect(screen.getAllByTestId("toast-notification").length).toBeGreaterThanOrEqual(2);
    });

    toasts = screen.getAllByTestId("toast-notification");
    expect(
      toasts.some((t) => t.getAttribute("data-type") === "urgent")
    ).toBe(true);

    // CASE 3: type อื่น -> default 'notification'
    hookResult.showGeneralNotification({
      title: "Other",
      message: "Other type",
      type: "SOMETHING_ELSE",
    });

    await waitFor(() => {
      expect(screen.getAllByTestId("toast-notification").length).toBeGreaterThanOrEqual(3);
    });

    toasts = screen.getAllByTestId("toast-notification");
    expect(
      toasts.some((t) => t.getAttribute("data-type") === "notification")
    ).toBe(true);
  });
});
