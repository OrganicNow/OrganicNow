import React from "react";
import {
  render,
  screen,
  fireEvent,
  waitFor,
  cleanup,        // ✅ เพิ่ม cleanup
} from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest"; // ✅ เพิ่ม afterEach

// ------- shared mocks -------
let mockUser = null;
const mockLogout = vi.fn();
const mockShowMessageConfirmProcess = vi.fn();
const mockNavigate = vi.fn();

// ------- module mocks -------

// ✅ mock AuthContext (path จากไฟล์เทส → ../../contexts/AuthContext)
vi.mock("../../contexts/AuthContext", () => ({
  __esModule: true,
  useAuth: () => ({
    logout: mockLogout,
    user: mockUser,
  }),
}));

// ✅ mock useMessage (Topbar import "./useMessage" → จากไฟล์เทสต้อง "../useMessage")
vi.mock("../useMessage", () => ({
  __esModule: true,
  default: () => ({
    showMessageConfirmProcess: mockShowMessageConfirmProcess,
  }),
}));

// ✅ mock useNavigate
vi.mock("react-router-dom", () => ({
  __esModule: true,
  useNavigate: () => mockNavigate,
}));

// ✅ mock NotificationBell (Topbar import "./NotificationBell" → จากเทสใช้ "../NotificationBell")
vi.mock("../NotificationBell", () => ({
  __esModule: true,
  default: () => <div data-testid="notification-bell" />,
}));

// ✅ mock primereact components แบบง่าย ๆ
vi.mock("primereact/button", () => ({
  __esModule: true,
  Button: ({ children, ...props }) => <button {...props}>{children}</button>,
}));

vi.mock("primereact/badge", () => ({
  __esModule: true,
  Badge: (props) => <span {...props} />,
}));

vi.mock("primereact/avatar", () => ({
  __esModule: true,
  Avatar: (props) => (
    <div data-testid="avatar" {...props}>
      {props.children}
    </div>
  ),
}));

// ✅ mock Menu ให้มี ref.toggle() + render ปุ่มตาม model
vi.mock("primereact/menu", () => {
  const React = require("react");

  const Menu = React.forwardRef(({ model = [] }, ref) => {
    React.useImperativeHandle(ref, () => ({
      toggle: vi.fn(),
    }));

    return (
      <div data-testid="profile-menu-mock">
        {model.map((item, index) =>
          item.separator ? (
            <hr key={`sep-${index}`} data-testid={`separator-${index}`} />
          ) : (
            <button
              key={item.label ?? index}
              type="button"
              onClick={item.command}
              data-testid={`menu-item-${item.label}`}
            >
              {item.label}
            </button>
          )
        )}
      </div>
    );
  });

  return { __esModule: true, Menu };
});

// ⚠️ import Topbar หลังจาก mock ทั้งหมดด้านบน
import Topbar from "../topbar";

beforeEach(() => {
  mockUser = null;
  mockLogout.mockReset();
  mockShowMessageConfirmProcess.mockReset();
  mockNavigate.mockReset();
});

// ✅ ล้าง DOM ทุกครั้งหลังจบเทส เพื่อไม่ให้ Topbar จากเทสก่อนหน้าค้างอยู่
afterEach(() => {
  cleanup();
});

describe("Topbar component", () => {
  it("ควรแสดง title และ icon ที่ส่งเข้ามา พร้อม NotificationBell", () => {
    mockUser = {
      adminUsername: "alex",
      adminRole: 1,
    };

    render(<Topbar title="Dashboard" icon="pi pi-home" />);

    // title
    expect(screen.getByText("Dashboard")).toBeInTheDocument();

    // icon class
    const iconEl = document.querySelector(".topbar-icon.pi.pi-home");
    expect(iconEl).toBeInTheDocument();

    // NotificationBell ถูก render
    expect(screen.getByTestId("notification-bell")).toBeInTheDocument();
  });

  it("เมื่อไม่มี user ใน context ควรแสดง Admin User เป็นชื่อดีฟอลต์", () => {
    mockUser = null;

    render(<Topbar title="Dashboard" />);

    expect(screen.getByText("Admin User")).toBeInTheDocument();

    // ไม่ควรมี role label
    expect(screen.queryByText("Super Admin")).not.toBeInTheDocument();
    expect(screen.queryByText("Admin")).not.toBeInTheDocument();
  });

  it("เมื่อ user.adminRole = 1 ควรแสดงชื่อและ label Super Admin", () => {
    mockUser = {
      adminUsername: "superadmin",
      adminRole: 1,
    };

    render(<Topbar title="Dashboard" />);

    expect(screen.getByText("superadmin")).toBeInTheDocument();
    expect(screen.getByText("Super Admin")).toBeInTheDocument();
  });

  it("เมื่อ user.adminRole != 1 ควรแสดงชื่อและ label Admin", () => {
    mockUser = {
      adminUsername: "normaladmin",
      adminRole: 0,
    };

    render(<Topbar title="Dashboard" />);

    expect(screen.getByText("normaladmin")).toBeInTheDocument();
    expect(screen.getByText("Admin")).toBeInTheDocument();
  });

  it("เมื่อคลิก Logout แล้วกดยืนยัน isConfirmed = true ควรเรียก logout('/dashboard')", async () => {
    mockUser = {
      adminUsername: "alex",
      adminRole: 1,
    };

    mockShowMessageConfirmProcess.mockResolvedValue({ isConfirmed: true });

    render(<Topbar title="Dashboard" />);

    // ปุ่ม Logout มาจาก Menu mock
    const logoutButton = screen.getByTestId("menu-item-Logout");
    fireEvent.click(logoutButton);

    expect(mockShowMessageConfirmProcess).toHaveBeenCalledWith(
      "Are you sure you want to logout?"
    );

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalledTimes(1);
      expect(mockLogout).toHaveBeenCalledWith("/dashboard");
    });
  });

  it("เมื่อคลิก Logout แต่ isConfirmed = false ไม่ควรเรียก logout", async () => {
    mockUser = {
      adminUsername: "alex",
      adminRole: 1,
    };

    mockShowMessageConfirmProcess.mockResolvedValue({ isConfirmed: false });

    render(<Topbar title="Dashboard" />);

    const logoutButton = screen.getByTestId("menu-item-Logout");
    fireEvent.click(logoutButton);

    expect(mockShowMessageConfirmProcess).toHaveBeenCalled();

    await waitFor(() => {
      expect(mockLogout).not.toHaveBeenCalled();
    });
  });

  it("เมื่อคลิกที่โซนโปรไฟล์ ควรไม่ crash (ref.toggle ถูก mock แล้ว)", () => {
    mockUser = {
      adminUsername: "alex",
      adminRole: 1,
    };

    const { container } = render(<Topbar title="Dashboard" />);

    const profileArea = container.querySelector(".topbar-profile");
    expect(profileArea).toBeInTheDocument();

    fireEvent.click(profileArea); // แค่ให้แน่ใจว่าไม่ error
  });
});
