import api from './api';

export interface CategoriaVehiculo {
  idCategoria: number;
  tipo: string;
  descripcion?: string;
}

export interface Vehiculo {
  idVehiculo?: number;
  placa: string;
  marca: string;
  modelo: string;
  anio: number;
  categoria: CategoriaVehiculo;
}

export const vehiculoService = {
  getAll: async (): Promise<Vehiculo[]> => {
    const response = await api.get<Vehiculo[]>('/vehiculos');
    return response.data;
  },

  getById: async (id: number): Promise<Vehiculo> => {
    const response = await api.get<Vehiculo>(`/vehiculos/${id}`);
    return response.data;
  },

  create: async (vehiculo: Vehiculo): Promise<Vehiculo> => {
    const response = await api.post<Vehiculo>('/vehiculos', vehiculo);
    return response.data;
  },

  update: async (id: number, vehiculo: Vehiculo): Promise<Vehiculo> => {
    const response = await api.put<Vehiculo>(`/vehiculos/${id}`, vehiculo);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/vehiculos/${id}`);
  },
};
