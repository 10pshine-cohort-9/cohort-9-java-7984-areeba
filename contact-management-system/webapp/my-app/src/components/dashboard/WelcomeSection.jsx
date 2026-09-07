import { getDisplayName, getGreeting } from "../../utils/contactUtils";

export default function WelcomeSection({ user }) {
  return (
    <section className="welcome-section">
      <div>
        <p className="welcome-eyebrow">{getGreeting()}</p>
        <h2 className="welcome-title">{getDisplayName(user)}</h2>
        <p className="welcome-subtitle">Manage your contacts efficiently from one place.</p>
      </div>
    </section>
  );
}
