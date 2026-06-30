import React, { createContext, useContext, useState, useEffect } from 'react';

export interface UserSession {
  username: string;
  nombre: string;
  rol: 'ADMINISTRADOR' | 'COUNTER';
}

interface AuthContextType {
  user: UserSession | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserSession | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('alamo_session');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('alamo_session');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, contrasenia: string): Promise<boolean> => {
    setIsLoading(true);
    console.log("Iniciando sesión para:", username, "con hash contrasenia:", contrasenia.length);
    // Simulación de validación local de credenciales (backend no tiene Spring Security)
    // Pero permitimos cualquier DNI/contraseña con roles basados en el username
    await new Promise((resolve) => setTimeout(resolve, 800)); // Simula latencia de red

    let session: UserSession | null = null;
    const lowerUser = username.toLowerCase().trim();

    if (lowerUser === 'admin' || lowerUser.includes('admin')) {
      session = {
        username: username,
        nombre: 'Administrador del Sistema',
        rol: 'ADMINISTRADOR',
      };
    } else {
      session = {
        username: username,
        nombre: 'Agente Counter Álamo',
        rol: 'COUNTER',
      };
    }

    setUser(session);
    localStorage.setItem('alamo_session', JSON.stringify(session));
    setIsLoading(false);
    return true;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('alamo_session');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
