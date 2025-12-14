import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../CartContext';
import Header from '../components/Header';
import './AmazonTheme.css';

const Order = () => {
  const { cartCount } = useCart();
  return (
    <div className="amazon-order">
      <Header/>
      <main className="amazon-main">
        <section className="amazon-section">
          <h2>Order Summary</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ background: '#f7f7f7', padding: '1rem', borderRadius: '6px', border: '1px solid #eee' }}>
              <strong>Order #12345</strong>
              <p>Status: <span style={{ color: '#007600' }}>Delivered</span></p>
              <p>Total: ₹3,499</p>
            </div>
            <div style={{ background: '#f7f7f7', padding: '1rem', borderRadius: '6px', border: '1px solid #eee' }}>
              <strong>Order #12346</strong>
              <p>Status: <span style={{ color: '#B12704' }}>Pending</span></p>
              <p>Total: ₹2,999</p>
            </div>
          </div>
        </section>
      </main>
  </div>
  );
};

export default Order;
