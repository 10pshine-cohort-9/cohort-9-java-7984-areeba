const API_BASE_URL = import.meta.env.VITE_API_URL ?? "";

function getToken() {
  return localStorage.getItem("token");
}

export function setToken(token) {
  if (token) {
    localStorage.setItem("token", token);
  } else {
    localStorage.removeItem("token");
  }
}

export function clearAuth() {
  localStorage.removeItem("token");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && token) {
    clearAuth();
    throw new Error("Session expired. Please login again.");
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = typeof data === "string" ? data : data?.message || "Request failed";
    throw new Error(message);
  }

  return data;
}

export const api = {
  register: (body) => request("/api/auth/register", { method: "POST", body: JSON.stringify(body) }),
  login: (body) => request("/api/auth/login", { method: "POST", body: JSON.stringify(body) }),
  getCurrentUser: () => request("/api/users/me"),
  updateProfile: (body) =>
    request("/api/users/me", { method: "PUT", body: JSON.stringify(body) }),
  changePassword: (body) =>
    request("/api/users/me/password", { method: "PUT", body: JSON.stringify(body) }),
  listContacts: (page = 0, size = 10, sort = "lastName,asc") =>
    request(`/api/contacts?page=${page}&size=${size}&sort=${sort}`),
  searchContacts: (firstName, lastName, page = 0, size = 10) => {
    const params = new URLSearchParams({ page, size });
    if (firstName) params.set("firstName", firstName);
    if (lastName) params.set("lastName", lastName);
    return request(`/api/contacts/search?${params}`);
  },
  getContact: (id) => request(`/api/contacts/${id}`),
  createContact: (body) => request("/api/contacts", { method: "POST", body: JSON.stringify(body) }),
  updateContact: (id, body) =>
    request(`/api/contacts/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  deleteContact: (id) => request(`/api/contacts/${id}`, { method: "DELETE" }),
};
