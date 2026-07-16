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
import api from '../../services/api';

interface NotificationItem {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  leido: boolean;
  tipo: string;
  fechaCreacion?: string;
}

export const Navbar: React.FC = () => {
  const location = useLocation();
  const [time, setTime] = useState(new Date());
  const [showNotifications, setShowNotifications] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const [notifications, setNotifications] = useState<NotificationItem[]>([]);

  const fetchNotifications = async () => {
    try {
      const response = await api.get('/notificaciones/recientes');
      setNotifications(response.data);
    } catch (e) {
      console.error("Error al cargar notificaciones:", e);
    }
  };

  useEffect(() => {
    fetchNotifications();
    const intervalNotif = setInterval(fetchNotifications, 10000); // Polling cada 10s
    return () => clearInterval(intervalNotif);
  }, []);

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

  const formatNotificationTime = (dateStr?: string) => {
    if (!dateStr) return 'Hace un momento';
    try {
      const date = new Date(dateStr);
      const diffMs = Date.now() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      if (diffMins < 1) return 'Hace un momento';
      if (diffMins < 60) return `Hace ${diffMins}m`;
      const diffHours = Math.floor(diffMins / 60);
      if (diffHours < 24) return `Hace ${diffHours}h`;
      return date.toLocaleDateString();
    } catch (e) {
      return 'Recientemente';
    }
  };

  const unreadCount = notifications.filter(n => !n.leido).length;

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.put(`/notificaciones/${id}/leer`);
      setNotifications(prev => prev.map(n => n.idNotificacion === id ? { ...n, leido: true } : n));
    } catch (e) {
      console.error("Error al marcar como leída:", e);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await api.put('/notificaciones/leer-todas');
      setNotifications(prev => prev.map(n => ({ ...n, leido: true })));
    } catch (e) {
      console.error("Error al marcar todas como leídas:", e);
    }
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
                        key={n.idNotificacion} 
                        className={`notification-item ${n.leido ? 'read' : 'unread'}`}
                        onClick={() => handleMarkAsRead(n.idNotificacion)}
                      >
                        <div className="item-dot-indicator"></div>
                        <div className="item-content">
                          <strong className="notification-title" style={{ fontSize: '0.85rem', color: 'var(--text-primary)', display: 'block', marginBottom: '2px' }}>{n.titulo}</strong>
                          <p className="notification-text" style={{ margin: 0, fontSize: '0.8rem' }}>{n.mensaje}</p>
                          <span className="notification-time">{formatNotificationTime(n.fechaCreacion)}</span>
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
