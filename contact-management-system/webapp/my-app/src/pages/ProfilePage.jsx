import { useEffect, useState } from "react";
import { api } from "../api/client";
import Card from "../components/common/Card";
import { Icons } from "../components/common/Icons";
import { getDisplayName } from "../utils/contactUtils";

export default function ProfilePage() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadUser = async () => {
      try {
        const response = await api.getCurrentUser();
        setUser(response);
      } catch (err) {
        setError(err.message || "Failed to load profile");
      }
    };

    loadUser();
  }, []);

  if (!user && !error) {
    return <p className="loading-text">Loading profile...</p>;
  }

  return (
    <div className="profile-page">
      <div className="page-heading-row">
        <div>
          <h2 className="page-title">Profile</h2>
          <p className="muted">Your account information</p>
        </div>
      </div>

      <div className="profile-stack">
        <Card className="profile-card">
          <div className="profile-header">
            <span className="avatar avatar-lg">{getDisplayName(user).slice(0, 2).toUpperCase()}</span>
            <div>
              <h3>{getDisplayName(user)}</h3>
              <p className="muted">
                Member since {user ? new Date(user.createdAt).toLocaleDateString() : "—"}
              </p>
            </div>
          </div>

          <div className="profile-details">
            <div className="profile-detail-row">
              <Icons.Mail size={16} />
              <span>{user?.email}</span>
            </div>
            <div className="profile-detail-row">
              <Icons.Phone size={16} />
              <span>{user?.phoneNumber || "Not set"}</span>
            </div>
            <div className="profile-detail-row">
              <Icons.User size={16} />
              <span>Account ID: {user?.id}</span>
            </div>
          </div>

          {error && <p className="error">{error}</p>}
        </Card>
      </div>
    </div>
  );
}
