import React from "react";
import SideBar from "./sidebar";
import Topbar from "./topbar";
import "../assets/css/sidebar.css";
import "../assets/css/topbar.css";

export default function Layout({
  children,
  title = "Page Title",
  icon = "pi pi-home",
  notifications = 0,
}) {
  return (
    <div className="app-shell">
      {/* === Sidebar === */}
      <SideBar />

      {/* === Main area (Topbar + Content) === */}
      <div className="app-main">
        <Topbar title={title} icon={icon} notifications={notifications} />
        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}