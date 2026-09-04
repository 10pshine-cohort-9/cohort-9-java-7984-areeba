import { useEffect } from "react";
import { Outlet } from "react-router-dom";

export default function AuthLayout() {
  useEffect(() => {
    document.body.classList.add("auth-page");
    return () => document.body.classList.remove("auth-page");
  }, []);

  return (
    <div className="auth-layout">
      <div className="auth-layout-bg" />
      <div className="auth-layout-content">
        <Outlet />
      </div>
    </div>
  );
}
