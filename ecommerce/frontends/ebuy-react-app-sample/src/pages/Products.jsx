

import React from 'react';
import { useCart } from '../CartContext';
import Header from '../components/Header';
import './AmazonTheme.css';
import './Products.css';
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
        image: p.imageUrl ? (p.imageUrl.startsWith('http') ? p.imageUrl : `./assets/images/${p.imageUrl}`) : '',
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
      // const res = await fetch('http://localhost:8083/api/v1/cart/items', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify(payload)
      // });
      // if (!res.ok) throw new Error('Failed to add to cart');
      // const data = await res.json();
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
        <section className="amazon-section products-section">
          <div className="products-grid">
            {products.map(product => (
              <div key={product.id || product.productId} className="product-card">
                <img
                  src={product.image}
                  alt={product.name}
                  onClick={() => navigate(`/product/${product.id || product.productId}`, { state: { product } })}
                />
                <h3 className="product-title">{product.name}</h3>
                <div className="product-rating">
                  {[...Array(5)].map((_, i) => (
                    <span
                      key={i}
                      style={{
                        color: i < Math.round(product.rating) ? '#FFA41C' : '#ddd',
                        fontWeight: 'bold',
                        fontSize: '1.05rem',
                      }}
                    >★</span>
                  ))}
                  <span style={{ fontWeight: 'bold', color: '#232f3e', fontSize: '1.05rem', marginLeft: '0.4rem' }}>{product.rating} / 5</span>
                </div>
                <p className="product-price">₹{product.price ? product.price.toLocaleString() : ''}</p>

                <button className="product-add-btn" onClick={() => handleAddToCart(product)}>
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
