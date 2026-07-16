import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  LayoutDashboard, 
  Users, 
  Car, 
  FileText, 
  Settings, 
  LogOut,
  ShieldAlert,
  LifeBuoy
} from 'lucide-react';
import './Sidebar.css';

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-icon-wrapper">
          <Car size={24} className="brand-icon" />
        </div>
        <div>
          <span className="brand-title">ÁLAMO</span>
          <span className="brand-subtitle">Rent-A-Car</span>
        </div>
      </div>

      <nav className="sidebar-menu">
        <NavLink 
          to="/" 
          className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
        >
          <LayoutDashboard size={20} />
          <span>Dashboard</span>
        </NavLink>

        {user?.rol === 'ADMINISTRADOR' && (
          <NavLink 
            to="/usuarios" 
            className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
          >
            <Users size={20} />
            <span>Usuarios</span>
          </NavLink>
        )}

        <NavLink 
          to="/vehiculos" 
          className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
        >
          <Car size={20} />
          <span>Vehículos</span>
        </NavLink>

        <NavLink 
          to="/contratos" 
          className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
        >
          <FileText size={20} />
          <span>Contratos</span>
        </NavLink>

        <NavLink 
          to="/configuracion" 
          className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
        >
          <Settings size={20} />
          <span>Configuración</span>
        </NavLink>

        <NavLink 
          to="/soporte" 
          className={({ isActive }) => `menu-item ${isActive ? 'active' : ''}`}
        >
          <LifeBuoy size={20} />
          <span>Soporte</span>
        </NavLink>
      </nav>

      <div className="sidebar-footer">
        <div className="user-profile">
          <div className="user-avatar">
            {user?.nombre.charAt(0)}
          </div>
          <div className="user-info">
            <span className="user-name" title={user?.nombre}>{user?.nombre}</span>
            <span className="user-role">
              <ShieldAlert size={12} style={{ marginRight: 4 }} />
              {user?.rol}
            </span>
          </div>
        </div>
        <button className="logout-btn" onClick={logout}>
          <LogOut size={18} />
          <span>Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  );
};
