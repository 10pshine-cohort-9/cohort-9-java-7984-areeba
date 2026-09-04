export function getContactType(contact) {
  const emailType = contact.emails?.[0]?.type;
  const phoneType = contact.phones?.[0]?.type;

  if (emailType === "WORK" || phoneType === "WORK") return "business";
  if (emailType === "PERSONAL" || phoneType === "PERSONAL" || phoneType === "HOME") return "personal";
  return "other";
}

export function getContactInitials(contact) {
  const first = contact.firstName?.[0] || "";
  const last = contact.lastName?.[0] || "";
  return `${first}${last}`.toUpperCase() || "?";
}

export function getPrimaryEmail(contact) {
  return contact.emails?.[0]?.email || "—";
}

export function getPrimaryPhone(contact) {
  return contact.phones?.[0]?.phoneNumber || "—";
}

export function countContactsWithMultipleDetails(contacts) {
  return contacts.filter(
    (contact) => (contact.emails?.length || 0) > 1 || (contact.phones?.length || 0) > 1
  ).length;
}

export function countContactsWithEmail(contacts) {
  return contacts.filter((contact) => contact.emails?.length > 0).length;
}

export function countContactsWithPhone(contacts) {
  return contacts.filter((contact) => contact.phones?.length > 0).length;
}

export function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

export function getDisplayName(user) {
  if (!user?.email) return "there";
  const name = user.email.split("@")[0];
  return name.charAt(0).toUpperCase() + name.slice(1);
}
