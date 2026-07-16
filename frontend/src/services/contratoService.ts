import api from './api';
import { Vehiculo } from './vehiculoService';
import { Usuario } from './usuarioService';

export interface Horario {
  idHorario?: number;
  dia: string;
  horaInicio: string;
  horaFin: string;
}

export interface Seguro {
  idSeguro?: number;
  nombre: string;
  activo: boolean;
}

export interface ContratoSeguro {
  idContratoSeguro?: number;
  descripcion?: string;
}

export interface Servicio {
  idServicio?: number;
  nombre: string;
  descripcion?: string;
  precio: number;
}

export interface ContratoAlquiler {
  idContrato?: number;
  codigo: string;
  fechaInicio: string;
  fechaFin: string;
  montoTotal: number;
  vehiculo: Vehiculo;
  cliente: Usuario;
  horario?: Horario;
  seguro?: Seguro;
  contratoSeguro?: ContratoSeguro;
  servicios: Servicio[];
  estado?: string;
}

export const contratoService = {
  getAll: async (): Promise<ContratoAlquiler[]> => {
    const response = await api.get<ContratoAlquiler[]>('/contratos-alquiler');
    return response.data;
  },

  getById: async (id: number): Promise<ContratoAlquiler> => {
    const response = await api.get<ContratoAlquiler>(`/contratos-alquiler/${id}`);
    return response.data;
  },

  create: async (contrato: ContratoAlquiler): Promise<ContratoAlquiler> => {
    const response = await api.post<ContratoAlquiler>('/contratos-alquiler', contrato);
    return response.data;
  },

  update: async (id: number, contrato: ContratoAlquiler): Promise<ContratoAlquiler> => {
    const response = await api.put<ContratoAlquiler>(`/contratos-alquiler/${id}`, contrato);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/contratos-alquiler/${id}`);
  },
};
