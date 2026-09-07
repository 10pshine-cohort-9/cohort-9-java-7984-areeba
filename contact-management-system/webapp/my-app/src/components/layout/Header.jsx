import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { api } from "../../api/client";
import { getDisplayName } from "../../utils/contactUtils";
import IconButton from "../common/IconButton";
import { Icons } from "../common/Icons";

function useIsMobile(breakpoint = 768) {
  const [isMobile, setIsMobile] = useState(() => window.innerWidth <= breakpoint);

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth <= breakpoint);
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, [breakpoint]);

  return isMobile;
}

export default function Header({ onMenuClick }) {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const isMobile = useIsMobile();

  useEffect(() => {
    api.getCurrentUser().then(setUser).catch(() => setUser(null));
  }, []);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="app-header glass-panel">
      <div className="header-left">
        {isMobile && (
          <IconButton icon={Icons.Menu} label="Open menu" onClick={onMenuClick} className="menu-btn" />
        )}
        <div>
          <p className="header-eyebrow">Welcome back</p>
          <h1 className="header-title">{getDisplayName(user)}</h1>
        </div>
      </div>

      <div className="header-actions">
        <IconButton
          icon={Icons.Search}
          label="Search contacts"
          onClick={() => navigate("/contacts")}
        />
        <button type="button" className="profile-chip" onClick={() => navigate("/profile")}>
          <span className="avatar">{getDisplayName(user).slice(0, 2).toUpperCase()}</span>
          <span className="profile-email">{user?.email || "Profile"}</span>
        </button>
        <IconButton icon={Icons.LogOut} label="Logout" onClick={handleLogout} />
      </div>
    </header>
  );
}
