# 🏢 ÁLAMO - Sistema de Alquiler de Vehículos (Premium Car Rental)

**Versión:** 2.0  
**Estado:** Proyecto Consolidado, Responsivo y Completamente Funcional  
**Tecnología:** Java 21 | Spring Boot 3.4.1 | React 18 | TypeScript | Vite | MySQL | Docker

---

## 📋 Descripción General

**ÁLAMO** es un sistema web premium diseñado para la gestión integral de alquileres de vehículos. Cuenta con un diseño Glassmorphism responsivo para celulares y escritorio, analíticas gráficas dinámicas y lógica de negocios automatizada en el backend.

### Características Clave:
* **📊 Analíticas & KPI Dashboard:** Gráficos interactivos de facturación mensual e histórico de flota por categoría utilizando `recharts`.
* **📑 Gestión de Contratos de Alquiler:** Registro y edición dinámica de alquileres, con asignación de seguros, horarios, servicios adicionales y cálculo automático de montos diarios.
* **🔍 Filtro de Disponibilidad de Autos:** Algoritmo en base de datos que detecta traslapes de reservas para excluir autos ya rentados en las fechas ingresadas.
* **🔔 Mesa de Notificaciones en Tiempo Real:** Alertas instantáneas y registro en la base de datos ante creación, edición o eliminación de cualquier entidad.
* **🛠️ Incidencias & Soporte:** Panel de control de incidencias (Tickets de soporte) para los clientes, con panel de purga de incidencias para administradores.
* **👥 Gestión de Usuarios y Flota:** CRUDs detallados con insignias dinámicas para tipos de licencias, placas de vehículos y roles del sistema.
* **📥 Reportes Multi-Formato:** Descarga instantánea de reportes de contratos en formato Excel y PDF.

---

## 🏗️ Estructura del Repositorio

```
alamo-proyecto/
├── src/                                 # CÓDIGO DEL BACKEND (SPRING BOOT)
│   ├── main/
│   │   ├── java/com/alamo/alquiler/
│   │   │   ├── 📁 controller/          # Endpoints REST (Contratos, Notificaciones, Analíticas)
│   │   │   ├── 📁 model/               # Entidades JPA (Vehiculo, Usuario, ContratoAlquiler)
│   │   │   ├── 📁 repository/          # Consultas JPA y algoritmos SQL
│   │   │   ├── 📁 service/             # Lógica transaccional de negocio
│   │   │   └── AlamoAlquilerApplication.java
│   │   └── resources/
│   │       └── application.properties   # Configuración de base de datos
│   └── test/                            # Suite de Pruebas Unitarias (14 Tests JUnit)
│
├── frontend/                            # CÓDIGO DEL FRONTEND (REACT + TS)
│   ├── src/
│   │   ├── 📁 components/               # Layout responsivo (Navbar, Sidebar)
│   │   ├── 📁 pages/                    # Vistas (Dashboard, Contratos, Soporte, Usuarios)
│   │   └── 📁 services/                 # Clientes API Axios
│   ├── package.json
│   └── vite.config.ts
│
├── docker/                              # CONFIGURACIÓN DOCKER
│   └── docker-compose.yml               # Orquestación MySQL local
│
└── docs/                                # DOCUMENTACIÓN TÉCNICA CENTRAL
    ├── README.md                        # Índice de documentación
    ├── RENTACAR_ARQUITECTURA.md         # Modelo Entidad-Relación y de datos
    ├── manual_desarrollador.md          # Guía técnica para desarrolladores
    └── manual_usuario.md                # Guía de uso de la interfaz
```

---

## 🛠️ Guía de Inicio Rápido

### 1. Requisitos Previos
* Java JDK 21
* Node.js v18+ (con npm)
* MySQL Server o Docker Desktop

### 2. Levantar la Base de Datos (Docker)
Si dispones de Docker, puedes levantar la base de datos localmente ejecutando:
```bash
docker-compose -f docker/docker-compose.yml up -d
```

### 3. Ejecutar el Backend (Spring Boot)
Desde la raíz del proyecto:
```bash
mvn spring-boot:run
```
El servidor backend se iniciará en `http://localhost:8080`.

### 4. Ejecutar el Frontend (React)
Navega al directorio `/frontend`, instala las dependencias y corre el servidor de desarrollo:
```bash
cd frontend
npm install
npm run dev
```
La aplicación web se abrirá en `http://localhost:5173`.

---

## 🧪 Pruebas Unitarias
El backend cuenta con tests JUnit que garantizan la estabilidad del sistema:
```bash
mvn test
```

---

## 📄 Licencia y Autores
Proyecto académico universitario consolidado para **Álamo Rent-A-Car**. Todos los derechos reservados.
