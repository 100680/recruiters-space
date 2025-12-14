import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../CartContext';
import Header from '../components/Header';
import './AmazonTheme.css';

const Checkout = () => {
  const { cartCount } = useCart();
  return (
    <div className="amazon-checkout">
      <Header/>
      <main className="amazon-main">
      <section className="amazon-section">
        <h2>Shipping Address</h2>
        {/* Add address form here */}
      </section>
      <section className="amazon-section">
        <h2>Order Details</h2>
        {/* Add order details here */}
      </section>
    </main>
  </div>
  );
};

export default Checkout;
