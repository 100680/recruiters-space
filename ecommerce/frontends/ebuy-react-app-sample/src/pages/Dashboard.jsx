import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../CartContext';
import Products from './Products';
import Header from '../components/Header';
import './AmazonTheme.css';
import Landing from './Landing';

const Dashboard = () => {
  const { cartCount, setCartCount } = useCart();
  const navigate = useNavigate();

  const handleAddToCart = () => setCartCount(cartCount + 1);

  return (
    <div className="amazon-dashboard">
      <Header/>
      <main className="amazon-main">
      <section>
        <Landing />
      </section>
    </main>
  </div>
  );
};

export default Dashboard;
