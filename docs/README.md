# 📚 Documentación del Sistema — Álamo Rent-A-Car

Esta es la carpeta central de documentación del proyecto final para **Álamo Rent-A-Car**. Aquí encontrarás todas las guías y manuales técnicos y operativos del sistema estructurado bajo la arquitectura React (Frontend) y Spring Boot (Backend REST API).

---

## 🗂️ Índice de Documentos Disponibles

### 🛠️ [Manual del Desarrollador (Guía Técnica)](file:///c:/Users/ASUS/OneDrive%20-%20Universidad%20Tecnologica%20del%20Peru/Documents/Proyecto%20Universidad/Alamo-1/docs/manual_desarrollador.md)
* Guía de arquitectura de software desacoplada (SPA React + API Spring Boot).
* Descripción del modelo de persistencia y controladores genéricos.
* Explicación de la suite de pruebas unitarias (`JUnit 5` + `Mockito`) con mock de repositorios (`@MockitoBean`).
* Configuración del pipeline de Integración Continua (CI/CD) con GitHub Actions.
* Guía de despliegue dinámico en Vercel y Render con contenedores Docker.

### 👤 [Manual del Usuario (Guía Operativa)](file:///c:/Users/ASUS/OneDrive%20-%20Universidad%20Tecnologica%20del%20Peru/Documents/Proyecto%20Universidad/Alamo-1/docs/manual_usuario.md)
* Flujo de inicio de sesión y gestión basada en Roles (`ADMINISTRADOR` vs. `COUNTER`).
* Uso del Dashboard operativo con KPIs comerciales en tiempo real.
* Manual de registro y edición para la administración de colaboradores, vehículos y alquileres.
* Funcionamiento del calculador dinámico de tarifas en el formulario de contratos de alquiler.
* Instrucciones para descargar reportes consolidados en formatos **Excel** y **PDF**.
* Uso de la mesa de soporte técnico integrado para reportar y resolver incidentes.
### 📐 [Arquitectura de Datos y MER](file:///c:/Users/ASUS/OneDrive%20-%20Universidad%20Tecnologica%20del%20Peru/Documents/Proyecto%20Universidad/Alamo-1/docs/RENTACAR_ARQUITECTURA.md)
* Modelo Entidad-Relación (MER lógico y físico) detallando las tablas físicas, llaves primarias, llaves foráneas y mapeos JPA de persistencia.

---

## ⚡ Guía de Inicio Rápido en Desarrollo Local

### Requisitos Previos
* Java JDK 21
* Node.js v20 (con npm)
* Docker Desktop (opcional, para base de datos local)
* Maven 3.x

### Paso 1: Levantar Base de Datos (Local)
Si cuentas con Docker, inicia el contenedor PostgreSQL desde el directorio raíz:
```bash
docker compose -f docker/docker-compose.yml up -d --build
```
*Si prefieres base de datos física, asegúrate de ajustar las credenciales de puerto, usuario y contraseña en el archivo [application.properties](file:///c:/Users/ASUS/OneDrive%20-%20Universidad%20Tecnologica%20del%20Peru/Documents/Proyecto%20Universidad/Alamo-1/src/main/resources/application.properties).*

### Paso 2: Compilar y Ejecutar Pruebas Backend
En la raíz del proyecto, ejecuta:
```bash
mvn clean test
```
Para iniciar el servidor de Spring Boot (corre en `http://localhost:8080`):
```bash
mvn spring-boot:run
```

### Paso 3: Iniciar el Cliente Frontend (React)
Navega a la carpeta `frontend/`, instala dependencias e inicia el servidor de desarrollo:
```bash
cd frontend
npm install
npm run dev
```
La aplicación se abrirá en `http://localhost:5173`.
