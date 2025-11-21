import React from "react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, waitFor } from "@testing-library/react";
import { AuthProvider, useAuth } from "../AuthContext";

// ให้ fetch เป็น mock function เสมอ
global.fetch = vi.fn();

let hookResult = null;

// component เอาไว้ดึงค่า context จาก useAuth แล้วเก็บใน hookResult
function TestComponent() {
  hookResult = useAuth();
  return null;
}

// helper render AuthProvider + TestComponent
const renderWithProvider = () => {
  render(
    <AuthProvider>
      <TestComponent />
    </AuthProvider>
  );
};

beforeEach(() => {
  hookResult = null;
  vi.clearAllMocks();
  sessionStorage.clear();

  // reset location
  delete window.location;
  window.location = {
    href: "/",
    pathname: "/",
  };
});

describe("AuthContext & useAuth", () => {
  it("ควรโยน error ถ้าเรียก useAuth นอก AuthProvider", () => {
    const originalError = console.error;
    console.error = vi.fn(); // กัน log แดงเยอะ

    const Wrapper = () => {
      useAuth();
      return null;
    };

    expect(() => render(<Wrapper />)).toThrow(
      "useAuth must be used within an AuthProvider"
    );

    console.error = originalError;
  });

  it("login สำเร็จ: ตั้ง user, isAuthenticated และเก็บ sessionStorage + sessionToken", async () => {
    const fakeLoginResponse = {
      success: true,
      data: {
        admin: { adminUsername: "alex", adminRole: 1 },
        token: "abc123",
      },
    };

    // ไม่มี user ใน sessionStorage → checkAuthStatus จะไม่ยิง fetch
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeLoginResponse, // ใช้กับ login โดยตรง
    });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    const result = await hookResult.login("alex", "password");

    expect(result).toEqual({ success: true });

    await waitFor(() => {
      expect(hookResult.user).toEqual(fakeLoginResponse.data.admin);
      expect(hookResult.isAuthenticated).toBe(true);
      expect(sessionStorage.getItem("user")).toBe(
        JSON.stringify(fakeLoginResponse.data.admin)
      );
      expect(sessionStorage.getItem("sessionToken")).toBe("abc123");
    });
  });

  it("login ไม่สำเร็จ: success = false และไม่เซ็ต user/isAuthenticated", async () => {
    const fakeLoginFailResponse = {
      success: false,
      message: "Invalid username or password",
    };

    // ไม่มี user ใน sessionStorage → checkAuthStatus จะไม่ยิง fetch
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeLoginFailResponse, // ใช้กับ login โดยตรง
    });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    const result = await hookResult.login("alex", "wrong");

    expect(result).toEqual({
      success: false,
      message: fakeLoginFailResponse.message,
    });

    await waitFor(() => {
      expect(hookResult.user).toBeNull();
      expect(hookResult.isAuthenticated).toBe(false);
      expect(sessionStorage.getItem("user")).toBeNull();
      expect(sessionStorage.getItem("sessionToken")).toBeNull();
    });
  });

    it("logout ควรล้าง user, isAuthenticated และ sessionStorage", async () => {
      const fakeLoginResponse = {
        success: true,
        data: {
          admin: { adminUsername: "alex", adminRole: 1 },
          token: "abc123",
        },
      };

      // ลำดับ fetch:
      // 1) login
      // 2) logout
      global.fetch
        .mockResolvedValueOnce({
          ok: true,
          json: async () => fakeLoginResponse, // login
        })
        .mockResolvedValueOnce({
          ok: true,
          json: async () => ({}), // logout
        });

      renderWithProvider();

      await waitFor(() => {
        expect(hookResult).not.toBeNull();
      });

      // login ก่อน
      await hookResult.login("alex", "password");

      await waitFor(() => {
        expect(hookResult.isAuthenticated).toBe(true);
        expect(hookResult.user).toEqual(fakeLoginResponse.data.admin);
      });

      // แล้วค่อย logout
      await hookResult.logout("/dashboard");

      // ⭐ รอให้ React อัปเดต state ก่อนค่อยเช็ค
      await waitFor(() => {
        expect(hookResult.user).toBeNull();
        expect(hookResult.isAuthenticated).toBe(false);
      });

      expect(sessionStorage.getItem("user")).toBeNull();
      expect(sessionStorage.getItem("sessionToken")).toBeNull();
      expect(window.location.href).toBe("/dashboard");
    });


  it("hasPermission: adminRole = 1 ควรผ่านทั้ง admin, super_admin, manage_packages", async () => {
    const superAdminUser = { adminUsername: "superadmin", adminRole: 1 };

    const fakeLoginResponse = {
      success: true,
      data: {
        admin: superAdminUser,
        token: "token-superadmin",
      },
    };

    // ไม่ใช้ checkAuth เลย ใช้ login ตั้ง user ให้เป็น superadmin
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeLoginResponse,
    });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    await hookResult.login("superadmin", "password");

    await waitFor(() => {
      expect(hookResult.isAuthenticated).toBe(true);
      expect(hookResult.user).toEqual(superAdminUser);
    });

    expect(hookResult.hasPermission("admin")).toBe(true);
    expect(hookResult.hasPermission("super_admin")).toBe(true);
    expect(hookResult.hasPermission("manage_packages")).toBe(true);
  });

  it("hasPermission: adminRole = 0 ควรผ่านเฉพาะ admin", async () => {
    const normalAdminUser = { adminUsername: "normaladmin", adminRole: 0 };

    const fakeLoginResponse = {
      success: true,
      data: {
        admin: normalAdminUser,
        token: "token-normaladmin",
      },
    };

    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => fakeLoginResponse,
    });

    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    await hookResult.login("normaladmin", "password");

    await waitFor(() => {
      expect(hookResult.isAuthenticated).toBe(true);
      expect(hookResult.user).toEqual(normalAdminUser);
    });

    expect(hookResult.hasPermission("admin")).toBe(true);
    expect(hookResult.hasPermission("super_admin")).toBe(false);
    expect(hookResult.hasPermission("manage_packages")).toBe(false);
  });

  it("hasPermission: ถ้าไม่มี user ต้องคืน false ทุกกรณี", async () => {
    // ไม่มี user ใน sessionStorage → useEffect จะไม่เรียก fetch เลย
    renderWithProvider();

    await waitFor(() => {
      expect(hookResult).not.toBeNull();
    });

    expect(hookResult.user).toBeNull();
    expect(hookResult.hasPermission("admin")).toBe(false);
    expect(hookResult.hasPermission("super_admin")).toBe(false);
    expect(hookResult.hasPermission("manage_packages")).toBe(false);
  });
});
