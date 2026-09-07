import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api, clearAuth, setToken } from "../api/client";

const AuthContext = createContext(null);

export class RegistrationLoginError extends Error {
  constructor(message = "Account created successfully. Please sign in with your credentials.") {
    super(message);
    this.name = "RegistrationLoginError";
  }
}

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(null);

  useEffect(() => {
    localStorage.removeItem("token");
  }, []);

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
        await api.register({ email, password });

        try {
          const loginResponse = await api.login({ email, password });
          setToken(loginResponse.token);
          setTokenState(loginResponse.token);
          return loginResponse;
        } catch (loginError) {
          throw new RegistrationLoginError(
            loginError.message
              ? `Account created, but automatic sign-in failed: ${loginError.message} Please sign in.`
              : undefined
          );
        }
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
