import api from './api';
import { Rol } from './usuarioService';
import { CategoriaVehiculo } from './vehiculoService';
import { Seguro, Servicio, Horario, ContratoSeguro } from './contratoService';

export const catalogoService = {
  getRoles: async (): Promise<Rol[]> => {
    const response = await api.get<Rol[]>('/roles');
    return response.data;
  },

  getCategorias: async (): Promise<CategoriaVehiculo[]> => {
    const response = await api.get<CategoriaVehiculo[]>('/categorias-vehiculo');
    return response.data;
  },

  getSeguros: async (): Promise<Seguro[]> => {
    const response = await api.get<Seguro[]>('/seguros');
    return response.data;
  },

  getServicios: async (): Promise<Servicio[]> => {
    const response = await api.get<Servicio[]>('/servicios');
    return response.data;
  },

  getHorarios: async (): Promise<Horario[]> => {
    const response = await api.get<Horario[]>('/horarios');
    return response.data;
  },

  getContratosSeguro: async (): Promise<ContratoSeguro[]> => {
    const response = await api.get<ContratoSeguro[]>('/contratos-seguro');
    return response.data;
  },
};
