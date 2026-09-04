import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import WelcomeSection from "../components/dashboard/WelcomeSection";
import KpiCard from "../components/dashboard/KpiCard";
import Card from "../components/common/Card";
import { Icons } from "../components/common/Icons";
import ContactTypeBadge from "../components/contacts/ContactTypeBadge";
import {
  countContactsWithEmail,
  countContactsWithMultipleDetails,
  countContactsWithPhone,
  getContactType,
  getPrimaryEmail,
} from "../utils/contactUtils";

export default function DashboardPage() {
  const [user, setUser] = useState(null);
  const [contacts, setContacts] = useState([]);
  const [totalContacts, setTotalContacts] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDashboard = async () => {
      setLoading(true);
      setError("");

      try {
        const [userResponse, contactsResponse] = await Promise.all([
          api.getCurrentUser(),
          api.listContacts(0, 100, "lastName,asc"),
        ]);

        setUser(userResponse);
        setContacts(contactsResponse.content);
        setTotalContacts(contactsResponse.totalElements);
      } catch (err) {
        setError(err.message || "Failed to load dashboard");
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, []);

  if (loading) {
    return <p className="loading-text">Loading dashboard...</p>;
  }

  return (
    <div className="dashboard-page">
      <WelcomeSection user={user} />

      {error && <p className="error">{error}</p>}

      <div className="kpi-grid">
        <KpiCard icon={Icons.Users} value={totalContacts} label="Total Contacts" trend="Your contact directory" />
        <KpiCard
          icon={Icons.Mail}
          value={countContactsWithEmail(contacts)}
          label="With Email"
          trend="Contacts with email addresses"
        />
        <KpiCard
          icon={Icons.Phone}
          value={countContactsWithPhone(contacts)}
          label="With Phone"
          trend="Contacts with phone numbers"
        />
        <KpiCard
          icon={Icons.ContactRound}
          value={countContactsWithMultipleDetails(contacts)}
          label="Rich Profiles"
          trend="Multiple emails or phones"
        />
      </div>

      <Card className="recent-card">
        <div className="section-header">
          <div>
            <h3>Recent Contacts</h3>
            <p className="muted">Your latest saved contacts</p>
          </div>
          <Link to="/contacts/new" className="btn btn-primary">
            <Icons.Plus size={18} /> Add Contact
          </Link>
        </div>

        {contacts.length === 0 ? (
          <p className="empty-state">No contacts yet. Create your first contact to get started.</p>
        ) : (
          <div className="recent-list">
            {contacts.slice(0, 5).map((contact) => (
              <div key={contact.id} className="recent-item">
                <div>
                  <p className="contact-name">
                    {contact.firstName} {contact.lastName}
                  </p>
                  <p className="contact-meta">{getPrimaryEmail(contact)}</p>
                </div>
                <ContactTypeBadge type={getContactType(contact)} />
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
