import React, { useRef } from "react";
import { Button } from "primereact/button";
import { Badge } from "primereact/badge";
import { Avatar } from "primereact/avatar";
import { Menu } from "primereact/menu";
import { profileMenuItems, settingsMenuItems } from "./menuitem";
import { useNavigate } from "react-router-dom";
import NotificationBell from "./NotificationBell";

import "primereact/resources/themes/lara-light-indigo/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "../assets/css/topbar.css";

export default function Topbar({ title = "", icon = "" }) {
  const profileMenu = useRef(null);
  const settingsMenu = useRef(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    // ✅ แจ้งเตือนก่อน logout (ถ้าต้องการ)
    if (window.confirm("Are you sure you want to logout?")) {
      // ไม่มี auth context แล้ว สามารถ navigate ไปหน้าอื่นได้เลย
      navigate("/dashboard", { replace: true });
    }
  };

  // ✅ Enhanced profile menu with logout
  const enhancedProfileMenuItems = [
    ...profileMenuItems.filter(item => item.label !== "Logout"), // กรอง Logout ออกจาก profileMenuItems
    { separator: true },
    {
      label: "Logout",
      icon: "pi pi-sign-out",
      command: handleLogout,
      style: { color: '#dc3545' } // สีแดงเพื่อเน้น
    }
  ];

  return (
    <header className="topbar">
      <div className="topbar-inner">
        {/* Left: Icon + Title */}
        <div className="topbar-left">
          {icon && <i className={`topbar-icon ${icon}`} />}
          <h2 className="topbar-title">{title}</h2>
        </div>

        {/* Right: Bell + Cog + Profile */}
        <div className="topbar-right">
          <NotificationBell />

          <span>
            <Button
              icon="pi pi-cog"
              className="p-button-rounded p-button-text topbar-btn"
              aria-label="Settings"
              onClick={(e) => settingsMenu.current.toggle(e)}
              tooltip="Settings"
              tooltipOptions={{ position: "bottom" }}
            />
            <Menu model={settingsMenuItems} popup ref={settingsMenu} appendTo={document.body} />
          </span>

          <div
            className="topbar-profile"
            onClick={(e) => profileMenu.current.toggle(e)}
          >
            <Avatar icon="pi pi-user" shape="circle" className="topbar-avatar" />
            <span className="topbar-username">Admin User</span>
            <Menu model={enhancedProfileMenuItems} popup ref={profileMenu} appendTo={document.body} />
          </div>
        </div>
      </div>
    </header>
  );
}