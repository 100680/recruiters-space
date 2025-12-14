
import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../CartContext';
import ProductSearch from './ProductSearch';
import { useAuth } from '../utils/authUtils';

import ebuyLogo from '../assets/ebuy.png';
import cartImg from '../assets/images/cart.png';

const Header = ({ title, onSearch }) => {
  const { cartCount } = useCart();
  const { logout } = useAuth();
  const handleSearch = (term, category) => {
    if (onSearch) onSearch(term, category);
  };
  const handleSignOut = (e) => {
    e.preventDefault();
    logout();
  };
  return (
    <header className="amazon-header" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <div style={{ display: 'flex', alignItems: 'center', flex: 1 }}>
        <Link to="/" style={{ display: 'flex', alignItems: 'center', marginRight: '2rem', textDecoration: 'none' }}>
          <img src={ebuyLogo} alt="eBuy Logo" style={{ height: '40px', width: 'auto', display: 'block' }} />
        </Link>
      </div>
      <div style={{ flex: 2, display: 'flex', justifyContent: 'center' }}>
        <ProductSearch onSearch={handleSearch} />
      </div>
      <div style={{ display: 'flex', alignItems: 'center', marginRight: '2rem' }}>
        <Link to="/order" style={{ color: 'black', textDecoration: 'none', fontWeight: 'bold', marginRight: '1.5rem', fontSize: '1.1rem', background: '#43d4c0', padding: '0.5rem 1rem', cursor: 'pointer' }}>
          Orders
        </Link>
        <Link to="/signin" style={{ background: '#43d4c0', color: 'black', padding: '0.5rem 1rem', fontWeight: 'bold', marginRight: '1rem', cursor: 'pointer', textDecoration: 'none' }}>
          Sign in
        </Link>
        <button
          style={{ background: '#43d4c0', color: 'black', padding: '0.5rem 1rem', fontWeight: 'bold', marginRight: '1rem', cursor: 'pointer', border: 'none', borderRadius: '4px' }}
          onClick={handleSignOut}
        >
          Sign out
        </button>
        <div style={{ position: 'relative' }}>
          <Link to="/cart" style={{ textDecoration: 'none', color: '#fff', display: 'inline-block', position: 'relative' }}>
            <img src={cartImg} alt="Cart" style={{ width: 38, height: 38, display: 'block', filter: 'invert(1) brightness(2)' }} />
            {cartCount > 0 && (
              <span style={{ position: 'absolute', top: '-8px', right: '-8px', background: '#43d4c0', color: '#131921', borderRadius: '50%', padding: '2px 7px', fontSize: '0.9rem', fontWeight: 'bold', border: '1px solid #fff' }}>{cartCount}</span>
            )}
          </Link>
        </div>
      </div>
    </header>
  );
};

export default Header;
