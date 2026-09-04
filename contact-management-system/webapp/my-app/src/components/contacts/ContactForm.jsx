import { useEffect, useState } from "react";
import Card from "../common/Card";
import IconButton from "../common/IconButton";
import { Icons } from "../common/Icons";

const EMAIL_TYPES = ["WORK", "PERSONAL", "OTHER"];
const PHONE_TYPES = ["WORK", "HOME", "PERSONAL", "OTHER"];

const emptyEmail = () => ({ email: "", type: "WORK" });
const emptyPhone = () => ({ phoneNumber: "", type: "HOME" });

export function createEmptyContact() {
  return {
    firstName: "",
    lastName: "",
    title: "",
    emails: [emptyEmail()],
    phones: [emptyPhone()],
  };
}

export default function ContactForm({ initialValues, onSubmit, submitLabel }) {
  const [form, setForm] = useState(initialValues);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setForm(initialValues);
    setError("");
    setLoading(false);
  }, [initialValues]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateListItem = (listName, index, field, value) => {
    setForm((current) => {
      const updated = [...current[listName]];
      updated[index] = { ...updated[index], [field]: value };
      return { ...current, [listName]: updated };
    });
  };

  const addListItem = (listName, factory) => {
    setForm((current) => ({ ...current, [listName]: [...current[listName], factory()] }));
  };

  const removeListItem = (listName, index) => {
    setForm((current) => ({
      ...current,
      [listName]: current[listName].filter((_, itemIndex) => itemIndex !== index),
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const payload = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        title: form.title.trim() || null,
        emails: form.emails.filter((item) => item.email.trim()),
        phones: form.phones.filter((item) => item.phoneNumber.trim()),
      };
      await onSubmit(payload);
    } catch (err) {
      setError(err.message || "Failed to save contact");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card className="form-card">
      <form onSubmit={handleSubmit} className="form">
        <div className="page-heading">
          <h2>{submitLabel}</h2>
          <p className="muted">Fill in the contact details below.</p>
        </div>

        <section className="form-section">
          <h3>Basic Information</h3>
          <div className="form-grid">
            <label className="field">
              <span>First Name</span>
              <input
                value={form.firstName}
                onChange={(e) => updateField("firstName", e.target.value)}
                placeholder="John"
                required
              />
            </label>
            <label className="field">
              <span>Last Name</span>
              <input
                value={form.lastName}
                onChange={(e) => updateField("lastName", e.target.value)}
                placeholder="Doe"
                required
              />
            </label>
          </div>
          <label className="field">
            <span>Job Title</span>
            <input
              value={form.title}
              onChange={(e) => updateField("title", e.target.value)}
              placeholder="Software Engineer"
            />
          </label>
        </section>

        <section className="form-section">
          <div className="section-header">
            <h3>Email Addresses</h3>
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => addListItem("emails", emptyEmail)}>
              <Icons.Plus size={16} /> Add another email
            </button>
          </div>
          {form.emails.map((item, index) => (
            <div key={`email-${index}`} className="inline-row">
              <label className="field field-grow">
                <span>Email</span>
                <div className="input-with-icon">
                  <Icons.Mail size={16} />
                  <input
                    type="email"
                    value={item.email}
                    onChange={(e) => updateListItem("emails", index, "email", e.target.value)}
                    placeholder="email@example.com"
                  />
                </div>
              </label>
              <label className="field">
                <span>Type</span>
                <select
                  value={item.type}
                  onChange={(e) => updateListItem("emails", index, "type", e.target.value)}
                >
                  {EMAIL_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </label>
              {form.emails.length > 1 && (
                <IconButton
                  icon={Icons.Trash2}
                  label="Remove email"
                  variant="danger"
                  onClick={() => removeListItem("emails", index)}
                />
              )}
            </div>
          ))}
        </section>

        <section className="form-section">
          <div className="section-header">
            <h3>Phone Numbers</h3>
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => addListItem("phones", emptyPhone)}>
              <Icons.Plus size={16} /> Add another phone
            </button>
          </div>
          {form.phones.map((item, index) => (
            <div key={`phone-${index}`} className="inline-row">
              <label className="field field-grow">
                <span>Phone</span>
                <div className="input-with-icon">
                  <Icons.Phone size={16} />
                  <input
                    value={item.phoneNumber}
                    onChange={(e) => updateListItem("phones", index, "phoneNumber", e.target.value)}
                    placeholder="+92 300 1234567"
                  />
                </div>
              </label>
              <label className="field">
                <span>Type</span>
                <select
                  value={item.type}
                  onChange={(e) => updateListItem("phones", index, "type", e.target.value)}
                >
                  {PHONE_TYPES.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </label>
              {form.phones.length > 1 && (
                <IconButton
                  icon={Icons.Trash2}
                  label="Remove phone"
                  variant="danger"
                  onClick={() => removeListItem("phones", index)}
                />
              )}
            </div>
          ))}
        </section>

        {error && <p className="error">{error}</p>}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? "Saving..." : submitLabel}
          </button>
        </div>
      </form>
    </Card>
  );
}
