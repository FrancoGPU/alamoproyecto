import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { usuarioService } from '../services/usuarioService';
import { vehiculoService } from '../services/vehiculoService';
import { contratoService } from '../services/contratoService';
import { 
  Users, 
  Car, 
  FileText, 
  DollarSign, 
  PlusCircle, 
  CheckCircle2, 
  History,
  TrendingUp
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // Contadores y estado
  const [stats, setStats] = useState({
    usuarios: 0,
    vehiculos: 0,
    contratos: 0,
    ingresos: 0,
  });
  const [recentContratos, setRecentContratos] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [usuarios, vehiculos, contratos] = await Promise.all([
          usuarioService.getAll().catch(() => []),
          vehiculoService.getAll().catch(() => []),
          contratoService.getAll().catch(() => []),
        ]);

        const totalIngresos = contratos.reduce((acc: number, curr: any) => acc + (Number(curr.montoTotal) || 0), 0);

        setStats({
          usuarios: usuarios.length,
          vehiculos: vehiculos.length,
          contratos: contratos.length,
          ingresos: totalIngresos,
        });

        // Ordenamos los contratos por id de forma descendente para simular "recientes"
        const sorted = [...contratos].sort((a, b) => (b.idContrato || 0) - (a.idContrato || 0));
        setRecentContratos(sorted.slice(0, 5));
      } catch (e) {
        console.error("Error al cargar estadísticas", e);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStats();
  }, []);

  return (
    <div className="page-container fade-in">
      <div className="welcome-banner card">
        <div className="welcome-text">
          <h1>¡Hola de nuevo, {user?.nombre}!</h1>
          <p>Este es el estado operativo de Álamo Rent-A-Car para el día de hoy.</p>
        </div>
        <div className="welcome-badge">
          <TrendingUp size={24} />
          <span>Rendimiento Óptimo</span>
        </div>
      </div>

      {/* Grid de Estadísticas */}
      <div className="stats-grid">
        <div className="stat-card card">
          <div className="stat-icon-wrapper blue">
            <Users size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Usuarios Colaboradores</span>
            <h3 className="stat-value">{isLoading ? '...' : stats.usuarios}</h3>
          </div>
        </div>

        <div className="stat-card card">
          <div className="stat-icon-wrapper cyan">
            <Car size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Vehículos en Flota</span>
            <h3 className="stat-value">{isLoading ? '...' : stats.vehiculos}</h3>
          </div>
        </div>

        <div className="stat-card card">
          <div className="stat-icon-wrapper purple">
            <FileText size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Contratos Totales</span>
            <h3 className="stat-value">{isLoading ? '...' : stats.contratos}</h3>
          </div>
        </div>

        <div className="stat-card card">
          <div className="stat-icon-wrapper green">
            <DollarSign size={24} />
          </div>
          <div className="stat-content">
            <span className="stat-label">Ingresos Registrados</span>
            <h3 className="stat-value">
              {isLoading ? '...' : `S/. ${stats.ingresos.toLocaleString('es-PE', { minimumFractionDigits: 2 })}`}
            </h3>
          </div>
        </div>
      </div>

      <div className="dashboard-sections">
        {/* Acciones Rápidas */}
        <div className="dashboard-section card quick-actions">
          <h3>Acciones Rápidas</h3>
          <p className="section-desc">Atajos rápidos para las tareas más frecuentes.</p>
          <div className="actions-grid">
            <button className="action-btn" onClick={() => navigate('/contratos')}>
              <PlusCircle size={20} />
              <span>Registrar Alquiler</span>
            </button>
            <button className="action-btn" onClick={() => navigate('/vehiculos')}>
              <Car size={20} />
              <span>Ver Catálogo</span>
            </button>
            {user?.rol === 'ADMINISTRADOR' && (
              <button className="action-btn" onClick={() => navigate('/usuarios')}>
                <Users size={20} />
                <span>Gestionar Personal</span>
              </button>
            )}
          </div>
        </div>

        {/* Contratos Recientes */}
        <div className="dashboard-section card recent-activity">
          <div className="section-header">
            <div className="title-with-icon">
              <History size={20} className="section-icon" />
              <h3>Últimos Contratos</h3>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={() => navigate('/contratos')}>
              Ver todos
            </button>
          </div>
          
          {isLoading ? (
            <div className="loader-container">
              <div className="spinner"></div>
            </div>
          ) : recentContratos.length === 0 ? (
            <div className="empty-state">
              <FileText size={48} className="empty-icon" />
              <p>No se han registrado contratos de alquiler todavía.</p>
              <button className="btn btn-primary" onClick={() => navigate('/contratos')}>
                Crear Primer Contrato
              </button>
            </div>
          ) : (
            <div className="recent-list">
              {recentContratos.map((contrato) => (
                <div key={contrato.idContrato} className="recent-item">
                  <div className="item-details">
                    <span className="item-code">{contrato.codigo}</span>
                    <span className="item-info">
                      {contrato.vehiculo?.marca} {contrato.vehiculo?.modelo} ({contrato.vehiculo?.placa})
                    </span>
                    <span className="item-meta">
                      Cliente: {contrato.cliente?.nombres} {contrato.cliente?.apellidoPaterno}
                    </span>
                  </div>
                  <div className="item-status">
                    <span className="item-price">S/. {Number(contrato.montoTotal).toFixed(2)}</span>
                    <span className="badge badge-success">
                      <CheckCircle2 size={12} style={{ marginRight: 4 }} />
                      Activo
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
