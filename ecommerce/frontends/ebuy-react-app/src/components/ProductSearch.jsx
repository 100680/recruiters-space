import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';


const ProductSearch = ({ onSearch }) => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const skipAutocompleteRef = useRef(false);
  const [category, setCategory] = useState('All');
  const [categories, setCategories] = useState([{ categoryId: 0, name: 'All' }]);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const inputRef = useRef(null);
  const suggestionsRef = useRef(null);
  // Close suggestions on outside click
  useEffect(() => {
    function handleClickOutside(event) {
      if (
        inputRef.current &&
        !inputRef.current.contains(event.target) &&
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target)
      ) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    fetch('https://ebuy-product-categories-worker.kvbalajimtech.workers.dev/')
      .then(res => res.json())
      .then(result => {
        if (result && result.success && Array.isArray(result.data)) {
          setCategories([{ categoryId: 0, name: 'All' }, ...result.data]);
          console.log('Fetched categories:', result.data);
        } else {
          console.log('Unexpected API response:', result);
        }
      })
      .catch((err) => {
        console.log('Fetch error:', err);
        // fallback: keep only 'All' if fetch fails
      });
  }, []);

  // Find selected categoryId
  const selectedCategoryObj = categories.find(cat => cat.name === category);
  const selectedCategoryId = selectedCategoryObj ? selectedCategoryObj.categoryId : null;

  // Autocomplete fetch
  useEffect(() => {
    if (skipAutocompleteRef.current) {
      skipAutocompleteRef.current = false;
      return;
    }
    if (searchTerm.length < 2) {
      setSuggestions([]);
      return;
    }
    const controller = new AbortController();
    fetch('http://localhost:8081/api/v1/products/autocomplete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: searchTerm, size: 10, categoryId: selectedCategoryId }),
      signal: controller.signal
    })
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) {
          setSuggestions(data);
          setShowSuggestions(true);
        } else {
          setSuggestions([]);
        }
      })
      .catch(() => setSuggestions([]));
    return () => controller.abort();
  }, [searchTerm, selectedCategoryId]);

  // Helper to get categoryId for search
  const getCategoryId = () => {
    const obj = categories.find(cat => cat.name === category);
    return obj ? obj.categoryId : 0;
  };

  // Fetch products and navigate to Products page
  const searchAndNavigate = (name, categoryId) => {
    fetch(`http://localhost:8082/api/products/search?name=${encodeURIComponent(name)}&categoryid=${categoryId}`)
      .then(res => res.json())
      .then(products => {
        navigate('/products', { state: { products } });
      });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setShowSuggestions(false);
    const categoryId = getCategoryId();
    searchAndNavigate(searchTerm, categoryId);
    if (onSearch) onSearch(searchTerm, category);
  };

  const handleSuggestionClick = (suggestion) => {
    skipAutocompleteRef.current = true;
    setSearchTerm(suggestion);
    setShowSuggestions(false);
    inputRef.current && inputRef.current.focus();
    const categoryId = getCategoryId();
    searchAndNavigate(suggestion, categoryId);
    if (onSearch) onSearch(suggestion, category);
  };

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        display: 'flex',
        alignItems: 'center',
        marginLeft: '2rem',
        background: '#fff',
        borderRadius: '6px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.07)',
        height: '40px',
        border: '1px solid #43d4c0',
      }}
    >
      <select
        value={category}
        onChange={e => setCategory(e.target.value)}
        style={{
          padding: '0.5rem 1rem',
          border: 'none',
          background: '#f3f3f3',
          fontWeight: 'bold',
          outline: 'none',
          height: '100%',
        }}
      >
        {categories.map(cat => (
          <option key={cat.categoryId} value={cat.name}>{cat.name}</option>
        ))}
      </select>
  <div style={{ position: 'relative', width: '220px', minWidth: '220px', height: '40px', display: 'flex', alignItems: 'center' }}>
        <input
          ref={inputRef}
          type="text"
          placeholder="Search products..."
          value={searchTerm}
          onChange={e => {
            setSearchTerm(e.target.value);
            setShowSuggestions(true);
          }}
          style={{
            padding: '0.5rem 1rem',
            border: 'none',
            width: '100%',
            outline: 'none',
            height: '32px',
            boxSizing: 'border-box',
            fontSize: '1rem',
            verticalAlign: 'middle',
            display: 'block',
          }}
          autoComplete="off"
        />
        {showSuggestions && suggestions.length > 0 && (
          <ul
            ref={suggestionsRef}
            style={{
              position: 'absolute',
              top: '40px',
              left: 0,
              right: 0,
              background: '#fff',
              border: '1px solid #ddd',
              borderTop: 'none',
              borderRadius: '0 0 8px 8px',
              boxShadow: '0 8px 32px rgba(0,0,0,0.18)',
              zIndex: 9999,
              margin: 0,
              padding: 0,
              listStyle: 'none',
              maxHeight: '260px',
              minWidth: '220px',
              overflowY: 'auto',
            }}
          >
            {suggestions.map((suggestion, idx) => (
              <li
                key={idx}
                onClick={() => handleSuggestionClick(suggestion)}
                style={{
                  padding: '0.7rem 1.2rem',
                  cursor: 'pointer',
                  borderBottom: idx !== suggestions.length - 1 ? '1px solid #f3f3f3' : 'none',
                  background: '#fff',
                  fontWeight: 'normal',
                  fontSize: '1rem',
                  color: '#232f3e',
                  transition: 'background 0.2s',
                }}
                onMouseDown={e => e.preventDefault()}
                onMouseOver={e => e.currentTarget.style.background = '#f7f7fa'}
                onMouseOut={e => e.currentTarget.style.background = '#fff'}
              >
                <span style={{ color: '#007185', fontWeight: 'bold', textAlign: 'left', display: 'block' }}>{suggestion}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
      <button
        type="submit"
        style={{
          background: '#43d4c0',
          color: '#131921',
          border: 'none',
          fontWeight: 'bold',
          cursor: 'pointer',
          padding: '0 1.2rem',
          height: '100%',
          display: 'flex',
          alignItems: 'center',
          borderLeft: '1px solid #FFD814',
        }}
      >
        <span style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>Search</span>
      </button>
    </form>
  );
};

export default ProductSearch;
