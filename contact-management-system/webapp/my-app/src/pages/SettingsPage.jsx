import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";
import Card from "../components/common/Card";
import { Icons } from "../components/common/Icons";
import { getDisplayName } from "../utils/contactUtils";

export default function SettingsPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [user, setUser] = useState(null);
  const [activeSection, setActiveSection] = useState(null);
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [profileMessage, setProfileMessage] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
  const [profileError, setProfileError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  const resetProfileForm = (profile) => {
    setEmail(profile?.email || "");
    setPhoneNumber(profile?.phoneNumber || "");
  };

  useEffect(() => {
    const loadUser = async () => {
      try {
        const response = await api.getCurrentUser();
        setUser(response);
        resetProfileForm(response);
      } catch (err) {
        setLoadError(err.message || "Failed to load settings");
      }
    };

    loadUser();
  }, []);

  const openSection = (section) => {
    setProfileMessage("");
    setPasswordMessage("");
    setProfileError("");
    setPasswordError("");
    setCurrentPassword("");
    setNewPassword("");

    if (section === "profile") {
      resetProfileForm(user);
    }

    setActiveSection((current) => (current === section ? null : section));
  };

  const handleProfileUpdate = async (event) => {
    event.preventDefault();
    setProfileMessage("");
    setProfileError("");
    setProfileLoading(true);

    const previousEmail = user?.email;

    try {
      const updated = await api.updateProfile({
        email: email.trim(),
        phoneNumber: phoneNumber.trim(),
      });
      setUser(updated);
      resetProfileForm(updated);
      setActiveSection(null);
      setProfileMessage("Profile updated successfully.");

      if (previousEmail && previousEmail !== updated.email) {
        setProfileMessage("Email updated. Please sign in again with your new email.");
        logout();
        setTimeout(() => navigate("/login"), 1500);
      }
    } catch (err) {
      setProfileError(err.message || "Failed to update profile");
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordChange = async (event) => {
    event.preventDefault();
    setPasswordMessage("");
    setPasswordError("");
    setPasswordLoading(true);

    try {
      await api.changePassword({ currentPassword, newPassword });
      setCurrentPassword("");
      setNewPassword("");
      setActiveSection(null);
      setPasswordMessage("Password changed successfully.");
    } catch (err) {
      setPasswordError(err.message || "Failed to change password");
    } finally {
      setPasswordLoading(false);
    }
  };

  if (!user && !loadError) {
    return <p className="loading-text">Loading settings...</p>;
  }

  return (
    <div className="settings-page">
      <div className="page-heading-row">
        <div>
          <h2 className="page-title">Settings</h2>
          <p className="muted">Manage your account preferences and security</p>
        </div>
      </div>

      {loadError && <p className="error">{loadError}</p>}
      {profileMessage && <p className="success settings-banner">{profileMessage}</p>}
      {passwordMessage && <p className="success settings-banner">{passwordMessage}</p>}

      <div className="profile-stack">
        <Card className="settings-option-card">
          <div className="settings-option">
            <div className="settings-option-info">
              <div className="settings-option-icon">
                <Icons.User size={18} />
              </div>
              <div>
                <h3>Edit Profile</h3>
                <p className="muted">Update your email and phone number</p>
              </div>
            </div>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => openSection("profile")}
            >
              <Icons.Pencil size={16} />
              {activeSection === "profile" ? "Close" : "Edit"}
            </button>
          </div>

          {activeSection === "profile" && (
            <form onSubmit={handleProfileUpdate} className="form settings-form">
              <label className="field">
                <span>Email</span>
                <div className="input-with-icon">
                  <Icons.Mail size={16} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </label>

              <label className="field">
                <span>Phone Number</span>
                <div className="input-with-icon">
                  <Icons.Phone size={16} />
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder="+92 300 1234567"
                  />
                </div>
              </label>

              {profileError && <p className="error">{profileError}</p>}

              <div className="profile-edit-actions">
                <button type="button" className="btn btn-secondary" onClick={() => openSection("profile")}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={profileLoading}>
                  {profileLoading ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          )}
        </Card>

        <Card className="settings-option-card">
          <div className="settings-option">
            <div className="settings-option-info">
              <div className="settings-option-icon">
                <Icons.Settings size={18} />
              </div>
              <div>
                <h3>Change Password</h3>
                <p className="muted">Signed in as {getDisplayName(user)}</p>
              </div>
            </div>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => openSection("password")}
            >
              <Icons.Pencil size={16} />
              {activeSection === "password" ? "Close" : "Update"}
            </button>
          </div>

          {activeSection === "password" && (
            <form onSubmit={handlePasswordChange} className="form settings-form">
              <label className="field">
                <span>Current Password</span>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                />
              </label>
              <label className="field">
                <span>New Password</span>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  minLength={8}
                />
              </label>

              {passwordError && <p className="error">{passwordError}</p>}

              <div className="profile-edit-actions">
                <button type="button" className="btn btn-secondary" onClick={() => openSection("password")}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={passwordLoading}>
                  {passwordLoading ? "Updating..." : "Update Password"}
                </button>
              </div>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
