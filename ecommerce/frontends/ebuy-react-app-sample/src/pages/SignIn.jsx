// import React, { useState } from 'react';

// const SignIn = () => {
//   const [email, setEmail] = useState('');
//   const [password, setPassword] = useState('');
//   const [error, setError] = useState('');

//   const handleSubmit = (e) => {
//     e.preventDefault();
//     // Demo validation
//     if (!email || !password) {
//       setError('Please enter both email and password.');
//       return;
//     }
//     setError('');
//     // Add authentication logic here
//     alert('Signed in successfully!');
//   };

//   return (
//     <div style={{ maxWidth: 400, margin: '60px auto', background: '#fff', padding: '2rem', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
//       <h2 style={{ textAlign: 'center', marginBottom: '1.5rem', color: '#232f3e' }}>Sign In to eBuy</h2>
//       <form onSubmit={handleSubmit}>
//         <div style={{ marginBottom: '1rem' }}>
//           <label htmlFor="email" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Email</label>
//           <input
//             id="email"
//             type="email"
//             value={email}
//             onChange={e => setEmail(e.target.value)}
//             style={{ width: '100%', padding: '0.7rem', borderRadius: '4px', border: '1px solid #ccc' }}
//             autoComplete="username"
//           />
//         </div>
//         <div style={{ marginBottom: '1rem' }}>
//           <label htmlFor="password" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Password</label>
//           <input
//             id="password"
//             type="password"
//             value={password}
//             onChange={e => setPassword(e.target.value)}
//             style={{ width: '100%', padding: '0.7rem', borderRadius: '4px', border: '1px solid #ccc' }}
//             autoComplete="current-password"
//           />
//         </div>
//         {error && <div style={{ color: 'red', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}
//         <button type="submit" style={{ width: '100%', background: '#FFD814', color: '#232f3e', fontWeight: 'bold', border: 'none', borderRadius: '4px', padding: '0.8rem', cursor: 'pointer', fontSize: '1rem' }}>
//           Sign In
//         </button>
//       </form>
//     </div>
//   );
// };

// export default SignIn;

import React from 'react';
import { useAuth } from '../utils/authUtils';

const SignIn = () => {
  const { login, register } = useAuth();

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100vh',
      backgroundColor: '#f5f5f5'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '8px',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
        minWidth: '300px',
        textAlign: 'center'
      }}>
        <h2 style={{ marginBottom: '1.5rem', color: '#232f3e' }}>
          Welcome to EBuy
        </h2>
        
        <p style={{ marginBottom: '1.5rem', color: '#666' }}>
          Please sign in to continue shopping
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button
            onClick={login}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#232f3e',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '1rem'
            }}
          >
            Sign In
          </button>

          <button
            onClick={register}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: 'white',
              color: '#232f3e',
              border: '2px solid #232f3e',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '1rem'
            }}
          >
            Create Account
          </button>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
