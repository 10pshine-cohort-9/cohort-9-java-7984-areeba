import { Link } from "react-router-dom";
import ContactTypeBadge from "./ContactTypeBadge";
import IconButton from "../common/IconButton";
import { Icons } from "../common/Icons";
import {
  getContactInitials,
  getContactType,
  getPrimaryEmail,
  getPrimaryPhone,
} from "../../utils/contactUtils";

export default function ContactTable({ contacts, onDelete }) {
  return (
    <>
      <div className="contact-table-wrap desktop-only">
        <table className="contact-table">
          <thead>
            <tr>
              <th>Contact</th>
              <th>Type</th>
              <th>Phone</th>
              <th>Email</th>
              <th className="actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {contacts.map((contact) => (
              <tr key={contact.id}>
                <td>
                  <div className="contact-cell">
                    <span className="avatar avatar-sm">{getContactInitials(contact)}</span>
                    <div>
                      <p className="contact-name">
                        {contact.firstName} {contact.lastName}
                      </p>
                      {contact.title && <p className="contact-meta">{contact.title}</p>}
                    </div>
                  </div>
                </td>
                <td>
                  <ContactTypeBadge type={getContactType(contact)} />
                </td>
                <td>{getPrimaryPhone(contact)}</td>
                <td>{getPrimaryEmail(contact)}</td>
                <td>
                  <div className="table-actions">
                    <Link
                      to={`/contacts/${contact.id}/edit`}
                      className="icon-btn icon-btn-ghost"
                      aria-label="Edit contact"
                      title="Edit contact"
                    >
                      <Icons.Pencil size={18} />
                    </Link>
                    <IconButton
                      icon={Icons.Trash2}
                      label="Delete contact"
                      variant="danger"
                      onClick={() => onDelete(contact)}
                    />
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="contact-cards mobile-only">
        {contacts.map((contact) => (
          <article key={contact.id} className="card contact-mobile-card">
            <div className="contact-cell">
              <span className="avatar avatar-sm">{getContactInitials(contact)}</span>
              <div>
                <p className="contact-name">
                  {contact.firstName} {contact.lastName}
                </p>
                {contact.title && <p className="contact-meta">{contact.title}</p>}
              </div>
            </div>
            <ContactTypeBadge type={getContactType(contact)} />
            <p className="contact-detail">
              <Icons.Phone size={14} /> {getPrimaryPhone(contact)}
            </p>
            <p className="contact-detail">
              <Icons.Mail size={14} /> {getPrimaryEmail(contact)}
            </p>
            <div className="table-actions">
              <Link to={`/contacts/${contact.id}/edit`} className="btn btn-secondary btn-sm">
                <Icons.Pencil size={16} /> Edit
              </Link>
              <button type="button" className="btn btn-danger btn-sm" onClick={() => onDelete(contact)}>
                <Icons.Trash2 size={16} /> Delete
              </button>
            </div>
          </article>
        ))}
      </div>
    </>
  );
}
