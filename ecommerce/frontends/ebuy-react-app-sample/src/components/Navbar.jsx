import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../utils/authUtils';

const Navbar = () => {
  const { authenticated, login, logout, getUserInfo } = useAuth();
  const userInfo = getUserInfo();

  const handleAuthAction = () => {
    if (authenticated) {
      logout();
    } else {
      login();
    }
  };

  return (
    <nav style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '1rem',
      backgroundColor: '#232f3e',
      color: 'white'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <Link to="/" style={{ color: 'white', textDecoration: 'none', fontSize: '1.5rem', fontWeight: 'bold' }}>
          EBuy
        </Link>
        
        <div style={{ display: 'flex', gap: '1rem' }}>
          <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>
            Home
          </Link>
          <Link to="/products" style={{ color: 'white', textDecoration: 'none' }}>
            Products
          </Link>
          {authenticated && (
            <>
              <Link to="/dashboard" style={{ color: 'white', textDecoration: 'none' }}>
                Dashboard
              </Link>
              <Link to="/cart" style={{ color: 'white', textDecoration: 'none' }}>
                Cart
              </Link>
            </>
          )}
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {authenticated ? (
          <>
            <span>Welcome, {userInfo?.firstName || userInfo?.username || 'User'}!</span>
            <button
              onClick={handleAuthAction}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#ff9900',
                border: 'none',
                borderRadius: '4px',
                color: 'white',
                cursor: 'pointer'
              }}
            >
              Logout
            </button>
          </>
        ) : (
          <button
            onClick={handleAuthAction}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#ff9900',
              border: 'none',
              borderRadius: '4px',
              color: 'white',
              cursor: 'pointer'
            }}
          >
            Sign In
          </button>
        )}
      </div>
    </nav>
  );
};

export default Navbar;