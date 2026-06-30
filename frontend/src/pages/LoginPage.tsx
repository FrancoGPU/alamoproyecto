import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Car, Lock, User, AlertCircle, Eye, EyeOff } from 'lucide-react';
import './LoginPage.css';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      setError('Por favor, ingresa tu usuario y contraseña.');
      return;
    }

    setError(null);
    setIsSubmitting(true);

    try {
      await login(username, password);
    } catch (err) {
      setError('Ocurrió un error al intentar iniciar sesión.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-glow-1"></div>
      <div className="login-glow-2"></div>
      
      <div className="login-card fade-in">
        <div className="login-header">
          <div className="login-logo-container">
            <Car size={32} className="login-logo-icon" />
          </div>
          <h1 className="login-title">ÁLAMO</h1>
          <p className="login-subtitle">Sistema de Reservas y Alquiler</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <h2 className="form-title">Iniciar Sesión</h2>
          <p className="form-tagline">Bienvenido al sistema. Ingresa tus credenciales.</p>

          {error && (
            <div className="login-error-alert slide-in-left">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label className="form-label" htmlFor="username">Usuario / DNI</label>
            <div className="input-with-icon">
              <User size={18} className="input-icon" />
              <input
                id="username"
                type="text"
                className="form-input"
                placeholder="Ej. admin o counter"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={isSubmitting}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Contraseña</label>
            <div className="input-with-icon">
              <Lock size={18} className="input-icon" />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isSubmitting}
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
                disabled={isSubmitting}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className="btn btn-primary login-btn" disabled={isSubmitting}>
            {isSubmitting ? (
              <span className="spinner"></span>
            ) : (
              'Ingresar al Sistema'
            )}
          </button>
        </form>

        <div className="login-footer">
          <p>Uso interno. Reservado para colaboradores de Álamo.</p>
          <p className="hint">💡 Tip: Escribe <strong>"admin"</strong> para rol de Administrador, o cualquier otro nombre para rol de <strong>"Counter"</strong>.</p>
        </div>
      </div>
    </div>
  );
};
