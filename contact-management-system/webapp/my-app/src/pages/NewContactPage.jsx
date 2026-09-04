import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import ContactForm, { createEmptyContact } from "../components/contacts/ContactForm";

export default function NewContactPage() {
  const navigate = useNavigate();

  return (
    <ContactForm
      initialValues={createEmptyContact()}
      submitLabel="Create Contact"
      onSubmit={async (payload) => {
        await api.createContact(payload);
        navigate("/contacts");
      }}
    />
  );
}
