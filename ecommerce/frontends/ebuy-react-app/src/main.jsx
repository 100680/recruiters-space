import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import Keycloak from 'keycloak-js';

// Try different URL formats based on your Keycloak version:
// For Keycloak 17+: http://localhost:8080/
// For Keycloak 16 and below: http://localhost:8080/auth

const keycloak = new Keycloak({
  // Option 1: For newer Keycloak versions (17+)
  url: 'http://localhost:8080/',
  realm: 'ebuy-realm',
  clientId: 'ebuyclient',
  
  // Option 2: Alternative configuration using direct realm URL
  // url: 'http://localhost:8080',
  // realm: 'ebuy-realm',
  // clientId: 'ebuyclient',
});

// Configuration to handle third-party cookie issues
const keycloakProviderInitOptions = {
  checkLoginIframe: false,
  enableLogging: true,
  // Add these if you still have issues:
  // flow: 'standard',
  // responseMode: 'fragment',
};

// Loading component
const KeycloakLoading = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh',
    fontSize: '18px' 
  }}>
    Loading authentication...
  </div>
);

// Enhanced error handling
const handleKeycloakEvent = (event, error) => {
  console.log('Keycloak event:', event);
  if (error) {
    console.error('Keycloak error:', error);
  }
  
  // Handle specific events
  switch(event) {
    case 'onAuthSuccess':
      console.log('Authentication successful');
      break;
    case 'onAuthError':
      console.error('Authentication failed');
      break;
    case 'onAuthLogout':
      console.log('User logged out');
      break;
    default:
      break;
  }
};

const handleKeycloakTokens = (tokens) => {
  console.log('Keycloak tokens updated:', tokens);
};

createRoot(document.getElementById('root')).render(
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={keycloakProviderInitOptions}
    LoadingComponent={<KeycloakLoading />}
    onEvent={handleKeycloakEvent}
    onTokens={handleKeycloakTokens}
  >
    <App />
  </ReactKeycloakProvider>
);