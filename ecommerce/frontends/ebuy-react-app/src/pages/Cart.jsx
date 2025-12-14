
import React from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import './AmazonTheme.css';
import { useCart } from '../CartContext';




const Cart = () => {
  const { cartItems, setCartItems } = useCart();


  const handleQuantityChange = (id, delta) => {
    setCartItems(items =>
      items.map(item =>
        (item.id || item.productId) === id
          ? { ...item, quantity: Math.max(1, item.quantity + delta) }
          : item
      )
    );
  };


  const total = cartItems.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 1), 0);

  const navigate = useNavigate();

  return (
    <>
      <Header/>
      <div className="amazon-main" style={{ maxWidth: 800, margin: '2rem auto' }}>
        <h2 style={{ color: '#232f3e', marginBottom: '1.5rem' }}>Your Shopping Cart</h2>
  {(!cartItems || cartItems.length === 0) ? (
          <div style={{ textAlign: 'center', color: '#555' }}>Your cart is empty.</div>
        ) : (
          <div style={{ background: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', padding: '2rem' }}>
            {cartItems.map(item => (
              <div key={item.id || item.productId} style={{ display: 'flex', alignItems: 'center', borderBottom: '1px solid #eee', padding: '1rem 0' }}>
                <img src={item.image} alt={item.name} style={{ width: 80, borderRadius: '6px', marginRight: '1.5rem' }} />
                <div style={{ flex: 1 }}>
                  <h3 style={{ margin: 0, fontSize: '1.1rem', color: '#232f3e' }}>{item.name}</h3>
                  <p style={{ color: '#B12704', fontWeight: 'bold', margin: '0.5rem 0' }}>₹{item.price ? item.price.toLocaleString() : ''}</p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <button onClick={() => handleQuantityChange(item.id || item.productId, -1)} style={{ background: '#60cb76', border: 'none', borderRadius: '50%', width: 32, height: 32, fontWeight: 'bold', fontSize: '1.2rem', cursor: 'pointer' }}>-</button>
                    <span style={{ fontWeight: 'bold', fontSize: '1.1rem', minWidth: 32, textAlign: 'center' }}>{item.quantity}</span>
                    <button onClick={() => handleQuantityChange(item.id || item.productId, 1)} style={{ background: '#60cb76', border: 'none', borderRadius: '50%', width: 32, height: 32, fontWeight: 'bold', fontSize: '1.2rem', cursor: 'pointer' }}>+</button>
                  </div>
                </div>
                <div style={{ fontWeight: 'bold', color: '#232f3e', fontSize: '1.1rem', minWidth: 100, textAlign: 'right' }}>₹{((item.price || 0) * (item.quantity || 1)).toLocaleString()}</div>
              </div>
            ))}
            <div style={{ textAlign: 'right', marginTop: '2rem', fontWeight: 'bold', fontSize: '1.2rem', color: '#232f3e' }}>
              Subtotal ({cartItems.reduce((sum, item) => sum + item.quantity, 0)} items): ₹{total.toLocaleString()}
            </div>
            <button
              style={{ background: '#FFA41C', border: 'none', padding: '0.8rem 2rem', borderRadius: '4px', cursor: 'pointer', color: '#fff', fontWeight: 'bold', fontSize: '1.1rem', marginTop: '2rem', float: 'right' }}
              onClick={() => navigate('/checkout')}
            >
              Proceed to Checkout
            </button>
          </div>
        )}
      </div>
    </>
  );
};

export default Cart;
