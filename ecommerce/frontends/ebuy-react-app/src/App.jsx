import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Landing from './pages/Landing';
import Cart from './pages/Cart';
import ProductDetails from './pages/ProductDetails';
import SignIn from './pages/SignIn';
import { CartProvider } from './CartContext';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Checkout from './pages/Checkout';
import Order from './pages/Order';
import Payment from './pages/Payment';
import './pages/AmazonTheme.css';
import { useKeycloak } from '@react-keycloak/web';
import React from 'react';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { keycloak, initialized } = useKeycloak();
  
  if (!initialized) {
    return <div>Loading...</div>;
  }
  
  if (!keycloak.authenticated) {
    keycloak.login();
    return <div>Redirecting to login...</div>;
  }
  
  return children;
};

// Public Route Component (for routes that should redirect authenticated users)
const PublicRoute = ({ children }) => {
  const { keycloak, initialized } = useKeycloak();
  
  if (!initialized) {
    return <div>Loading...</div>;
  }
  
  if (keycloak.authenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return children;
};

function App() {
  const { keycloak, initialized } = useKeycloak();

  // Show loading while Keycloak initializes
  if (!initialized) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px' 
      }}>
        Initializing application...
      </div>
    );
  }

  return (
    <CartProvider>
      <Router>
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<Landing />} />
          <Route path="/products" element={<Products />} />
          <Route path="/product/:id" element={<ProductDetails />} />
          
          {/* Public route that redirects if authenticated */}
          <Route 
            path="/signin" 
            element={
              <PublicRoute>
                <SignIn />
              </PublicRoute>
            } 
          />
          
          {/* Protected routes */}
          <Route 
            path="/dashboard" 
            element={
              <PublicRoute>
                <Dashboard />
              </PublicRoute>
            } 
          />
          <Route 
            path="/cart" 
            element={
              <PublicRoute>
                <Cart />
              </PublicRoute>
            } 
          />
          <Route 
            path="/checkout" 
            element={
              <ProtectedRoute>
                <Checkout />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/order" 
            element={
              <ProtectedRoute>
                <Order />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/payment" 
            element={
              <ProtectedRoute>
                <Payment />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </Router>
    </CartProvider>
  );
}

export default App;