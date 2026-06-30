import React, { useState, useEffect } from 'react';
import { vehiculoService, Vehiculo, CategoriaVehiculo } from '../services/vehiculoService';
import { catalogoService } from '../services/catalogoService';
import { 
  Plus, 
  Search, 
  Edit2, 
  Trash2, 
  X, 
  Car, 
  AlertTriangle,
  FolderOpen
} from 'lucide-react';
import './Vehiculos.css';

export const Vehiculos: React.FC = () => {
  const [vehiculos, setVehiculos] = useState<Vehiculo[]>([]);
  const [categorias, setCategorias] = useState<CategoriaVehiculo[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estado del Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedVehiculo, setSelectedVehiculo] = useState<Vehiculo | null>(null);

  // Estado del Formulario
  const [formData, setFormData] = useState({
    placa: '',
    marca: '',
    modelo: '',
    anio: new Date().getFullYear(),
    idCategoria: '',
  });

  const fetchDatos = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [listaVehiculos, listaCategorias] = await Promise.all([
        vehiculoService.getAll(),
        catalogoService.getCategorias(),
      ]);
      setVehiculos(listaVehiculos);
      setCategorias(listaCategorias);

      // Si no hay categorías registradas, creamos unas ficticias por seguridad para el formulario
      if (listaCategorias.length === 0) {
        setCategorias([
          { idCategoria: 1, tipo: 'ECONOMICA', descripcion: 'Autos de bajo consumo' },
          { idCategoria: 2, tipo: 'ESTANDAR', descripcion: 'Sedanes y SUVs familiares' },
          { idCategoria: 3, tipo: 'PREMIUM', descripcion: 'Camionetas y autos de lujo' },
        ]);
      }
    } catch (e) {
      console.error(e);
      setError('Error al conectar con la base de datos de vehículos. Asegúrate de iniciar tu servidor de Spring Boot.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDatos();
  }, []);

  const handleOpenCreateModal = () => {
    setModalMode('create');
    setSelectedVehiculo(null);
    setFormData({
      placa: '',
      marca: '',
      modelo: '',
      anio: new Date().getFullYear(),
      idCategoria: categorias[0]?.idCategoria?.toString() || '1',
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (vehiculo: Vehiculo) => {
    setModalMode('edit');
    setSelectedVehiculo(vehiculo);
    setFormData({
      placa: vehiculo.placa,
      marca: vehiculo.marca || '',
      modelo: vehiculo.modelo || '',
      anio: vehiculo.anio || new Date().getFullYear(),
      idCategoria: vehiculo.categoria?.idCategoria?.toString() || '1',
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este vehículo de la flota?')) return;
    try {
      await vehiculoService.delete(id);
      fetchDatos();
    } catch (e) {
      alert('Error al eliminar el vehículo. Puede estar asociado a un contrato activo.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const targetCat = categorias.find(c => c.idCategoria.toString() === formData.idCategoria);
    if (!targetCat) {
      setError('Selecciona una categoría de vehículo válida.');
      return;
    }

    const payload: Vehiculo = {
      idVehiculo: selectedVehiculo?.idVehiculo,
      placa: formData.placa.toUpperCase().trim(),
      marca: formData.marca.trim(),
      modelo: formData.modelo.trim(),
      anio: Number(formData.anio),
      categoria: targetCat,
    };

    try {
      if (modalMode === 'create') {
        await vehiculoService.create(payload);
      } else {
        if (selectedVehiculo?.idVehiculo) {
          await vehiculoService.update(selectedVehiculo.idVehiculo, payload);
        }
      }
      setIsModalOpen(false);
      fetchDatos();
    } catch (e) {
      setError('Ocurrió un error al guardar los datos del vehículo. Verifica que la placa no esté duplicada.');
    }
  };

  const filteredVehiculos = vehiculos.filter(v => {
    const term = searchTerm.toLowerCase();
    return (
      v.placa.toLowerCase().includes(term) ||
      (v.marca && v.marca.toLowerCase().includes(term)) ||
      (v.modelo && v.modelo.toLowerCase().includes(term)) ||
      (v.categoria && v.categoria.tipo.toLowerCase().includes(term))
    );
  });

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div className="header-text">
          <h1>Flota de Vehículos</h1>
          <p>Administra los vehículos de tu flota, registra nuevas unidades y clasifícalas por categorías.</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenCreateModal}>
          <Plus size={18} />
          <span>Nuevo Vehículo</span>
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
      <div className="table-filters card">
        <div className="search-bar">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="form-input search-input"
            placeholder="Buscar por placa, marca, modelo, categoría..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* Tabla */}
      {isLoading ? (
        <div className="loader-container">
          <div className="spinner"></div>
        </div>
      ) : filteredVehiculos.length === 0 ? (
        <div className="empty-state card">
          <Car size={48} className="empty-icon" />
          <p>No se encontraron vehículos registrados en el catálogo.</p>
        </div>
      ) : (
        <div className="table-container card">
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Placa</th>
                <th>Marca / Modelo</th>
                <th>Año</th>
                <th>Categoría</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredVehiculos.map((v) => (
                <tr key={v.idVehiculo}>
                  <td>{v.idVehiculo}</td>
                  <td className="vehicle-plate">
                    <span className="plate-badge">{v.placa}</span>
                  </td>
                  <td>{v.marca} {v.modelo}</td>
                  <td>{v.anio}</td>
                  <td>
                    <span className={`badge ${
                      v.categoria?.tipo === 'PREMIUM' ? 'badge-danger' : 
                      v.categoria?.tipo === 'ESTANDAR' ? 'badge-warning' : 'badge-success'
                    }`}>
                      {v.categoria?.tipo}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <button 
                      className="action-icon-btn edit" 
                      onClick={() => handleOpenEditModal(v)}
                      title="Editar Vehículo"
                    >
                      <Edit2 size={16} />
                    </button>
                    <button 
                      className="action-icon-btn delete" 
                      onClick={() => v.idVehiculo && handleDelete(v.idVehiculo)}
                      title="Eliminar Vehículo"
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

      {/* Modal */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content fade-in">
            <div className="modal-header">
              <h3>{modalMode === 'create' ? 'Registrar Nuevo Vehículo' : 'Editar Vehículo'}</h3>
              <button className="modal-close" onClick={() => setIsModalOpen(false)}>
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="placa">Número de Placa</label>
                <input
                  id="placa"
                  type="text"
                  required
                  placeholder="Ej. ABC-123"
                  className="form-input"
                  value={formData.placa}
                  onChange={(e) => setFormData({ ...formData, placa: e.target.value })}
                />
              </div>

              <div className="form-row split">
                <div className="form-group">
                  <label className="form-label" htmlFor="marca">Marca</label>
                  <input
                    id="marca"
                    type="text"
                    required
                    placeholder="Ej. Toyota"
                    className="form-input"
                    value={formData.marca}
                    onChange={(e) => setFormData({ ...formData, marca: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="modelo">Modelo</label>
                  <input
                    id="modelo"
                    type="text"
                    required
                    placeholder="Ej. Corolla"
                    className="form-input"
                    value={formData.modelo}
                    onChange={(e) => setFormData({ ...formData, modelo: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-row split">
                <div className="form-group">
                  <label className="form-label" htmlFor="anio">Año de Fabricación</label>
                  <input
                    id="anio"
                    type="number"
                    required
                    min="1900"
                    max={new Date().getFullYear() + 1}
                    className="form-input"
                    value={formData.anio}
                    onChange={(e) => setFormData({ ...formData, anio: Number(e.target.value) })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="categoria">Categoría</label>
                  <select
                    id="categoria"
                    className="form-select"
                    value={formData.idCategoria}
                    onChange={(e) => setFormData({ ...formData, idCategoria: e.target.value })}
                  >
                    {categorias.map((c) => (
                      <option key={c.idCategoria} value={c.idCategoria}>
                        {c.tipo} ({c.descripcion})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary">
                  {modalMode === 'create' ? 'Registrar' : 'Guardar Cambios'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
