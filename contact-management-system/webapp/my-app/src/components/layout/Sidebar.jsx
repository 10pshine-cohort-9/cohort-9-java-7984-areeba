import { NavLink } from "react-router-dom";
import { Icons } from "../common/Icons";

const NAV_ITEMS = [
  { to: "/dashboard", label: "Dashboard", icon: Icons.LayoutDashboard },
  { to: "/contacts", label: "Contacts", icon: Icons.Users },
  { to: "/profile", label: "Profile", icon: Icons.User },
  { to: "/settings", label: "Settings", icon: Icons.Settings },
];

export default function Sidebar({ open, onClose }) {
  return (
    <>
      <div className={`sidebar-overlay ${open ? "visible" : ""}`} onClick={onClose} role="presentation" />
      <aside className={`sidebar ${open ? "open" : ""}`}>
        <div className="sidebar-brand">
          <div className="brand-mark">
            <Icons.ContactRound size={22} />
          </div>
          <div>
            <p className="brand-title">Contact Management</p>
            <p className="brand-subtitle">System</p>
          </div>
        </div>

        <nav className="sidebar-nav">
          {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
              onClick={onClose}
            >
              <Icon size={18} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
