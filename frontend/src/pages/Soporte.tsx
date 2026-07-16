import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { 
  Mail, 
  FileText, 
  MessageSquare, 
  CheckCircle, 
  Clock, 
  AlertCircle, 
  Send, 
  Check,
  LifeBuoy
} from 'lucide-react';
import './Soporte.css';
import api from '../services/api';

interface Ticket {
  idTicket?: number;
  cliente: string;
  correo: string;
  asunto: string;
  mensaje: string;
  estado: 'ABIERTO' | 'RESUELTO';
  fechaCreacion?: string;
}

export const Soporte: React.FC = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Formulario
  const [formData, setFormData] = useState({
    asunto: '',
    mensaje: '',
    correo: user?.username ? `${user.username}@alamo.com` : 'soporte@alamo.com',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchTickets = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.get('/soporte');
      setTickets(response.data);
    } catch (e) {
      console.error(e);
      setError('Error al recuperar los tickets de soporte del servidor.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSuccessMsg(null);
    setError(null);

    const payload: Ticket = {
      cliente: user?.nombre || 'Agente Anónimo',
      correo: formData.correo,
      asunto: formData.asunto,
      mensaje: formData.mensaje,
      estado: 'ABIERTO',
    };

    try {
      await api.post('/soporte', payload);
      setSuccessMsg('¡Tu ticket ha sido registrado con éxito! Un administrador lo revisará pronto.');
      setFormData({
        asunto: '',
        mensaje: '',
        correo: user?.username ? `${user.username}@alamo.com` : 'soporte@alamo.com',
      });
      fetchTickets();
    } catch (e) {
      console.error(e);
      setError('Error al enviar el ticket. Intenta de nuevo.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResolveTicket = async (ticket: Ticket) => {
    try {
      const updatedTicket: Ticket = {
        ...ticket,
        estado: 'RESUELTO',
      };
      await api.put(`/soporte/${ticket.idTicket}`, updatedTicket);
      fetchTickets();
    } catch (e) {
      console.error(e);
      alert('Error al actualizar el estado del ticket.');
    }
  };

  const isAdmin = user?.rol === 'ADMINISTRADOR';

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div className="header-text">
          <h1>Soporte Técnico y Ayuda</h1>
          <p>Reporta incidencias, dudas o sugerencias del sistema para la mesa de ayuda.</p>
        </div>
      </div>

      {error && (
        <div className="alert-custom warning-alert">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {successMsg && (
        <div className="alert-custom success-alert" style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', borderColor: '#10b981', color: '#10b981' }}>
          <CheckCircle size={20} />
          <span>{successMsg}</span>
        </div>
      )}

      <div className="soporte-layout">
        {/* Formulario de Enviar Ticket (Para todos los usuarios) */}
        <div className="card soporte-form-card">
          <h2>
            <LifeBuoy size={22} className="card-header-icon" />
            <span>Crear Ticket de Asistencia</span>
          </h2>
          <form onSubmit={handleSubmit} className="soporte-form">
            <div className="form-group">
              <label htmlFor="correo">Correo de Contacto</label>
              <input
                type="email"
                id="correo"
                name="correo"
                className="form-input"
                required
                value={formData.correo}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label htmlFor="asunto">Asunto / Tema</label>
              <input
                type="text"
                id="asunto"
                name="asunto"
                className="form-input"
                placeholder="Ej. Error al guardar vehículo o fallos en red"
                required
                value={formData.asunto}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label htmlFor="mensaje">Detalle de la Incidencia</label>
              <textarea
                id="mensaje"
                name="mensaje"
                className="form-input form-textarea"
                rows={5}
                placeholder="Describe con detalle el inconveniente encontrado..."
                required
                value={formData.mensaje}
                onChange={handleInputChange}
              />
            </div>

            <button type="submit" className="btn btn-primary w-full" disabled={isSubmitting}>
              <Send size={18} />
              <span>{isSubmitting ? 'Enviando...' : 'Enviar Solicitud'}</span>
            </button>
          </form>
        </div>

        {/* Listado de Tickets (Inbox del Administrador, o Historial del Agente) */}
        <div className="card soporte-list-card">
          <h2>
            <MessageSquare size={22} className="card-header-icon" />
            <span>{isAdmin ? 'Bandeja de Tickets Recibidos (Mesa de Control)' : 'Mis Tickets de Ayuda'}</span>
          </h2>

          {isLoading ? (
            <div className="loader-container">
              <div className="spinner"></div>
            </div>
          ) : tickets.length === 0 ? (
            <div className="empty-state">
              <Clock size={48} className="empty-icon" />
              <p>No se han registrado tickets de soporte todavía.</p>
            </div>
          ) : (
            <div className="tickets-list">
              {tickets.map((t) => {
                const dateFormatted = t.fechaCreacion 
                  ? new Date(t.fechaCreacion).toLocaleString() 
                  : 'Fecha no registrada';
                
                return (
                  <div key={t.idTicket} className={`ticket-item ${t.estado.toLowerCase()}`}>
                    <div className="ticket-header">
                      <span className="ticket-subject">{t.asunto}</span>
                      <span className={`status-badge ${t.estado.toLowerCase()}`}>
                        {t.estado === 'ABIERTO' ? <Clock size={12} /> : <CheckCircle size={12} />}
                        <span>{t.estado}</span>
                      </span>
                    </div>

                    <p className="ticket-msg">{t.mensaje}</p>

                    <div className="ticket-footer">
                      <div className="ticket-meta">
                        <span>Por: <strong>{t.cliente}</strong></span>
                        <span className="bullet">•</span>
                        <span>{t.correo}</span>
                        <span className="bullet">•</span>
                        <span>{dateFormatted}</span>
                      </div>

                      {isAdmin && t.estado === 'ABIERTO' && (
                        <button 
                          className="btn btn-secondary btn-sm resolve-btn"
                          onClick={() => handleResolveTicket(t)}
                        >
                          <Check size={14} />
                          <span>Resolver</span>
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
