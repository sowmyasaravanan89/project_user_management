import React from 'react';
import './App.css';
import { AuthProvider, useAuth } from './AuthContext';
import Login from './components/Login';
import UserApp from './components/UserApp';

function AppContent() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="App">
      {isAuthenticated ? <UserApp /> : <Login />}
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;