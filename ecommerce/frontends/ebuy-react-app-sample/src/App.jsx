import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
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
import React from 'react';

// Protected Route Component (no Keycloak)
const ProtectedRoute = ({ children }) => {
  return children;
};

// Public Route Component (no Keycloak)
const PublicRoute = ({ children }) => {
  return children;
};

function App() {
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