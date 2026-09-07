import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import ContactForm, { createEmptyContact } from "../components/contacts/ContactForm";

export default function EditContactPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [initialValues, setInitialValues] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setInitialValues(null);
    setError("");

    const loadContact = async () => {
      try {
        const contact = await api.getContact(id);
        if (cancelled) return;

        setInitialValues({
          firstName: contact.firstName,
          lastName: contact.lastName,
          title: contact.title || "",
          emails: contact.emails?.length ? contact.emails : createEmptyContact().emails,
          phones: contact.phones?.length ? contact.phones : createEmptyContact().phones,
        });
      } catch (err) {
        if (cancelled) return;
        setError(err.message || "Failed to load contact");
      }
    };

    loadContact();

    return () => {
      cancelled = true;
    };
  }, [id]);

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (!initialValues) {
    return <p className="loading-text">Loading contact...</p>;
  }

  return (
    <ContactForm
      key={id}
      initialValues={initialValues}
      submitLabel="Update Contact"
      onSubmit={async (payload) => {
        await api.updateContact(id, payload);
        navigate("/contacts");
      }}
    />
  );
}
