// frontend/src/component/test/Layout.test.jsx
import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import Layout from "../layout"; // ถ้าไฟล์ชื่อ Layout.jsx ให้เปลี่ยนเป็น "../Layout"

// ตัวแปรเอาไว้เก็บ props ล่าสุดที่ส่งเข้า Topbar
let lastTopbarProps = null;

// 🧪 Mock SideBar component
vi.mock("../sidebar", () => ({
  __esModule: true,
  default: () => <div data-testid="sidebar">Sidebar Mock</div>,
}));

// 🧪 Mock Topbar component เพื่อดูว่า Layout ส่ง props อะไรมา
vi.mock("../topbar", () => {
  const MockTopbar = (props) => {
    lastTopbarProps = props;
    return (
      <div data-testid="topbar">
        <span>{props.title}</span>
      </div>
    );
  };

  return {
    __esModule: true,
    default: MockTopbar,
  };
});

describe("Layout component", () => {
  beforeEach(() => {
    // reset ค่า props ที่เก็บไว้ก่อนแต่ละเทสต์
    lastTopbarProps = null;
  });

  it("ควร render Sidebar, Topbar และ children ครบ และส่ง default props ให้ Topbar", () => {
    render(
      <Layout>
        <div data-testid="child">Hello Content</div>
      </Layout>
    );

    // ✅ ตรวจว่ามี Sidebar, Topbar, children
    expect(screen.getByTestId("sidebar")).toBeTruthy();
    expect(screen.getByTestId("topbar")).toBeTruthy();
    expect(screen.getByTestId("child")).toBeTruthy();

    // ✅ ตรวจโครงสร้าง class หลัก ๆ
    expect(document.querySelector(".app-shell")).not.toBeNull();
    expect(document.querySelector(".app-main")).not.toBeNull();
    expect(document.querySelector(".app-content")).not.toBeNull();

    // ✅ ตรวจว่า Topbar ได้รับ default props ถูกต้อง
    expect(lastTopbarProps).not.toBeNull();
    expect(lastTopbarProps.title).toBe("Page Title");
    expect(lastTopbarProps.icon).toBe("pi pi-home");
    expect(lastTopbarProps.notifications).toBe(0);
  });

  it("ควรส่ง title, icon, notifications ที่รับมาจาก props ต่อให้ Topbar", () => {
    render(
      <Layout
        title="Dashboard"
        icon="pi pi-chart-bar"
        notifications={7}
      >
        <div>Another Content</div>
      </Layout>
    );

    expect(lastTopbarProps).not.toBeNull();
    expect(lastTopbarProps.title).toBe("Dashboard");
    expect(lastTopbarProps.icon).toBe("pi pi-chart-bar");
    expect(lastTopbarProps.notifications).toBe(7);
  });
});
