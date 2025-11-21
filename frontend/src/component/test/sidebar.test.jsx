// src/component/test/sidebar.test.jsx
import React from "react";
import { render } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { MemoryRouter } from "react-router-dom";

// ✅ mock AuthContext.useAuth ก่อน import SideBar
vi.mock("../../contexts/AuthContext", () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from "../../contexts/AuthContext";
import SideBar from "../sidebar";

describe("SideBar component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderWithRouter = (initialPath = "/dashboard") => {
    return render(
      <MemoryRouter initialEntries={[initialPath]}>
        <SideBar />
      </MemoryRouter>
    );
  };

  it("ควรแสดงเมนูพื้นฐานทุกตัว และไม่แสดง Package Management เมื่อไม่มีสิทธิ์ super_admin", () => {
    const hasPermissionMock = vi.fn().mockReturnValue(false);
    useAuth.mockReturnValue({ hasPermission: hasPermissionMock });

    const { container } = renderWithRouter("/dashboard");

    // Dashboard
    expect(
      container.querySelector('[data-tooltip="Dashboard"]')
    ).not.toBeNull();

    // Tenant Management
    expect(
      container.querySelector('[data-tooltip="Tenant Management"]')
    ).not.toBeNull();

    // Room Management
    expect(
      container.querySelector('[data-tooltip="Room Management"]')
    ).not.toBeNull();

    // Maintenance Request
    expect(
      container.querySelector('[data-tooltip="Maintenance Request"]')
    ).not.toBeNull();

    // Asset Management
    expect(
      container.querySelector('[data-tooltip="Asset Management"]')
    ).not.toBeNull();

    // Invoice Management
    expect(
      container.querySelector('[data-tooltip="Invoice Management"]')
    ).not.toBeNull();

    // Maintenance Schedule
    expect(
      container.querySelector('[data-tooltip="Maintenance Schedule"]')
    ).not.toBeNull();

    // ❌ ไม่ควรมี Package Management
    expect(
      container.querySelector(
        '[data-tooltip="Package Management (Super Admin Only)"]'
      )
    ).toBeNull();

    // เรียก hasPermission ด้วย 'super_admin'
    expect(hasPermissionMock).toHaveBeenCalledWith("super_admin");
  });

  it("ควรแสดงเมนู Package Management เมื่อ hasPermission('super_admin') เป็น true", () => {
    const hasPermissionMock = vi.fn().mockReturnValue(true);
    useAuth.mockReturnValue({ hasPermission: hasPermissionMock });

    const { container } = renderWithRouter("/dashboard");

    const pkgLink = container.querySelector(
      '[data-tooltip="Package Management (Super Admin Only)"]'
    );
    expect(pkgLink).not.toBeNull();
    expect(hasPermissionMock).toHaveBeenCalledWith("super_admin");
  });

  it("ควรใส่คลาส active ให้ลิงก์ที่ตรงกับ path ปัจจุบัน (Dashboard และ Tenant Management)", () => {
    // 🔹 กรณี path = /dashboard
    useAuth.mockReturnValue({
      hasPermission: vi.fn().mockReturnValue(true),
    });

    let { container, unmount } = renderWithRouter("/dashboard");

    const dashboardLink = container.querySelector(
      '[data-tooltip="Dashboard"]'
    );
    const tenantLink = container.querySelector(
      '[data-tooltip="Tenant Management"]'
    );

    expect(dashboardLink).not.toBeNull();
    expect(tenantLink).not.toBeNull();

    // Dashboard ใช้ location.pathname === '/dashboard'
    expect(dashboardLink.className).toContain("active");
    // Tenant ตอนนี้ไม่ active
    expect(tenantLink.className).not.toContain("active");

    // 🔹 กรณี path = /tenantmanagement
    unmount();

    ({ container } = renderWithRouter("/tenantmanagement"));

    const dashboardLink2 = container.querySelector(
      '[data-tooltip="Dashboard"]'
    );
    const tenantLink2 = container.querySelector(
      '[data-tooltip="Tenant Management"]'
    );

    // Dashboard ไม่ควร active แล้ว
    expect(dashboardLink2.className).not.toContain("active");
    // Tenant ควร active เพราะใช้ NavLink + linkClass
    expect(tenantLink2.className).toContain("active");
  });
});
