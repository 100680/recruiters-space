import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../CartContext';
import Header from '../components/Header';
import './AmazonTheme.css';

const Payment = () => {
  const { cartCount } = useCart();
  return (
    <div className="amazon-payment">
      <Header/>
      <main className="amazon-main">
      <section className="amazon-section">
        <h2>Payment Options</h2>
        {/* Add payment form here */}
      </section>
    </main>
  </div>
  );
};

export default Payment;
