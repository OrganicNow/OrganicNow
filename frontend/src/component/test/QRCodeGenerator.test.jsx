// src/component/test/QRCodeGenerator.test.jsx
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";

// ✅ mock library 'qrcode' ให้ไม่ไปยุ่งกับ canvas จริง
vi.mock("qrcode", () => {
  return {
    default: {
      toCanvas: vi.fn(() => Promise.resolve()),
    },
  };
});

import QRCode from "qrcode";
import QRCodeGenerator from "../QRCodeGenerator";

describe("QRCodeGenerator component", () => {
  beforeEach(() => {
    // เคลียร์ค่า mock ก่อนทุกเทสต์
    QRCode.toCanvas.mockClear();
  });

  it("ควรแสดง fallback เมื่อไม่มี value และไม่เรียก QRCode.toCanvas", () => {
    const { container } = render(<QRCodeGenerator value={""} />);

    // ไม่ควรมี canvas
    const canvas = container.querySelector("canvas");
    expect(canvas).toBeNull();

    // ต้องมีข้อความ error default
    const errorText = screen.getByText("Unable to generate QR code");
    expect(errorText).toBeTruthy();

    // ไม่เรียก toCanvas เลย
    expect(QRCode.toCanvas).not.toHaveBeenCalled();
  });

  it("ควรใช้ errorMessage ที่ส่งเข้ามาเมื่อไม่มี value", () => {
    const customMessage = "กรุณาระบุข้อมูลสำหรับสร้าง QR code";
    const { container } = render(
      <QRCodeGenerator value={null} errorMessage={customMessage} />
    );

    const msgEl = screen.getByText(customMessage);
    expect(msgEl).toBeTruthy();

    // ยังไม่ควรมี canvas
    const canvas = container.querySelector("canvas");
    expect(canvas).toBeNull();
  });

  it("เมื่อมี value ควรแสดง canvas และเรียก QRCode.toCanvas ด้วยพารามิเตอร์ที่ถูกต้อง (ใช้ default size 150)", async () => {
    const { container } = render(<QRCodeGenerator value="https://example.com" />);

    const canvas = container.querySelector("canvas");
    expect(canvas).not.toBeNull();

    // รอให้ useEffect ทำงานแล้วเรียก toCanvas
    await waitFor(() => {
      expect(QRCode.toCanvas).toHaveBeenCalledTimes(1);
    });

    const [canvasArg, valueArg, optionsArg] = QRCode.toCanvas.mock.calls[0];

    expect(canvasArg).toBe(canvas);
    expect(valueArg).toBe("https://example.com");

    // ตรวจ options
    expect(optionsArg.width).toBe(150);
    expect(optionsArg.height).toBe(150);
    expect(optionsArg.color.dark).toBe("#000000");
    expect(optionsArg.color.light).toBe("#FFFFFF");
    expect(optionsArg.margin).toBe(2);
    expect(optionsArg.errorCorrectionLevel).toBe("M");
  });

  it("เมื่อส่ง size เข้ามา ควรใช้ค่า size นั้นในการเรียก QRCode.toCanvas", async () => {
    const { container } = render(
      <QRCodeGenerator value="123456" size={256} />
    );

    const canvas = container.querySelector("canvas");
    expect(canvas).not.toBeNull();

    await waitFor(() => {
      expect(QRCode.toCanvas).toHaveBeenCalledTimes(1);
    });

    const [, , optionsArg] = QRCode.toCanvas.mock.calls[0];

    expect(optionsArg.width).toBe(256);
    expect(optionsArg.height).toBe(256);
  });

  it("เมื่อเปลี่ยน size ควรเรียก QRCode.toCanvas ใหม่ด้วยค่า size ล่าสุด", async () => {
    const { container, rerender } = render(
      <QRCodeGenerator value="ABC" size={150} />
    );

    await waitFor(() => {
      expect(QRCode.toCanvas).toHaveBeenCalledTimes(1);
    });

    QRCode.toCanvas.mockClear();

    // เปลี่ยน size เป็น 300
    rerender(<QRCodeGenerator value="ABC" size={300} />);

    const canvas = container.querySelector("canvas");
    expect(canvas).not.toBeNull();

    await waitFor(() => {
      expect(QRCode.toCanvas).toHaveBeenCalledTimes(1);
    });

    const [, , optionsArg] = QRCode.toCanvas.mock.calls[0];
    expect(optionsArg.width).toBe(300);
    expect(optionsArg.height).toBe(300);
  });

  it("ควรใส่ className เพิ่มบน wrapper หรือ fallback ให้ถูกต้อง", () => {
    // กรณีมี value → ใช้ .qr-code-wrapper
    const { container, rerender } = render(
      <QRCodeGenerator value="XYZ" className="my-extra-class" />
    );

    const wrapper = container.querySelector(".qr-code-wrapper");
    expect(wrapper).not.toBeNull();
    expect(wrapper.className).toContain("my-extra-class");

    // กรณีไม่มี value → className ต้องอยู่บน fallback div
    rerender(
      <QRCodeGenerator value={null} className="my-extra-class" />
    );

    const fallbackDiv = container.querySelector("div.d-flex");
    expect(fallbackDiv).not.toBeNull();
    expect(fallbackDiv.className).toContain("my-extra-class");
  });
});
