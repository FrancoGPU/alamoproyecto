import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { 
  Bell, 
  Calendar, 
  Clock, 
  CloudSun,
  CheckCheck,
  BellOff
} from 'lucide-react';
import './Navbar.css';

interface Notification {
  id: number;
  text: string;
  read: boolean;
  time: string;
}

export const Navbar: React.FC = () => {
  const location = useLocation();
  const [time, setTime] = useState(new Date());
  const [showNotifications, setShowNotifications] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const [notifications, setNotifications] = useState<Notification[]>([
    { id: 1, text: 'Contrato ALAMO-SEED-01 generado con éxito.', read: false, time: 'Hace 5m' },
    { id: 2, text: 'Mantenimiento preventivo sugerido para BMW X5.', read: false, time: 'Hace 1h' },
    { id: 3, text: 'Nuevo colaborador registrado: Franco Paolo.', read: false, time: 'Hace 2h' },
  ]);

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // Clic fuera para cerrar dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setShowNotifications(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/':
        return 'Panel de Control (Dashboard)';
      case '/usuarios':
        return 'Gestión de Usuarios';
      case '/vehiculos':
        return 'Catálogo de Vehículos';
      case '/contratos':
        return 'Contratos de Alquiler';
      case '/configuracion':
        return 'Configuración del Sistema';
      default:
        return 'Álamo Rent-A-Car';
    }
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('es-PE', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('es-PE', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  const handleMarkAsRead = (id: number) => {
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const handleMarkAllAsRead = () => {
    setNotifications(prev => prev.map(n => ({ ...n, read: true })));
  };

  return (
    <header className="navbar">
      <div className="navbar-left">
        <h2 className="navbar-title">{getPageTitle()}</h2>
      </div>

      <div className="navbar-right">
        <div className="status-widget">
          <CloudSun size={18} className="status-icon" />
          <span className="status-text">Sistema Online</span>
        </div>

        <div className="time-widget">
          <Calendar size={16} />
          <span className="date-text">{formatDate(time)}</span>
          <span className="divider">|</span>
          <Clock size={16} />
          <span className="time-text">{formatTime(time)}</span>
        </div>

        {/* Contenedor de Notificaciones con Dropdown */}
        <div className="notification-container" ref={dropdownRef}>
          <button 
            className={`notification-btn ${showNotifications ? 'active' : ''}`} 
            title="Notificaciones"
            onClick={() => setShowNotifications(!showNotifications)}
          >
            <Bell size={20} />
            {unreadCount > 0 && <span className="badge-count">{unreadCount}</span>}
          </button>

          {showNotifications && (
            <div className="notifications-dropdown card fade-in">
              <div className="dropdown-header">
                <h3>Notificaciones</h3>
                {unreadCount > 0 && (
                  <button className="mark-all-read" onClick={handleMarkAllAsRead}>
                    <CheckCheck size={14} />
                    <span>Leer todas</span>
                  </button>
                )}
              </div>

              <div className="dropdown-body">
                {notifications.length === 0 ? (
                  <div className="dropdown-empty">
                    <BellOff size={32} />
                    <p>No tienes notificaciones</p>
                  </div>
                ) : (
                  <div className="notifications-list">
                    {notifications.map(n => (
                      <div 
                        key={n.id} 
                        className={`notification-item ${n.read ? 'read' : 'unread'}`}
                        onClick={() => handleMarkAsRead(n.id)}
                      >
                        <div className="item-dot-indicator"></div>
                        <div className="item-content">
                          <p className="notification-text">{n.text}</p>
                          <span className="notification-time">{n.time}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
