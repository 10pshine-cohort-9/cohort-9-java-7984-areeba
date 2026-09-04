import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import Card from "../components/common/Card";
import Modal from "../components/common/Modal";
import ContactTable from "../components/contacts/ContactTable";
import { Icons } from "../components/common/Icons";
import { getContactType } from "../utils/contactUtils";

const TYPE_FILTERS = [
  { value: "all", label: "All Types" },
  { value: "business", label: "Business" },
  { value: "personal", label: "Personal" },
  { value: "other", label: "Other" },
];

const SORT_OPTIONS = [
  { value: "lastName,asc", label: "Name (A–Z)" },
  { value: "lastName,desc", label: "Name (Z–A)" },
  { value: "firstName,asc", label: "First Name (A–Z)" },
];

export default function ContactsPage() {
  const [contacts, setContacts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [typeFilter, setTypeFilter] = useState("all");
  const [sort, setSort] = useState("lastName,asc");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const loadContacts = async (pageNumber = page) => {
    setLoading(true);
    setError("");

    try {
      const hasSearch = firstName.trim() || lastName.trim();
      const response = hasSearch
        ? await api.searchContacts(firstName.trim(), lastName.trim(), pageNumber, 10, sort)
        : await api.listContacts(pageNumber, 10, sort);

      setContacts(response.content);
      setTotalPages(response.totalPages);
      setPage(response.page);
    } catch (err) {
      setError(err.message || "Failed to load contacts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContacts(0);
  }, [sort]);

  const filteredContacts = useMemo(() => {
    if (typeFilter === "all") return contacts;
    return contacts.filter((contact) => getContactType(contact) === typeFilter);
  }, [contacts, typeFilter]);

  const handleSearch = (event) => {
    event.preventDefault();
    loadContacts(0);
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;

    setDeleting(true);
    try {
      await api.deleteContact(deleteTarget.id);
      setDeleteTarget(null);
      await loadContacts(page);
    } catch (err) {
      setError(err.message || "Failed to delete contact");
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="contacts-page">
      <div className="page-heading-row">
        <div>
          <h2 className="page-title">Contacts</h2>
          <p className="muted">Manage and organize your contacts</p>
        </div>
        <Link to="/contacts/new" className="btn btn-primary">
          <Icons.Plus size={18} /> Add Contact
        </Link>
      </div>

      <Card className="search-card">
        <form onSubmit={handleSearch} className="search-toolbar">
          <div className="search-input-wrap">
            <Icons.Search size={18} />
            <input
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="Search by first name..."
            />
          </div>
          <div className="search-input-wrap">
            <Icons.Search size={18} />
            <input
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Search by last name..."
            />
          </div>
          <div className="filter-group">
            <Icons.Filter size={16} />
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
              {TYPE_FILTERS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <Icons.ArrowUpDown size={16} />
            <select value={sort} onChange={(e) => setSort(e.target.value)}>
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-secondary">
            Search
          </button>
        </form>
      </Card>

      {loading && <p className="loading-text">Loading contacts...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && filteredContacts.length === 0 && (
        <Card className="empty-state-card">
          <p>No contacts found.</p>
        </Card>
      )}

      {!loading && filteredContacts.length > 0 && (
        <Card className="table-card">
          <ContactTable contacts={filteredContacts} onDelete={setDeleteTarget} />
        </Card>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button type="button" className="btn btn-secondary" disabled={page === 0} onClick={() => loadContacts(page - 1)}>
            <Icons.ChevronLeft size={16} /> Previous
          </button>
          <span className="pagination-label">
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary"
            disabled={page + 1 >= totalPages}
            onClick={() => loadContacts(page + 1)}
          >
            Next <Icons.ChevronRight size={16} />
          </button>
        </div>
      )}

      <Modal
        open={Boolean(deleteTarget)}
        title="Delete Contact"
        onClose={() => setDeleteTarget(null)}
        footer={
          <>
            <button type="button" className="btn btn-secondary" onClick={() => setDeleteTarget(null)}>
              Cancel
            </button>
            <button type="button" className="btn btn-danger" onClick={handleDeleteConfirm} disabled={deleting}>
              {deleting ? "Deleting..." : "Delete"}
            </button>
          </>
        }
      >
        <p>
          Are you sure you want to delete{" "}
          <strong>
            {deleteTarget?.firstName} {deleteTarget?.lastName}
          </strong>
          ? This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
