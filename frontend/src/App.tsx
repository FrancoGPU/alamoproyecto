import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/layout/Sidebar';
import { Navbar } from './components/layout/Navbar';
import { LoginPage } from './pages/LoginPage';
import { Dashboard } from './pages/Dashboard';
import { Usuarios } from './pages/Usuarios';
import { Vehiculos } from './pages/Vehiculos';
import { Contratos } from './pages/Contratos';
import { Configuracion } from './pages/Configuracion';
import { Soporte } from './pages/Soporte';

const AppContent: React.FC = () => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="loader-container" style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div className="spinner" style={{ width: '40px', height: '40px' }}></div>
      </div>
    );
  }

  if (!user) {
    return <LoginPage />;
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <main className="page-container">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            {user.rol === 'ADMINISTRADOR' && (
              <Route path="/usuarios" element={<Usuarios />} />
            )}
            <Route path="/vehiculos" element={<Vehiculos />} />
            <Route path="/contratos" element={<Contratos />} />
            <Route path="/configuracion" element={<Configuracion />} />
            <Route path="/soporte" element={<Soporte />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppContent />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
