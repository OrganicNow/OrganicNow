// src/component/test/Toast.test.jsx
import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import { describe, it, expect } from "vitest";

import { ToastProvider, useToast } from "../../contexts/ToastContext";

describe("ToastContext / ToastProvider / Toast (basic behavior)", () => {
  it("ควรโยน error ถ้าเรียก useToast นอก ToastProvider", () => {
    const TestComponent = () => {
      // เรียก useToast นอก ToastProvider -> ควร throw
      useToast();
      return null;
    };

    expect(() => render(<TestComponent />)).toThrow(
      "useToast must be used within a ToastProvider"
    );
  });

  it("ToastProvider ควร render children และมี toast-container อยู่ใน DOM", () => {
    const Child = () => <div>dummy child</div>;

    const { container } = render(
      <ToastProvider>
        <Child />
      </ToastProvider>
    );

    // children ต้องถูก render
    expect(screen.getByText("dummy child")).toBeInTheDocument();

    // จาก log เห็นว่ามี <div class="toast-container"></div> อยู่เสมอ
    const containerEl = container.querySelector(".toast-container");
    expect(containerEl).not.toBeNull();
  });

  it("เมื่อเรียก useToast ภายใน ToastProvider ควรได้ค่า context บางอย่างกลับมา (ไม่ว่างเปล่า)", () => {
    const Consumer = () => {
      const value = useToast();
      // แปลงเป็น string เอาไว้เช็กเฉย ๆ
      return (
        <div data-testid="toast-context-value">
          {value !== undefined && value !== null
            ? JSON.stringify(value)
            : ""}
        </div>
      );
    };

    render(
      <ToastProvider>
        <Consumer />
      </ToastProvider>
    );

    const div = screen.getByTestId("toast-context-value");
    const text = div.textContent || "";

    // แค่เช็กว่าไม่ใช่ string ว่าง แปลว่ามีค่า context บางอย่าง
    expect(text).not.toEqual("");
  });
});
