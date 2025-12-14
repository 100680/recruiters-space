// utils/authUtils.js
import { useKeycloak } from '@react-keycloak/web';

// Custom hook for authentication
export const useAuth = () => {
  const { keycloak, initialized } = useKeycloak();

  const login = () => {
    keycloak.login();
  };

  const logout = () => {
    keycloak.logout({
      redirectUri: window.location.origin
    });
  };

  const register = () => {
    keycloak.register();
  };

  const getUserInfo = () => {
    if (keycloak.authenticated && keycloak.tokenParsed) {
      return {
        id: keycloak.tokenParsed.sub,
        username: keycloak.tokenParsed.preferred_username,
        email: keycloak.tokenParsed.email,
        firstName: keycloak.tokenParsed.given_name,
        lastName: keycloak.tokenParsed.family_name,
        fullName: keycloak.tokenParsed.name,
        roles: keycloak.tokenParsed.realm_access?.roles || [],
      };
    }
    return null;
  };

  const hasRole = (role) => {
    if (!keycloak.authenticated) return false;
    return keycloak.hasRealmRole(role);
  };

  const hasAnyRole = (roles) => {
    if (!keycloak.authenticated) return false;
    return roles.some(role => keycloak.hasRealmRole(role));
  };

  const getToken = () => {
    return keycloak.token;
  };

  const updateToken = (minValidity = 30) => {
    return keycloak.updateToken(minValidity);
  };

  const isTokenExpired = () => {
    return keycloak.isTokenExpired();
  };

  return {
    keycloak,
    initialized,
    authenticated: keycloak.authenticated,
    login,
    logout,
    register,
    getUserInfo,
    hasRole,
    hasAnyRole,
    getToken,
    updateToken,
    isTokenExpired,
  };
};

// HTTP interceptor for API calls
export const createAuthenticatedRequest = (keycloak) => {
  return async (url, options = {}) => {
    // Update token if it's about to expire
    try {
      await keycloak.updateToken(30);
    } catch (error) {
      console.error('Failed to refresh token:', error);
      keycloak.login();
      return;
    }

    // Add authorization header
    const authHeaders = {
      'Authorization': `Bearer ${keycloak.token}`,
      'Content-Type': 'application/json',
      ...options.headers,
    };

    const requestOptions = {
      ...options,
      headers: authHeaders,
    };

    return fetch(url, requestOptions);
  };
};