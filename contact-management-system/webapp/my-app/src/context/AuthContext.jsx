import { createContext, useContext, useMemo, useState } from "react";
import { api, clearAuth, setToken } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(() => localStorage.getItem("token"));

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(token),
      login: async (email, password) => {
        const response = await api.login({ email, password });
        setToken(response.token);
        setTokenState(response.token);
        return response;
      },
      register: async (email, password) => {
        const response = await api.register({ email, password });
        const loginResponse = await api.login({ email, password });
        setToken(loginResponse.token);
        setTokenState(loginResponse.token);
        return response;
      },
      logout: () => {
        clearAuth();
        setTokenState(null);
      },
    }),
    [token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
