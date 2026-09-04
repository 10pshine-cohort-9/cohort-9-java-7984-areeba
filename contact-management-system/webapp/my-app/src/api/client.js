function resolveApiBaseUrl(rawUrl) {
  const url = (rawUrl ?? "").trim();
  if (!url) {
    return "";
  }

  let parsed;
  try {
    parsed = new URL(url);
  } catch {
    throw new Error("VITE_API_URL must be a valid absolute URL or left empty for relative requests.");
  }

  if (parsed.protocol === "https:") {
    return url.replace(/\/$/, "");
  }

  const isLocalhost =
    parsed.protocol === "http:" &&
    (parsed.hostname === "localhost" || parsed.hostname === "127.0.0.1");

  if (isLocalhost && import.meta.env.DEV) {
    return url.replace(/\/$/, "");
  }

  throw new Error(
    "VITE_API_URL must be empty, use https://, or http://localhost during local development only."
  );
}

const API_BASE_URL = resolveApiBaseUrl(import.meta.env.VITE_API_URL);

function assertSecureAuthenticatedTransport(hasToken) {
  if (!hasToken || API_BASE_URL || typeof window === "undefined") {
    return;
  }

  const isHttps = window.location.protocol === "https:";
  const isLocalDevHttp =
    import.meta.env.DEV &&
    window.location.protocol === "http:" &&
    (window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1");

  if (!isHttps && !isLocalDevHttp) {
    throw new Error(
      "Authenticated API requests require HTTPS. Set VITE_API_URL to an https:// backend or serve the app over HTTPS."
    );
  }
}

let authToken = localStorage.getItem("token") || null;
let onAuthCleared = null;

export function setAuthClearHandler(handler) {
  onAuthCleared = handler;
}

export function getToken() {
  return authToken;
}

export function setToken(token) {
  authToken = token || null;

  if (authToken) {
    localStorage.setItem("token", authToken);
  } else {
    localStorage.removeItem("token");
  }
}

export function clearAuth() {
  authToken = null;
  localStorage.removeItem("token");
  onAuthCleared?.();
}

function parseResponseBody(text) {
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function getErrorMessage(data, fallback) {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data.message === "string" && data.message.trim()) {
    return data.message;
  }

  return fallback;
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) {
    assertSecureAuthenticatedTransport(true);
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
  const data = parseResponseBody(text);

  if (!response.ok) {
    throw new Error(getErrorMessage(data, "Request failed"));
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
  listAllContacts: async (sort = "lastName,asc", pageSize = 100) => {
    const firstPage = await request(`/api/contacts?page=0&size=${pageSize}&sort=${sort}`);
    const contacts = [...firstPage.content];

    for (let page = 1; page < firstPage.totalPages; page++) {
      const nextPage = await request(`/api/contacts?page=${page}&size=${pageSize}&sort=${sort}`);
      contacts.push(...nextPage.content);
    }

    return { contacts, totalElements: firstPage.totalElements };
  },
  searchContacts: (firstName, lastName, page = 0, size = 10, sort = "lastName,asc") => {
    const params = new URLSearchParams({ page, size, sort });
    if (firstName) params.set("firstName", firstName);
    if (lastName) params.set("lastName", lastName);
    return request(`/api/contacts/search?${params}`);
  },
  getContact: (id) => request(`/api/contacts/${id}`),
  createContact: (body) => request("/api/contacts", { method: "POST", body: JSON.stringify(body) }),
  updateContact: (id, body) =>
    request(`/api/contacts/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  deleteContact: (id) => request(`/api/contacts/${id}`, { method: "DELETE" }),
  exportContacts: async () => {
    const token = getToken();
    const headers = {};
    if (token) {
      assertSecureAuthenticatedTransport(true);
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/contacts/export`, { headers });

    if (response.status === 401 && token) {
      clearAuth();
      throw new Error("Session expired. Please login again.");
    }

    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Failed to export contacts");
    }

    return response.blob();
  },
  importContacts: async (file) => {
    const token = getToken();
    const formData = new FormData();
    formData.append("file", file);

    const headers = {};
    if (token) {
      assertSecureAuthenticatedTransport(true);
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/contacts/import`, {
      method: "POST",
      headers,
      body: formData,
    });

    if (response.status === 401 && token) {
      clearAuth();
      throw new Error("Session expired. Please login again.");
    }

    const text = await response.text();
    const data = parseResponseBody(text);

    if (!response.ok) {
      throw new Error(getErrorMessage(data, "Failed to import contacts"));
    }

    return data;
  },
};
