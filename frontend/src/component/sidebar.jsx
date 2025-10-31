import { NavLink, useLocation } from "react-router-dom";
import "primeicons/primeicons.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import "../assets/css/sidebar.css";

export default function SideBar() {
  const location = useLocation();
  const linkClass = ({ isActive }) =>
    "sidebar-link" + (isActive ? " active" : "");

  return (
    <aside className="sidebar">
      {/* กลุ่ม icon ตรงกลาง */}
      <nav className="sidebar-icons">
        <NavLink
          to="/dashboard"
          className={
            location.pathname === "/dashboard"
              ? "sidebar-link active"
              : "sidebar-link"
          }
          data-tooltip="Dashboard"
        >
          <i className="pi pi-home icon-lg" />
        </NavLink>

        <NavLink
          to="/tenantmanagement"
          className={linkClass}
          data-tooltip="Tenant Management"
        >
          <i className="pi pi-user icon-lg" />
        </NavLink>

        <NavLink
          to="/roommanagement"
          className={linkClass}
          data-tooltip="Room Management"
        >
          <i className="bi bi-building icon-lg" />
        </NavLink>

        <NavLink
          to="/maintenancerequest"
          className={linkClass}
          data-tooltip="Maintenance Request"
        >
          <i className="pi pi-wrench icon-lg" />
        </NavLink>

        <NavLink
          to="/assetmanagement"
          className={linkClass}
          data-tooltip="Asset Management"
        >
          <i className="bi bi-box-seam icon-lg" />
        </NavLink>

        <NavLink
          to="/invoicemanagement"
          className={linkClass}
          data-tooltip="Invoice Management"
        >
          <i className="bi bi-currency-dollar icon-lg" />
        </NavLink>

        <NavLink
          to="/maintenanceschedule"
          className={linkClass}
          data-tooltip="Maintenance Schedule"
        >
          <i className="bi bi-alarm icon-lg" />
        </NavLink>

        <NavLink
          to="/packagemanagement"
          className={linkClass}
          data-tooltip="Package Management"
        >
          <i className="bi bi-sticky icon-lg" />
        </NavLink>
      </nav>

      {/* ปุ่ม logout อยู่ล่างสุด */}
      <button className="sidebar-logout">
        <i className="pi pi-sign-out icon-lg" />
        <span className="logout-text">Logout</span>
      </button>
    </aside>
  );
}