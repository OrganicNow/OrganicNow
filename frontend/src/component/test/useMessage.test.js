import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render } from "@testing-library/react";
import useMessage from "../useMessage";
import Swal from "sweetalert2";

// ✅ mock useNavigate ให้คืน mock fn
const mockNavigate = vi.fn();

vi.mock("react-router-dom", () => ({
  useNavigate: () => mockNavigate,
}));

// ✅ mock SweetAlert2
vi.mock("sweetalert2", () => {
  return {
    default: {
      fire: vi.fn(),
    },
  };
});

// helper ใช้เรียก hook โดยไม่ต้องมี UI จริง (ไม่ใช้ JSX)
const setupUseMessage = () => {
  let hookResult;

  const TestComponent = () => {
    hookResult = useMessage();
    return null;
  };

  render(React.createElement(TestComponent));
  return hookResult;
};

describe("useMessage hook", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("showMessageAdjust ควรเรียก Swal.fire ด้วย text และ icon ที่ถูกต้อง", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    hookResult.showMessageAdjust("Test message", "warning");

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: "Test message",
        icon: "warning",
        showConfirmButton: true,
        confirmButtonText: "Close Window",
      })
    );
  });

  it("showMessagePermission ควรโชว์ error และ navigate ไป mainRoute+'/'", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    hookResult.showMessagePermission();

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: "You not have permission, Please contact administrator",
        icon: "error",
      })
    );

    // ไม่ผูกกับ implementation ของ session/local storage แล้ว
    expect(mockNavigate).toHaveBeenCalledTimes(1);
    expect(mockNavigate).toHaveBeenCalledWith(expect.stringContaining("/"));
  });

  it("showMessageError (string ที่มี duplicate_national_id) ควรแมปข้อความ NationalID Already Exists", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    hookResult.showMessageError("some error duplicate_national_id here");

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        title: "Error",
        text: "NationalID Already Exists",
        icon: "error",
      })
    );
  });

  it("showMessageError (axios error + duplicate_national_id) ควรแสดง National ID already exists", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    const axiosLikeError = {
      response: {
        data: {
          message: "duplicate_national_id something",
          detail: "more detail",
        },
      },
    };

    hookResult.showMessageError(axiosLikeError);

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        title: "Error",
        text: "National ID already exists",
        icon: "error",
      })
    );
  });

  it("showMessageSave ควรเรียก Swal.fire ด้วยข้อความ success ที่ถูกต้อง", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    hookResult.showMessageSave();

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: "The process has been saved successfully.",
        icon: "success",
        showConfirmButton: true,
        confirmButtonText: "Close Window",
      })
    );
  });

  it("showMessageSend ควรเรียก Swal.fire ด้วยข้อความ success ที่ถูกต้อง", () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    hookResult.showMessageSend();

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: "The process has been sent successfully.",
        icon: "success",
        showConfirmButton: true,
        confirmButtonText: "Close Window",
      })
    );
  });

  it("showMessageConfirmDelete ควรเรียก Swal.fire แบบ confirm/cancel และคืนค่า result", async () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    const fakeResult = { isConfirmed: true, isDismissed: false };
    fireMock.mockResolvedValueOnce(fakeResult);

    const result = await hookResult.showMessageConfirmDelete("TEST DATA");

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: 'Are you sure to delete "TEST DATA"?',
        icon: "error",
        showCancelButton: true,
        showConfirmButton: true,
      })
    );
    expect(result).toBe(fakeResult);
  });

  it("showMessageConfirmProcess ควรเรียก Swal.fire ด้วย text ที่ส่งมา และคืนค่า result", async () => {
    const hookResult = setupUseMessage();
    const fireMock = Swal.fire;

    const fakeResult = { isConfirmed: false, isDismissed: true };
    fireMock.mockResolvedValueOnce(fakeResult);

    const result = await hookResult.showMessageConfirmProcess(
      "Custom confirm text"
    );

    expect(fireMock).toHaveBeenCalledTimes(1);
    expect(fireMock).toHaveBeenCalledWith(
      expect.objectContaining({
        text: "Custom confirm text",
        icon: "question",
        showCancelButton: true,
        showConfirmButton: true,
      })
    );
    expect(result).toBe(fakeResult);
  });
});
