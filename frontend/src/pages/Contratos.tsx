import React, { useState, useEffect } from 'react';
import { contratoService, ContratoAlquiler, Seguro, Servicio, Horario, ContratoSeguro } from '../services/contratoService';
import { usuarioService, Usuario } from '../services/usuarioService';
import { vehiculoService, Vehiculo } from '../services/vehiculoService';
import { catalogoService } from '../services/catalogoService';
import { 
  Plus, 
  Search, 
  X, 
  FileText, 
  AlertTriangle,
  Calendar,
  CheckSquare,
  Square,
  DollarSign,
  User,
  Car,
  Trash2,
  Download,
  Edit
} from 'lucide-react';
import './Contratos.css';
import api from '../services/api';

export const Contratos: React.FC = () => {
  const [contratos, setContratos] = useState<ContratoAlquiler[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [vehiculos, setVehiculos] = useState<Vehiculo[]>([]);
  const [seguros, setSeguros] = useState<Seguro[]>([]);
  const [servicios, setServicios] = useState<Servicio[]>([]);
  const [horarios, setHorarios] = useState<Horario[]>([]);
  const [contratosSeguro, setContratosSeguro] = useState<ContratoSeguro[]>([]);

  const [searchTerm, setSearchTerm] = useState('');
  const [filterCategory, setFilterCategory] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estado del Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedContrato, setSelectedContrato] = useState<ContratoAlquiler | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  // Formulario de Creación
  const [formData, setFormData] = useState({
    idCliente: '',
    idVehiculo: '',
    fechaInicio: new Date().toISOString().split('T')[0],
    fechaFin: new Date(Date.now() + 86400000).toISOString().split('T')[0], // +1 día
    idSeguro: '',
    idHorario: '',
    idContratoSeguro: '',
    serviciosSeleccionados: [] as number[],
  });

  const [precioCalculado, setPrecioCalculado] = useState(0);

  const fetchDatos = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [
        listaContratos,
        listaUsuarios,
        listaVehiculos,
        listaSeguros,
        listaServicios,
        listaHorarios,
        listaContratosSeguro
      ] = await Promise.all([
        contratoService.getAll().catch(() => []),
        usuarioService.getAll().catch(() => []),
        vehiculoService.getAll().catch(() => []),
        catalogoService.getSeguros().catch(() => []),
        catalogoService.getServicios().catch(() => []),
        catalogoService.getHorarios().catch(() => []),
        catalogoService.getContratosSeguro().catch(() => []),
      ]);

      setContratos(listaContratos);
      setUsuarios(listaUsuarios);
      setVehiculos(listaVehiculos);
      setSeguros(listaSeguros);
      setServicios(listaServicios);
      setHorarios(listaHorarios);
      setContratosSeguro(listaContratosSeguro);
    } catch (e) {
      console.error(e);
      setError('Error al recuperar datos para contratos de alquiler. Asegúrate de que el backend esté ejecutándose.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDatos();
  }, []);

  // Efecto para calcular precio dinámicamente
  useEffect(() => {
    if (!formData.idVehiculo) {
      setPrecioCalculado(0);
      return;
    }

    const vehiculo = vehiculos.find(v => v.idVehiculo?.toString() === formData.idVehiculo);
    if (!vehiculo) return;

    // Calcular días
    const fInicio = new Date(formData.fechaInicio);
    const fFin = new Date(formData.fechaFin);
    const diffTime = Math.abs(fFin.getTime() - fInicio.getTime());
    const diffDays = Math.max(1, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));

    // Tarifa base diaria de la categoría
    let tarifaDiaria = 50;
    const cat = vehiculo.categoria?.tipo;
    if (cat === 'ESTANDAR') tarifaDiaria = 95;
    else if (cat === 'PREMIUM') tarifaDiaria = 175;

    let subtotal = tarifaDiaria * diffDays;

    // Sumar seguros
    if (formData.idSeguro) {
      // Supongamos que el seguro cuesta S/. 25 diarios
      subtotal += 25 * diffDays;
    }

    // Sumar servicios adicionales (costo único)
    formData.serviciosSeleccionados.forEach(id => {
      const serv = servicios.find(s => s.idServicio === id);
      if (serv) {
        subtotal += Number(serv.precio) || 0;
      }
    });

    setPrecioCalculado(subtotal);
  }, [formData, vehiculos, servicios]);

  // Filtrar vehículos disponibles al cambiar fechas
  useEffect(() => {
    if (!formData.fechaInicio || !formData.fechaFin) return;
    
    let active = true;
    const fetchDisponibles = async () => {
      try {
        const response = await api.get(`/vehiculos/disponibles?fechaInicio=${formData.fechaInicio}&fechaFin=${formData.fechaFin}`);
        if (active) {
          setVehiculos(response.data);
          if (response.data.length > 0) {
            const exists = response.data.some((v: any) => v.idVehiculo?.toString() === formData.idVehiculo);
            if (!exists) {
              setFormData(prev => ({
                ...prev,
                idVehiculo: response.data[0].idVehiculo?.toString() || ''
              }));
            }
          } else {
            setFormData(prev => ({
              ...prev,
              idVehiculo: ''
            }));
          }
        }
      } catch (e) {
        console.error("Error al filtrar disponibilidad", e);
      }
    };

    if (isModalOpen) {
      fetchDisponibles();
    }
    return () => {
      active = false;
    };
  }, [formData.fechaInicio, formData.fechaFin, isModalOpen]);

  const handleDownload = async (format: 'excel' | 'pdf') => {
    try {
      const response = await api.get(`/reportes/contratos/${format}`, {
        responseType: 'blob',
      });
      const blob = new Blob([response.data], {
        type: format === 'excel' 
          ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          : 'application/pdf',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `reporte_contratos.${format === 'excel' ? 'xlsx' : 'pdf'}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Error al descargar el reporte', error);
      alert('No se pudo descargar el reporte.');
    }
  };

  const handleOpenCreateModal = () => {
    if (usuarios.length === 0 || vehiculos.length === 0) {
      alert('Debes tener al menos un usuario (cliente) y un vehículo registrado para poder alquilar.');
      return;
    }

    setFormData({
      idCliente: usuarios[0]?.idUsuario?.toString() || '',
      idVehiculo: vehiculos[0]?.idVehiculo?.toString() || '',
      fechaInicio: new Date().toISOString().split('T')[0],
      fechaFin: new Date(Date.now() + 86400000).toISOString().split('T')[0],
      idSeguro: seguros[0]?.idSeguro?.toString() || '',
      idHorario: horarios[0]?.idHorario?.toString() || '',
      idContratoSeguro: contratosSeguro[0]?.idContratoSeguro?.toString() || '',
      serviciosSeleccionados: [],
    });
    setSelectedContrato(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (contrato: ContratoAlquiler) => {
    setSelectedContrato(contrato);
    setFormData({
      idCliente: contrato.cliente?.idUsuario?.toString() || '',
      idVehiculo: contrato.vehiculo?.idVehiculo?.toString() || '',
      fechaInicio: contrato.fechaInicio || '',
      fechaFin: contrato.fechaFin || '',
      idSeguro: contrato.seguro?.idSeguro?.toString() || '',
      idHorario: contrato.horario?.idHorario?.toString() || '',
      idContratoSeguro: contrato.contratoSeguro?.idContratoSeguro?.toString() || '',
      serviciosSeleccionados: contrato.servicios?.map(s => s.idServicio || 0) || [],
    });
    setIsModalOpen(true);
  };

  const handleToggleServicio = (id: number) => {
    setFormData(prev => {
      const check = prev.serviciosSeleccionados.includes(id);
      return {
        ...prev,
        serviciosSeleccionados: check 
          ? prev.serviciosSeleccionados.filter(x => x !== id)
          : [...prev.serviciosSeleccionados, id],
      };
    });
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Está seguro de que desea rescindir este contrato?')) return;
    try {
      await contratoService.delete(id);
      fetchDatos();
    } catch (e) {
      alert('Error al rescindir el contrato.');
    }
  };

  const handleOpenDetailModal = (contrato: ContratoAlquiler) => {
    setSelectedContrato(contrato);
    setIsDetailModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const client = usuarios.find(u => u.idUsuario?.toString() === formData.idCliente);
    const car = vehiculos.find(v => v.idVehiculo?.toString() === formData.idVehiculo);
    const ins = seguros.find(s => s.idSeguro?.toString() === formData.idSeguro);
    const sched = horarios.find(h => h.idHorario?.toString() === formData.idHorario);
    const insContract = contratosSeguro.find(cs => cs.idContratoSeguro?.toString() === formData.idContratoSeguro);

    const selectedServs = servicios.filter(s => formData.serviciosSeleccionados.includes(s.idServicio || 0));

    if (!client || !car) {
      setError('Por favor, selecciona un cliente y un vehículo válidos.');
      return;
    }

    const uniqueCode = selectedContrato ? selectedContrato.codigo : `ALAMO-${Date.now().toString().slice(-6)}-${formData.idVehiculo}`;

    const payload: ContratoAlquiler = {
      idContrato: selectedContrato ? selectedContrato.idContrato : undefined,
      codigo: uniqueCode,
      fechaInicio: formData.fechaInicio,
      fechaFin: formData.fechaFin,
      montoTotal: precioCalculado,
      vehiculo: car,
      cliente: client,
      seguro: ins || undefined,
      horario: sched || undefined,
      contratoSeguro: insContract || undefined,
      servicios: selectedServs,
    };

    try {
      if (selectedContrato && selectedContrato.idContrato) {
        await api.put(`/contratos-alquiler/${selectedContrato.idContrato}`, payload);
      } else {
        await contratoService.create(payload);
      }
      setIsModalOpen(false);
      fetchDatos();
    } catch (err) {
      console.error(err);
      setError('Error al registrar el contrato de alquiler en el backend.');
    }
  };

  const filteredContratos = contratos.filter(c => {
    const term = searchTerm.toLowerCase();
    const matchesSearch = (
      c.codigo.toLowerCase().includes(term) ||
      (c.cliente && c.cliente.nombres.toLowerCase().includes(term)) ||
      (c.cliente && c.cliente.apellidoPaterno?.toLowerCase().includes(term)) ||
      (c.vehiculo && c.vehiculo.placa.toLowerCase().includes(term)) ||
      (c.vehiculo && c.vehiculo.marca.toLowerCase().includes(term))
    );
    const matchesCategory = filterCategory === '' || (c.vehiculo?.categoria?.tipo === filterCategory);
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div className="header-text">
          <h1>Contratos de Alquiler</h1>
          <p>Consulta, registra y rescinde contratos transaccionales para la renta de vehículos.</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenCreateModal}>
          <Plus size={18} />
          <span>Registrar Alquiler</span>
        </button>
      </div>

      {error && (
        <div className="alert-custom warning-alert">
          <AlertTriangle size={20} />
          <span>{error}</span>
          <button className="btn btn-secondary btn-sm" onClick={fetchDatos} style={{ marginLeft: 'auto' }}>
            Reintentar
          </button>
        </div>
      )}

      {/* Filtros */}
      <div className="table-filters card" style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
        <div className="search-bar" style={{ flex: 1 }}>
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="form-input search-input"
            placeholder="Buscar por código de contrato, cliente, placa..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        
        <div className="category-filter" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Categoría:</span>
          <select
            className="form-select"
            style={{ width: '160px', padding: '6px 12px', fontSize: '0.85rem', height: '38px', borderRadius: 'var(--border-radius-sm)', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)', color: 'var(--text-primary)' }}
            value={filterCategory}
            onChange={(e) => setFilterCategory(e.target.value)}
          >
            <option value="">Todas</option>
            <option value="ECONOMICO">Económico</option>
            <option value="ESTANDAR">Estándar</option>
            <option value="PREMIUM">Premium</option>
          </select>
        </div>
        <div className="export-actions" style={{ display: 'flex', gap: '8px', marginLeft: 'auto' }}>
          <button className="btn btn-secondary" onClick={() => handleDownload('excel')} title="Exportar a Excel">
            <Download size={16} />
            <span>Excel</span>
          </button>
          <button className="btn btn-secondary" onClick={() => handleDownload('pdf')} title="Exportar a PDF">
            <FileText size={16} />
            <span>PDF</span>
          </button>
        </div>
      </div>

      {/* Tabla */}
      {isLoading ? (
        <div className="loader-container">
          <div className="spinner"></div>
        </div>
      ) : filteredContratos.length === 0 ? (
        <div className="empty-state card">
          <FileText size={48} className="empty-icon" />
          <p>No se encontraron contratos registrados.</p>
        </div>
      ) : (
        <div className="table-container card">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Cliente</th>
                <th>Vehículo</th>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredContratos.map((c) => (
                <tr key={c.idContrato}>
                  <td className="contract-code" onClick={() => handleOpenDetailModal(c)}>
                    {c.codigo}
                  </td>
                  <td>{c.cliente?.nombres} {c.cliente?.apellidoPaterno}</td>
                  <td>
                    <span className="plate-badge mini-plate">{c.vehiculo?.placa}</span>
                    <span style={{ marginLeft: 8 }}>{c.vehiculo?.marca} {c.vehiculo?.modelo}</span>
                  </td>
                  <td>{c.fechaInicio}</td>
                  <td>{c.fechaFin}</td>
                  <td className="price-td">S/. {Number(c.montoTotal).toFixed(2)}</td>
                  <td>
                    {c.estado === 'RESCINDIDO' ? (
                      <span className="badge-danger" style={{ padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, backgroundColor: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
                        Rescindido
                      </span>
                    ) : (
                      <span className="badge-success" style={{ padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, backgroundColor: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                        Activo
                      </span>
                    )}
                  </td>
                  <td className="actions-cell" style={{ display: 'flex', gap: '8px' }}>
                    <button 
                      className="action-icon-btn edit" 
                      style={{ color: '#6366f1', backgroundColor: 'rgba(99, 102, 241, 0.1)', border: 'none', padding: '6px', borderRadius: '4px', cursor: 'pointer', opacity: c.estado === 'RESCINDIDO' ? 0.5 : 1 }}
                      onClick={() => handleOpenEditModal(c)}
                      title="Editar Contrato"
                      disabled={c.estado === 'RESCINDIDO'}
                    >
                      <Edit size={16} />
                    </button>
                    <button 
                      className="action-icon-btn delete" 
                      style={{ opacity: c.estado === 'RESCINDIDO' ? 0.5 : 1 }}
                      onClick={() => c.idContrato && handleDelete(c.idContrato)}
                      title="Rescindir Contrato"
                      disabled={c.estado === 'RESCINDIDO'}
                    >
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Creación */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content large fade-in">
            <div className="modal-header">
              <h3>{selectedContrato ? 'Editar Contrato de Alquiler' : 'Registrar Contrato de Alquiler'}</h3>
              <button className="modal-close" onClick={() => setIsModalOpen(false)}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-row split">
                <div className="form-group">
                  <label className="form-label" htmlFor="cliente">
                    <User size={14} style={{ marginRight: 6 }} />
                    Cliente
                  </label>
                  <select
                    id="cliente"
                    className="form-select"
                    value={formData.idCliente}
                    onChange={(e) => setFormData({ ...formData, idCliente: e.target.value })}
                  >
                    {usuarios.map(u => (
                      <option key={u.idUsuario} value={u.idUsuario}>
                        {u.nombres} {u.apellidoPaterno} ({u.correo})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="vehiculo">
                    <Car size={14} style={{ marginRight: 6 }} />
                    Vehículo Disponible
                  </label>
                  <select
                    id="vehiculo"
                    className="form-select"
                    value={formData.idVehiculo}
                    onChange={(e) => setFormData({ ...formData, idVehiculo: e.target.value })}
                  >
                    {vehiculos.map(v => (
                      <option key={v.idVehiculo} value={v.idVehiculo}>
                        {v.placa} - {v.marca} {v.modelo} ({v.categoria?.tipo})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row split">
                <div className="form-group">
                  <label className="form-label" htmlFor="fechaInicio">
                    <Calendar size={14} style={{ marginRight: 6 }} />
                    Fecha de Entrega
                  </label>
                  <input
                    id="fechaInicio"
                    type="date"
                    required
                    className="form-input"
                    value={formData.fechaInicio}
                    onChange={(e) => setFormData({ ...formData, fechaInicio: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="fechaFin">
                    <Calendar size={14} style={{ marginRight: 6 }} />
                    Fecha de Devolución
                  </label>
                  <input
                    id="fechaFin"
                    type="date"
                    required
                    className="form-input"
                    value={formData.fechaFin}
                    onChange={(e) => setFormData({ ...formData, fechaFin: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-row split">
                {seguros.length > 0 && (
                  <div className="form-group">
                    <label className="form-label" htmlFor="seguro">Seguro Cobertura</label>
                    <select
                      id="seguro"
                      className="form-select"
                      value={formData.idSeguro}
                      onChange={(e) => setFormData({ ...formData, idSeguro: e.target.value })}
                    >
                      <option value="">Sin seguro adicional</option>
                      {seguros.filter(s => s.activo).map(s => (
                        <option key={s.idSeguro} value={s.idSeguro}>
                          {s.nombre} (+ S/. 25/día)
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                {horarios.length > 0 && (
                  <div className="form-group">
                    <label className="form-label" htmlFor="horario">Horario de Recojo</label>
                    <select
                      id="horario"
                      className="form-select"
                      value={formData.idHorario}
                      onChange={(e) => setFormData({ ...formData, idHorario: e.target.value })}
                    >
                      <option value="">Seleccionar horario...</option>
                      {horarios.map(h => (
                        <option key={h.idHorario} value={h.idHorario}>
                          {h.dia} ({h.horaInicio} - {h.horaFin})
                        </option>
                      ))}
                    </select>
                  </div>
                )}
              </div>

              {servicios.length > 0 && (
                <div className="form-group">
                  <label className="form-label">Servicios Adicionales (Extras)</label>
                  <div className="services-checkbox-grid">
                    {servicios.map(s => {
                      const isChecked = formData.serviciosSeleccionados.includes(s.idServicio || 0);
                      return (
                        <div 
                          key={s.idServicio} 
                          className={`service-check-card ${isChecked ? 'selected' : ''}`}
                          onClick={() => s.idServicio && handleToggleServicio(s.idServicio)}
                        >
                          <div className="check-icon">
                            {isChecked ? <CheckSquare size={18} className="icon-blue" /> : <Square size={18} />}
                          </div>
                          <div className="service-desc">
                            <span className="service-name">{s.nombre}</span>
                            <span className="service-price">S/. {Number(s.precio).toFixed(2)}</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Caja de Resumen de Precios */}
              <div className="price-summary-box">
                <div className="summary-row">
                  <span>Monto Total de Alquiler Estimado:</span>
                  <span className="total-amount">
                    <DollarSign size={20} />
                    S/. {precioCalculado.toFixed(2)}
                  </span>
                </div>
                <p className="summary-note">El precio definitivo se calculará al confirmar las fechas seleccionadas.</p>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary">
                  Confirmar y Generar Contrato
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Detalle */}
      {isDetailModalOpen && selectedContrato && (
        <div className="modal-overlay">
          <div className="modal-content fade-in">
            <div className="modal-header">
              <h3>Detalles del Contrato {selectedContrato.codigo}</h3>
              <button className="modal-close" onClick={() => setIsDetailModalOpen(false)}>
                <X size={20} />
              </button>
            </div>
            
            <div className="contract-detail-body">
              <div className="detail-section">
                <h4>Información del Cliente</h4>
                <p><strong>Nombres:</strong> {selectedContrato.cliente?.nombres} {selectedContrato.cliente?.apellidoPaterno}</p>
                <p><strong>Correo:</strong> {selectedContrato.cliente?.correo}</p>
              </div>

              <div className="detail-section">
                <h4>Vehículo Alquilado</h4>
                <p><strong>Marca / Modelo:</strong> {selectedContrato.vehiculo?.marca} {selectedContrato.vehiculo?.modelo}</p>
                <p><strong>Placa:</strong> <span className="plate-badge mini-plate">{selectedContrato.vehiculo?.placa}</span></p>
                <p><strong>Categoría:</strong> {selectedContrato.vehiculo?.categoria?.tipo}</p>
              </div>

              <div className="detail-section">
                <h4>Fechas de Renta</h4>
                <p><strong>Entrega:</strong> {selectedContrato.fechaInicio}</p>
                <p><strong>Devolución:</strong> {selectedContrato.fechaFin}</p>
              </div>

              {selectedContrato.seguro && (
                <div className="detail-section">
                  <h4>Seguros Contratados</h4>
                  <p><strong>Cobertura:</strong> {selectedContrato.seguro.nombre}</p>
                </div>
              )}

              {selectedContrato.servicios && selectedContrato.servicios.length > 0 && (
                <div className="detail-section">
                  <h4>Servicios Adicionales contratados</h4>
                  <ul>
                    {selectedContrato.servicios.map(s => (
                      <li key={s.idServicio}>
                        {s.nombre} - S/. {Number(s.precio).toFixed(2)}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              <div className="detail-total-box">
                <span>Monto Total Cobrado</span>
                <h3>S/. {Number(selectedContrato.montoTotal).toFixed(2)}</h3>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                <button 
                  className="btn btn-primary" 
                  onClick={() => {
                    setIsDetailModalOpen(false);
                    handleOpenEditModal(selectedContrato);
                  }}
                  style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                >
                  <Edit size={16} />
                  <span>Editar Contrato</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
