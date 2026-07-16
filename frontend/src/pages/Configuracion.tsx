import React, { useState, useEffect } from 'react';
import { catalogoService } from '../services/catalogoService';
import api from '../services/api';
import { 
  Settings, 
  Database, 
  ShieldCheck, 
  Sparkles, 
  HelpCircle,
  Clock,
  Layers,
  Wrench,
  CheckCircle
} from 'lucide-react';
import './Configuracion.css';

export const Configuracion: React.FC = () => {
  const [categorias, setCategorias] = useState<any[]>([]);
  const [seguros, setSeguros] = useState<any[]>([]);
  const [servicios, setServicios] = useState<any[]>([]);
  const [horarios, setHorarios] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [seeding, setSeeding] = useState(false);
  const [seedSuccess, setSeedSuccess] = useState(false);

  const fetchCatalogos = async () => {
    setIsLoading(true);
    try {
      const [cats, segs, servs, hors] = await Promise.all([
        catalogoService.getCategorias().catch(() => []),
        catalogoService.getSeguros().catch(() => []),
        catalogoService.getServicios().catch(() => []),
        catalogoService.getHorarios().catch(() => []),
      ]);
      setCategorias(cats);
      setSeguros(segs);
      setServicios(servs);
      setHorarios(hors);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCatalogos();
  }, []);

  const handleSeedDatabase = async () => {
    setSeeding(true);
    setSeedSuccess(false);
    try {
      // 1. Seed Roles
      await api.post('/roles', { nombre: 'ADMINISTRADOR' }).catch(() => {});
      await api.post('/roles', { nombre: 'COUNTER' }).catch(() => {});

      // 2. Seed Categorías
      await api.post('/categorias-vehiculo', { tipo: 'ECONOMICA', descripcion: 'Autos pequeños de bajo consumo ideal para ciudad' }).catch(() => {});
      await api.post('/categorias-vehiculo', { tipo: 'ESTANDAR', descripcion: 'Sedanes y SUVs espaciosas para familias' }).catch(() => {});
      await api.post('/categorias-vehiculo', { tipo: 'PREMIUM', descripcion: 'Vehículos de lujo y de alta potencia' }).catch(() => {});

      // 3. Seed Seguros
      await api.post('/seguros', { nombre: 'Contra Todo Riesgo (Platinium)', activo: true }).catch(() => {});
      await api.post('/seguros', { nombre: 'Daños a Terceros (Básico)', activo: true }).catch(() => {});

      // 4. Seed Servicios
      await api.post('/servicios', { nombre: 'Silla para Bebé', descripcion: 'Silla de seguridad infantil homologada', precio: 35.00 }).catch(() => {});
      await api.post('/servicios', { nombre: 'Sistema de Navegación GPS', descripcion: 'Dispositivo GPS de alta precisión', precio: 20.00 }).catch(() => {});
      await api.post('/servicios', { nombre: 'Conductor Adicional', descripcion: 'Permiso para un conductor extra registrado', precio: 50.00 }).catch(() => {});

      // 5. Seed Horarios
      await api.post('/horarios', { dia: 'MONDAY', horaInicio: '08:00:00', horaFin: '18:00:00' }).catch(() => {});
      await api.post('/horarios', { dia: 'TUESDAY', horaInicio: '08:00:00', horaFin: '18:00:00' }).catch(() => {});
      await api.post('/horarios', { dia: 'WEDNESDAY', horaInicio: '08:00:00', horaFin: '18:00:00' }).catch(() => {});
      await api.post('/horarios', { dia: 'THURSDAY', horaInicio: '08:00:00', horaFin: '18:00:00' }).catch(() => {});
      await api.post('/horarios', { dia: 'FRIDAY', horaInicio: '08:00:00', horaFin: '18:00:00' }).catch(() => {});
      await api.post('/horarios', { dia: 'SATURDAY', horaInicio: '09:00:00', horaFin: '13:00:00' }).catch(() => {});

      // Obtener los roles y categorías recién creados de la base de datos para obtener sus IDs reales
      const rList = (await api.get('/roles')).data;
      const cList = (await api.get('/categorias-vehiculo')).data;

      const adminRol = rList.find((r: any) => r.nombre === 'ADMINISTRADOR') || rList[0];
      const counterRol = rList.find((r: any) => r.nombre === 'COUNTER') || rList[1] || adminRol;

      const econCat = cList.find((c: any) => c.tipo === 'ECONOMICA') || cList[0];
      const stdCat = cList.find((c: any) => c.tipo === 'ESTANDAR') || cList[1] || econCat;
      const premCat = cList.find((c: any) => c.tipo === 'PREMIUM') || cList[2] || econCat;

      // 6. Seed Usuarios (Colaboradores / Clientes)
      await api.post('/usuarios', {
        nombres: 'Franco Paolo',
        apellidoPaterno: 'García',
        apellidoMaterno: 'Urbano',
        correo: 'francopaolo.garciaurbano2@gmail.com',
        contrasenaHash: '12345678',
        rol: adminRol
      }).catch(() => {});

      await api.post('/usuarios', {
        nombres: 'Juan Carlos',
        apellidoPaterno: 'Pérez',
        apellidoMaterno: 'Gómez',
        correo: 'juan.perez@email.com',
        contrasenaHash: '123456',
        rol: counterRol
      }).catch(() => {});

      await api.post('/usuarios', {
        nombres: 'María Inés',
        apellidoPaterno: 'García',
        apellidoMaterno: 'López',
        correo: 'maria.garcia@email.com',
        contrasenaHash: '123456',
        rol: counterRol
      }).catch(() => {});

      const uList = (await api.get('/usuarios')).data;
      const seedClient1 = uList.find((u: any) => u.correo === 'juan.perez@email.com') || uList[0];
      const seedClient2 = uList.find((u: any) => u.correo === 'maria.garcia@email.com') || uList[1] || seedClient1;

      // 7. Seed Vehículos
      await api.post('/vehiculos', {
        placa: 'ABC-123',
        marca: 'Toyota',
        modelo: 'Corolla',
        anio: 2022,
        categoria: econCat
      }).catch(() => {});

      await api.post('/vehiculos', {
        placa: 'FGH-456',
        marca: 'Hyundai',
        modelo: 'Tucson',
        anio: 2023,
        categoria: stdCat
      }).catch(() => {});

      await api.post('/vehiculos', {
        placa: 'XYZ-789',
        marca: 'BMW',
        modelo: 'X5',
        anio: 2024,
        categoria: premCat
      }).catch(() => {});

      const vList = (await api.get('/vehiculos')).data;
      const seedCar1 = vList.find((v: any) => v.placa === 'ABC-123') || vList[0];
      const seedCar2 = vList.find((v: any) => v.placa === 'FGH-456') || vList[1] || seedCar1;
      const seedCar3 = vList.find((v: any) => v.placa === 'XYZ-789') || vList[2] || seedCar1;

      // 8. Seed Contratos de Alquiler
      if (seedClient1 && seedCar1) {
        // Enero
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-01',
          fechaInicio: '2026-01-05',
          fechaFin: '2026-01-10',
          montoTotal: 380.00,
          vehiculo: seedCar1,
          cliente: seedClient1,
          servicios: []
        }).catch(() => {});

        // Febrero
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-02',
          fechaInicio: '2026-02-12',
          fechaFin: '2026-02-18',
          montoTotal: 570.00,
          vehiculo: seedCar2,
          cliente: seedClient2,
          servicios: []
        }).catch(() => {});

        // Marzo
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-03',
          fechaInicio: '2026-03-02',
          fechaFin: '2026-03-08',
          montoTotal: 850.00,
          vehiculo: seedCar3,
          cliente: seedClient1,
          servicios: []
        }).catch(() => {});

        // Abril
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-04',
          fechaInicio: '2026-04-15',
          fechaFin: '2026-04-20',
          montoTotal: 620.00,
          vehiculo: seedCar2,
          cliente: seedClient2,
          servicios: []
        }).catch(() => {});

        // Mayo
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-05',
          fechaInicio: '2026-05-10',
          fechaFin: '2026-05-16',
          montoTotal: 1250.00,
          vehiculo: seedCar3,
          cliente: seedClient1,
          servicios: []
        }).catch(() => {});

        // Junio
        await api.post('/contratos-alquiler', {
          codigo: 'ALAMO-SEED-06',
          fechaInicio: '2026-06-01',
          fechaFin: '2026-06-05',
          montoTotal: 1550.00,
          vehiculo: seedCar3,
          cliente: seedClient2,
          servicios: []
        }).catch(() => {});
      }

      setSeedSuccess(true);
      fetchCatalogos();
    } catch (e) {
      alert('Ocurrió un error al poblar la base de datos.');
    } finally {
      setSeeding(false);
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div className="header-text">
          <h1>Configuración del Sistema</h1>
          <p>Consulta catálogos de metadatos del negocio y realiza tareas de administración del sistema.</p>
        </div>
      </div>

      <div className="config-layout">
        {/* Seeder Box */}
        <div className="config-sidebar-card card">
          <div className="card-header-icon">
            <Database size={24} className="icon-blue" />
            <h3>Herramientas de Datos</h3>
          </div>
          <p className="card-desc">
            Si acabas de levantar el proyecto o la base de datos está vacía, usa este botón para poblar automáticamente los roles, categorías, seguros y servicios de prueba.
          </p>
          <button 
            className="btn btn-primary seed-btn" 
            onClick={handleSeedDatabase} 
            disabled={seeding}
          >
            {seeding ? (
              <span className="spinner"></span>
            ) : seedSuccess ? (
              <>
                <CheckCircle size={16} />
                <span>Base Poblada con Éxito</span>
              </>
            ) : (
              <>
                <Sparkles size={16} />
                <span>Poblar Base de Datos (Seeds)</span>
              </>
            )}
          </button>
        </div>

        {/* Catalogos Details */}
        <div className="config-main">
          {isLoading ? (
            <div className="loader-container">
              <div className="spinner"></div>
            </div>
          ) : (
            <div className="catalogos-sections">
              {/* Categorías */}
              <div className="catalogo-card card">
                <div className="section-title">
                  <Layers size={18} className="icon-blue" />
                  <h4>Categorías de Vehículos ({categorias.length})</h4>
                </div>
                {categorias.length === 0 ? (
                  <p className="empty-catalog">No hay categorías registradas.</p>
                ) : (
                  <div className="catalog-list">
                    {categorias.map(c => (
                      <div key={c.idCategoria} className="catalog-item">
                        <span className="catalog-item-title">{c.tipo}</span>
                        <p className="catalog-item-desc">{c.descripcion || 'Sin descripción.'}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Seguros */}
              <div className="catalogo-card card">
                <div className="section-title">
                  <ShieldCheck size={18} className="icon-blue" />
                  <h4>Seguros Coberturas ({seguros.length})</h4>
                </div>
                {seguros.length === 0 ? (
                  <p className="empty-catalog">No hay seguros registrados.</p>
                ) : (
                  <div className="catalog-list">
                    {seguros.map(s => (
                      <div key={s.idSeguro} className="catalog-item flex-between">
                        <span className="catalog-item-title">{s.nombre}</span>
                        <span className={`badge ${s.activo ? 'badge-success' : 'badge-danger'}`}>
                          {s.activo ? 'ACTIVO' : 'INACTIVO'}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Servicios Adicionales */}
              <div className="catalogo-card card">
                <div className="section-title">
                  <Wrench size={18} className="icon-blue" />
                  <h4>Servicios Adicionales ({servicios.length})</h4>
                </div>
                {servicios.length === 0 ? (
                  <p className="empty-catalog">No hay servicios registrados.</p>
                ) : (
                  <div className="catalog-list">
                    {servicios.map(s => (
                      <div key={s.idServicio} className="catalog-item flex-between">
                        <div>
                          <span className="catalog-item-title">{s.nombre}</span>
                          <p className="catalog-item-desc">{s.descripcion || 'Sin descripción.'}</p>
                        </div>
                        <span className="catalog-item-price">S/. {Number(s.precio).toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Horarios */}
              <div className="catalogo-card card">
                <div className="section-title">
                  <Clock size={18} className="icon-blue" />
                  <h4>Horarios de Operación ({horarios.length})</h4>
                </div>
                {horarios.length === 0 ? (
                  <p className="empty-catalog">No hay horarios registrados.</p>
                ) : (
                  <div className="catalog-list grid-cols-2">
                    {horarios.map(h => (
                      <div key={h.idHorario} className="catalog-item">
                        <span className="catalog-item-title">{h.dia}</span>
                        <p className="catalog-item-desc">{h.horaInicio} - {h.horaFin}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
