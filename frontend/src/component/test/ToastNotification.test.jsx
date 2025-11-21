// src/component/test/ToastNotification.test.jsx
import React from "react";
import {
  render,
  screen,
  fireEvent,
  act,
} from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import { describe, it, expect, vi, afterEach } from "vitest";

import ToastNotification from "../ToastNotification";

afterEach(() => {
  // กันไม่ให้ fake timer ค้างข้ามเทสต์
  vi.useRealTimers();
});

describe("ToastNotification component", () => {
  it("ควรแสดง title, message และ icon ตาม type = success", () => {
    const onClose = vi.fn();

    render(
      <ToastNotification
        title="บันทึกสำเร็จ"
        message="ข้อมูลถูกบันทึกแล้ว"
        type="success"
        duration={10000} // ยืดเวลาไว้กัน auto-close ระหว่างเทสต์นี้
        onClose={onClose}
      />
    );

    // แสดง title
    expect(screen.getByText("บันทึกสำเร็จ")).toBeInTheDocument();
    // แสดง message
    expect(screen.getByText("ข้อมูลถูกบันทึกแล้ว")).toBeInTheDocument();
    // icon ของ success = ✅
    expect(screen.getByText("✅")).toBeInTheDocument();

    // root div ควรมี class พื้นฐาน
    const root = screen
      .getByText("บันทึกสำเร็จ")
      .closest(".toast-notification");
    expect(root).toBeInTheDocument();
    expect(root).toHaveClass("toast-show");
  });

  it("เมื่อไม่ส่ง type เข้ามา ควรใช้ type = notification และแสดง icon 🔔", () => {
    const onClose = vi.fn();

    render(
      <ToastNotification
        title="แจ้งเตือน"
        message="ข้อความแจ้งเตือน"
        // ไม่ส่ง type ให้ใช้ค่า default คือ 'notification'
        onClose={onClose}
      />
    );

    expect(screen.getByText("แจ้งเตือน")).toBeInTheDocument();
    expect(screen.getByText("ข้อความแจ้งเตือน")).toBeInTheDocument();

    // icon สำหรับ notification = 🔔
    expect(screen.getByText("🔔")).toBeInTheDocument();

    const root = screen
      .getByText("แจ้งเตือน")
      .closest(".toast-notification");
    expect(root).toBeInTheDocument();
  });

  it("ควรเรียก onClose อัตโนมัติหลังจาก duration + 300ms", () => {
    vi.useFakeTimers();
    const onClose = vi.fn();

    render(
      <ToastNotification
        title="Auto close"
        message="จะหายเอง"
        type="info"
        duration={1000}
        onClose={onClose}
      />
    );

    // ตอนแรกต้องยังไม่ถูกเรียก
    expect(onClose).not.toHaveBeenCalled();

    // ขยับเวลาไปตาม duration + 300 (เผื่อ animation)
    act(() => {
      vi.advanceTimersByTime(1000 + 300);
    });

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("เมื่อกดปุ่มปิดควรเปลี่ยนเป็น toast-hide และเรียก onClose หลัง 300ms", () => {
    vi.useFakeTimers();
    const onClose = vi.fn();

    // ดึง container เฉพาะของเทสต์นี้ ไม่ปนกับของเทสต์อื่น
    const { container } = render(
      <ToastNotification
        title="Closable"
        message="ปิดเองได้"
        type="warning"
        duration={10000} // ยาว ๆ กัน auto timer มาชน
        onClose={onClose}
      />
    );

    // หา toast เฉพาะอันนี้จาก container
    const toast = container.querySelector(
      ".toast-notification.toast-warning"
    );
    expect(toast).not.toBeNull();

    const closeButton = toast.querySelector(".toast-close");
    expect(closeButton).not.toBeNull();

    fireEvent.click(closeButton);

    // กดแล้ว state isVisible = false → class ควรมี toast-hide
    expect(toast).toHaveClass("toast-hide");

    // ขยับเวลาไป 300ms เพื่อให้ onClose ถูกเรียก
    act(() => {
      vi.advanceTimersByTime(300);
    });

    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
