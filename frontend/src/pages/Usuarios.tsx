import React, { useState, useEffect } from 'react';
import { usuarioService, Usuario, Rol } from '../services/usuarioService';
import { catalogoService } from '../services/catalogoService';
import { 
  Plus, 
  Search, 
  Edit2, 
  Trash2, 
  X, 
  UserPlus, 
  AlertTriangle,
  UserCheck,
  Download,
  FileText
} from 'lucide-react';
import './Usuarios.css';
import api from '../services/api';

export const Usuarios: React.FC = () => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [roles, setRoles] = useState<Rol[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estado del Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create');
  const [selectedUsuario, setSelectedUsuario] = useState<Usuario | null>(null);

  // Estado del Formulario
  const [formData, setFormData] = useState({
    nombres: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    correo: '',
    contrasenaHash: '',
    idRol: '',
  });

  const fetchDatos = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [listaUsuarios, listaRoles] = await Promise.all([
        usuarioService.getAll(),
        catalogoService.getRoles(),
      ]);
      setUsuarios(listaUsuarios);
      setRoles(listaRoles);

      // Si no hay roles registrados en la BD, creamos unos ficticios por seguridad en el dropdown
      if (listaRoles.length === 0) {
        setRoles([
          { idRol: 1, nombre: 'ADMINISTRADOR' },
          { idRol: 2, nombre: 'COUNTER' }
        ]);
      }
    } catch (e) {
      console.error(e);
      setError('Error al cargar la información de la base de datos. Asegúrate de tener PostgreSQL encendido.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDatos();
  }, []);

  const handleOpenCreateModal = () => {
    setModalMode('create');
    setSelectedUsuario(null);
    setFormData({
      nombres: '',
      apellidoPaterno: '',
      apellidoMaterno: '',
      correo: '',
      contrasenaHash: '',
      idRol: roles[0]?.idRol?.toString() || '1',
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (usuario: Usuario) => {
    setModalMode('edit');
    setSelectedUsuario(usuario);
    setFormData({
      nombres: usuario.nombres,
      apellidoPaterno: usuario.apellidoPaterno || '',
      apellidoMaterno: usuario.apellidoMaterno || '',
      correo: usuario.correo,
      contrasenaHash: '********', // No editamos contraseña de forma simple
      idRol: usuario.rol?.idRol?.toString() || '1',
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Está seguro de que desea eliminar a este colaborador?')) return;
    try {
      await usuarioService.delete(id);
      fetchDatos();
    } catch (e) {
      alert('Error al eliminar el usuario.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const targetRol = roles.find(r => r.idRol.toString() === formData.idRol);
    if (!targetRol) {
      setError('Por favor, selecciona un rol válido.');
      return;
    }

    const payload: Usuario = {
      idUsuario: selectedUsuario?.idUsuario,
      nombres: formData.nombres,
      apellidoPaterno: formData.apellidoPaterno,
      apellidoMaterno: formData.apellidoMaterno,
      correo: formData.correo,
      contrasenaHash: modalMode === 'create' ? formData.contrasenaHash : (selectedUsuario?.contrasenaHash || ''),
      rol: targetRol,
    };

    try {
      if (modalMode === 'create') {
        await usuarioService.create(payload);
      } else {
        if (selectedUsuario?.idUsuario) {
          await usuarioService.update(selectedUsuario.idUsuario, payload);
        }
      }
      setIsModalOpen(false);
      fetchDatos();
    } catch (e) {
      setError('Ocurrió un error al guardar los datos del usuario. Verifica los campos.');
    }
  };

  const handleDownload = async (format: 'excel' | 'pdf') => {
    try {
      const response = await api.get(`/reportes/usuarios/${format}`, {
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
      link.setAttribute('download', `reporte_usuarios.${format === 'excel' ? 'xlsx' : 'pdf'}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Error al descargar el reporte', error);
      alert('No se pudo descargar el reporte.');
    }
  };

  const filteredUsuarios = usuarios.filter(u => {
    const term = searchTerm.toLowerCase();
    return (
      u.nombres.toLowerCase().includes(term) ||
      (u.apellidoPaterno && u.apellidoPaterno.toLowerCase().includes(term)) ||
      u.correo.toLowerCase().includes(term) ||
      (u.rol && u.rol.nombre.toLowerCase().includes(term))
    );
  });

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div className="header-text">
          <h1>Gestión de Colaboradores</h1>
          <p>Registra y administra las cuentas de usuarios internos del sistema.</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenCreateModal}>
          <Plus size={18} />
          <span>Nuevo Colaborador</span>
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

      {/* Barra de Filtros */}
      <div className="table-filters card">
        <div className="search-bar">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="form-input search-input"
            placeholder="Buscar por nombre, correo, rol..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
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

      {/* Tabla de Resultados */}
      {isLoading ? (
        <div className="loader-container">
          <div className="spinner"></div>
        </div>
      ) : filteredUsuarios.length === 0 ? (
        <div className="empty-state card">
          <UserPlus size={48} className="empty-icon" />
          <p>No se encontraron colaboradores en la base de datos.</p>
        </div>
      ) : (
        <div className="table-container card">
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre Completo</th>
                <th>Correo Electrónico</th>
                <th>Rol</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsuarios.map((u) => (
                <tr key={u.idUsuario}>
                  <td>{u.idUsuario}</td>
                  <td className="user-table-name">
                    <UserCheck size={16} className="user-icon" />
                    <span>{u.nombres} {u.apellidoPaterno} {u.apellidoMaterno}</span>
                  </td>
                  <td>{u.correo}</td>
                  <td>
                    <span className={`badge ${u.rol?.nombre === 'ADMINISTRADOR' ? 'badge-danger' : 'badge-info'}`}>
                      {u.rol?.nombre}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <button 
                      className="action-icon-btn edit" 
                      onClick={() => handleOpenEditModal(u)}
                      title="Editar Colaborador"
                    >
                      <Edit2 size={16} />
                    </button>
                    <button 
                      className="action-icon-btn delete" 
                      onClick={() => u.idUsuario && handleDelete(u.idUsuario)}
                      title="Eliminar Colaborador"
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

      {/* Modal de Registro / Edición */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content fade-in">
            <div className="modal-header">
              <h3>{modalMode === 'create' ? 'Registrar Nuevo Colaborador' : 'Editar Colaborador'}</h3>
              <button className="modal-close" onClick={() => setIsModalOpen(false)}>
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="nombres">Nombres</label>
                  <input
                    id="nombres"
                    type="text"
                    required
                    className="form-input"
                    value={formData.nombres}
                    onChange={(e) => setFormData({ ...formData, nombres: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-row split">
                <div className="form-group">
                  <label className="form-label" htmlFor="apellidoPaterno">Apellido Paterno</label>
                  <input
                    id="apellidoPaterno"
                    type="text"
                    className="form-input"
                    value={formData.apellidoPaterno}
                    onChange={(e) => setFormData({ ...formData, apellidoPaterno: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="apellidoMaterno">Apellido Materno</label>
                  <input
                    id="apellidoMaterno"
                    type="text"
                    className="form-input"
                    value={formData.apellidoMaterno}
                    onChange={(e) => setFormData({ ...formData, apellidoMaterno: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="correo">Correo Electrónico</label>
                <input
                  id="correo"
                  type="email"
                  required
                  className="form-input"
                  value={formData.correo}
                  onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
                />
              </div>

              {modalMode === 'create' && (
                <div className="form-group">
                  <label className="form-label" htmlFor="contrasena">Contraseña</label>
                  <input
                    id="contrasena"
                    type="password"
                    required
                    className="form-input"
                    value={formData.contrasenaHash}
                    onChange={(e) => setFormData({ ...formData, contrasenaHash: e.target.value })}
                  />
                </div>
              )}

              <div className="form-group">
                <label className="form-label" htmlFor="rol">Rol del Colaborador</label>
                <select
                  id="rol"
                  className="form-select"
                  value={formData.idRol}
                  onChange={(e) => setFormData({ ...formData, idRol: e.target.value })}
                >
                  {roles.map((r) => (
                    <option key={r.idRol} value={r.idRol}>
                      {r.nombre}
                    </option>
                  ))}
                </select>
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
