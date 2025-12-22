import React from 'react';
import { useParams, Link } from 'react-router-dom';
import Header from '../components/Header';
import './AmazonTheme.css';

const products = [
  {
    id: 1,
    name: 'Echo Dot (4th Gen)',
    price: 3499,
    image: 'https://m.media-amazon.com/images/I/61Iz2yy2CKL._AC_UL320_.jpg',
    description: 'Smart speaker with Alexa, 4th Gen, premium sound.',
    rating: 4.5
  },
  {
    id: 2,
    name: 'Fire TV Stick',
    price: 2999,
    image: 'https://m.media-amazon.com/images/I/71qkR6yQbAL._AC_UL320_.jpg',
    description: 'Streaming device for TV, Alexa Voice Remote included.',
    rating: 4.2
  },
  {
    id: 3,
    name: 'Kindle Paperwhite',
    price: 13999,
    image: 'https://m.media-amazon.com/images/I/81pL3U9HhJL._AC_UL320_.jpg',
    description: 'Waterproof e-reader with high-resolution display.',
    rating: 4.7
  },
];

const ProductDetails = () => {
  const { id } = useParams();
  const product = products.find(p => p.id === Number(id));

  if (!product) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}>Product not found.</div>;
  }

  return (
    <>
      <Header/>
      <div className="amazon-main" style={{ display: 'flex', justifyContent: 'center', marginTop: '2rem' }}>
        <div style={{ maxWidth: 400, background: '#fff', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', padding: '2rem' }}>
          <img src={product.image} alt={product.name} style={{ width: '100%', borderRadius: '6px', marginBottom: '1rem' }} />
          <h2 style={{ color: '#232f3e', marginBottom: '0.5rem' }}>{product.name}</h2>
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
          <p style={{ color: '#B12704', fontWeight: 'bold', fontSize: '1.2rem' }}>₹{product.price.toLocaleString()}</p>
          <p style={{ margin: '1rem 0', color: '#555' }}>{product.description}</p>
          <Link to="/products" style={{ color: '#007185', textDecoration: 'underline', fontWeight: 'bold', display: 'block', marginBottom: '1rem' }}>Back to Products</Link>
          <button style={{ background: '#FFD814', border: 'none', padding: '0.7rem 1.5rem', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1rem', width: '100%' }}>Add to Cart</button>
        </div>
      </div>
    </>
  );
};

export default ProductDetails;
