import React, { createContext, useContext, useState } from 'react';

const CartContext = createContext();


export const CartProvider = ({ children }) => {
  const [cartCount, setCartCount] = useState(0);
  const [cartItems, setCartItems] = useState([]);

  // Add item to cart (or update quantity if exists)
  const addToCart = (item) => {
    setCartItems(prevItems => {
      const existing = prevItems.find(i => i.productId === item.productId);
      if (existing) {
        return prevItems.map(i =>
          i.productId === item.productId
            ? { ...i, quantity: i.quantity + item.quantity }
            : i
        );
      } else {
        return [...prevItems, item];
      }
    });
    setCartCount(prev => prev + item.quantity);
  };

  // Replace all cart items (e.g., after fetching from API)
  const setCart = (items) => {
    setCartItems(items);
    setCartCount(items.reduce((sum, i) => sum + i.quantity, 0));
  };

  return (
    <CartContext.Provider value={{ cartCount, setCartCount, cartItems, setCartItems, addToCart, setCart }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
