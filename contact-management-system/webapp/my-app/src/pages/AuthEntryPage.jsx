import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth, RegistrationLoginError } from "../context/AuthContext";
import { Icons } from "../components/common/Icons";

export default function AuthEntryPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [view, setView] = useState("landing");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (location.pathname === "/register") {
      setView("register");
    }
  }, [location.pathname]);

  const openView = (nextView) => {
    setError("");
    setSuccessMessage("");
    setView(nextView);
    navigate(nextView === "register" ? "/register" : "/login", { replace: true });
  };

  const backToLanding = () => {
    setError("");
    setSuccessMessage("");
    setEmail("");
    setPassword("");
    setView("landing");
    navigate("/login", { replace: true });
  };

  const handleLogin = async (event) => {
    event.preventDefault();
    setError("");
    setSuccessMessage("");
    setLoading(true);

    try {
      await login(email, password);
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await register(email, password);
      navigate("/dashboard");
    } catch (err) {
      if (err instanceof RegistrationLoginError) {
        setPassword("");
        setError("");
        setView("login");
        navigate("/login", { replace: true });
        setSuccessMessage(err.message);
        return;
      }
      setError(err.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-entry">
      <div className={`auth-main-card glass-auth${view !== "landing" ? " auth-main-card--compact" : ""}`}>
        <header className="auth-hero">
          <p className="auth-welcome">Welcome to</p>
          <h1>Contact Management System</h1>
          <p className="auth-hero-description">
            Organize, manage, and search your contacts in one secure place. Built for teams
            who need a simple, professional contact directory.
          </p>
        </header>

        {view === "landing" && (
          <div className="auth-action-bar">
            <button type="button" className="btn btn-primary auth-action-btn" onClick={() => openView("login")}>
              <Icons.User size={18} />
              Login
            </button>
            <button type="button" className="btn btn-secondary auth-action-btn" onClick={() => openView("register")}>
              <Icons.Plus size={18} />
              Sign Up
            </button>
          </div>
        )}

        {view === "login" && (
          <div className="auth-form-section">
            <button type="button" className="auth-back-btn" onClick={backToLanding}>
              <Icons.ChevronLeft size={16} /> Back
            </button>
            <h2>Sign in</h2>
            <p className="muted">Enter your credentials to access your account.</p>
            <form onSubmit={handleLogin} className="form">
              <label className="field">
                <span>Email</span>
                <div className="input-with-icon">
                  <Icons.Mail size={16} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    required
                  />
                </div>
              </label>
              <label className="field">
                <span>Password</span>
                <div className="input-with-icon">
                  <Icons.Lock size={16} />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    minLength={8}
                  />
                </div>
              </label>
              {successMessage && <p className="success">{successMessage}</p>}
              {error && <p className="error">{error}</p>}
              <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                {loading ? "Signing in..." : "Sign in"}
              </button>
            </form>
            <p className="auth-footer-text">
              No account?{" "}
              <button type="button" className="auth-link-btn" onClick={() => openView("register")}>
                Create one
              </button>
            </p>
          </div>
        )}

        {view === "register" && (
          <div className="auth-form-section">
            <button type="button" className="auth-back-btn" onClick={backToLanding}>
              <Icons.ChevronLeft size={16} /> Back
            </button>
            <h2>Create account</h2>
            <p className="muted">Start managing your contacts in minutes.</p>
            <form onSubmit={handleRegister} className="form">
              <label className="field">
                <span>Email</span>
                <div className="input-with-icon">
                  <Icons.Mail size={16} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    required
                  />
                </div>
              </label>
              <label className="field">
                <span>Password</span>
                <div className="input-with-icon">
                  <Icons.Lock size={16} />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Minimum 8 characters"
                    required
                    minLength={8}
                  />
                </div>
              </label>
              {error && <p className="error">{error}</p>}
              <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                {loading ? "Creating account..." : "Create account"}
              </button>
            </form>
            <p className="auth-footer-text">
              Already have an account?{" "}
              <button type="button" className="auth-link-btn" onClick={() => openView("login")}>
                Sign in
              </button>
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
