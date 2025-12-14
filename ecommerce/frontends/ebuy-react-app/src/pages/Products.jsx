

import React from 'react';
import { useCart } from '../CartContext';
import Header from '../components/Header';
import './AmazonTheme.css';
import { useLocation, useNavigate } from 'react-router-dom';


const defaultProducts = [
 
];


const Products = () => {
  const { addToCart } = useCart();
  const location = useLocation();
  const navigate = useNavigate();
  const products = (location.state && location.state.products && Array.isArray(location.state.products) && location.state.products.length > 0)
    ? location.state.products.map(p => ({
        ...p,
        image: p.imageUrl ? (p.imageUrl.startsWith('http') ? p.imageUrl : `/src/assets/images/${p.imageUrl}`) : '',
        rating: p.rating || 4.5 // fallback if not present
      }))
    : defaultProducts;


  // For demo, hardcode userId and sessionId. In real app, get from auth/session.
  const userId = 2;
  const sessionId = '43df2afd-15d3-46ea-81be-646f8e010202';

  const handleAddToCart = async (product) => {
    const payload = {
      productId: product.productId,
      quantity: 1, // Default to 1 for now
      userId,
      sessionId
    };
    try {
      const res = await fetch('http://localhost:8083/api/v1/cart/items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error('Failed to add to cart');
      const data = await res.json();
      // Assume API returns the added cart item or full cart
      addToCart({ ...product, productId: payload.productId, quantity: payload.quantity });
      alert('Added to cart!');
    } catch (err) {
      alert('Error adding to cart');
    }
  };

  return (
    <>
      <Header />
      <div>
        <section className="amazon-section">
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '2rem',
            justifyItems: 'center',
          }}>
            {products.map(product => (
              <div key={product.id || product.productId} style={{ width: '180px', border: '1px solid #eee', borderRadius: '8px', padding: '1rem', background: '#fafafa' }}>
                <img
                  src={product.image}
                  alt={product.name}
                  style={{ width: '100%', borderRadius: '4px', cursor: 'pointer' }}
                  onClick={() => navigate(`/product/${product.id || product.productId}`, { state: { product } })}
                />
                <h3 style={{ fontSize: '1rem', margin: '0.5rem 0' }}>{product.name}</h3>
                <div style={{ marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                  {[...Array(5)].map((_, i) => (
                    <span
                      key={i}
                      style={{
                        color: i < Math.round(product.rating) ? '#FFA41C' : '#ddd',
                        fontWeight: 'bold',
                        fontSize: '1.2rem',
                      }}
                    >★</span>
                  ))}
                  <span style={{ fontWeight: 'bold', color: '#232f3e', fontSize: '1.1rem', marginLeft: '0.5rem' }}>{product.rating} / 5</span>
                </div>
                <p style={{ color: '#B12704', fontWeight: 'bold' }}>₹{product.price ? product.price.toLocaleString() : ''}</p>

                <button
                  style={{ background: '#43d4c0', border: 'none', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer' }}
                  onClick={() => handleAddToCart(product)}
                >
                  Add to Cart
                </button>
              </div>
            ))}
          </div>
        </section>
      </div>
    </>
  );
};

export default Products;
