# Plan de Implementación: Diseño Responsivo (Mobile-First)

Este documento detalla el plan de diseño técnico para adaptar la interfaz premium de **Álamo Rent-A-Car** a dispositivos móviles (celulares y tablets), asegurando una experiencia táctil fluida y profesional.

---

## 📱 1. Estrategia de Layout Global (Sidebar & Navbar)

El principal reto en móviles es la barra lateral (`Sidebar`). En pantallas de escritorio se mantiene fija a la izquierda, pero en celulares debe colapsar.

### ☰ A. Hamburguesa y Sidebar Desplegable
* **Estado de React:** Añadiremos un estado booleano global o en el componente Layout (`isSidebarOpen`) para abrir/cerrar el Sidebar en móviles.
* **Comportamiento CSS (Media Queries):**
  * Pantallas **> 768px** (Desktop): Sidebar fijo a la izquierda (ancho regular).
  * Pantallas **<= 768px** (Móvil): 
    * El Sidebar se convierte en un panel flotante (`position: fixed; left: -250px; z-index: 1000`).
    * Al activar `isSidebarOpen = true`, se traslada a la posición visible (`left: 0`) con una transición suave (`transition: transform 0.3s ease`).
    * Se añade un fondo oscuro translúcido (overlay) para cerrar el menú haciendo clic fuera.

### 🔔 B. Compactación del Header (Navbar)
En móviles, el Navbar tiene demasiado texto (reloj, fecha, estado online).
* **Adaptación Móvil:**
  * Ocultar el widget de hora y fecha en pantallas `< 768px` (`display: none`).
  * Desplegar el botón del menú hamburguesa en el extremo izquierdo.
  * Mantener únicamente el título de la página actual y la campana de notificaciones.

---

## 📦 2. Reestructuración de Páginas y Grillas (Grids Responsivos)

Adaptar los componentes contenedores de flexbox/grid para apilarse verticalmente en pantallas angostas.

### 📊 A. Dashboard y Analíticas
* **Tarjetas de KPI (`stats-grid`):** Cambiar de 4 columnas a 2 columnas en tablets, y a 1 sola columna en móviles.
  ```css
  @media (max-width: 600px) {
    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
  ```
* **Gráficos Recharts:** Ajustar la altura a `200px` y asegurarse de usar `<ResponsiveContainer width="100%">` para evitar desbordes horizontales de SVG.

### 📋 B. Formularios y Modales
* **Filas Divididas (`.form-row.split`):** En escritorio se muestran en dos columnas. En móvil, se apilarán en una columna.
  ```css
  @media (max-width: 768px) {
    .form-row.split {
      flex-direction: column;
      gap: 12px;
    }
  }
  ```
* **Modales:** Configurar un ancho máximo del `95%` con scroll interno (`overflow-y: auto`) para que quepa en pantallas táctiles verticales sin cortarse.

---

## 🗂️ 3. Adaptación de Tablas de Datos (Responsividad de Tablas)

Las tablas de datos (Usuarios, Vehículos, Contratos) tienden a desbordarse horizontalmente en móviles. Se aplicarán dos estrategias:

### Estrategia A: Contenedor con Desplazamiento (Scroll Horizontal)
Envolver la tabla en un div con propiedad `overflow-x: auto`:
```css
.table-container {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch; /* Scroll suave en iOS */
}
```

### Estrategia B: Conversión Tabla-a-Tarjeta (Recomendado para Móviles)
Mediante CSS, convertir la tabla en un listado de tarjetas táctiles en pantallas angostas (`max-width: 600px`):
```css
@media (max-width: 600px) {
  /* Ocultar la cabecera de la tabla */
  .custom-table thead {
    display: none;
  }
  /* Convertir cada fila en una tarjeta individual */
  .custom-table tr {
    display: block;
    margin-bottom: 15px;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 10px;
    background-color: var(--card-bg);
  }
  /* Mostrar cada celda con etiqueta de bloque */
  .custom-table td {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid rgba(255,255,255,0.05);
  }
}
```

---

## 🗺️ Roadmap de Tareas

| Paso | Componente / Archivo | Cambio a Realizar |
| :--- | :--- | :--- |
| **1** | `index.css` / Variables | Declarar breakpoints responsivos estándar (`--tablet: 768px`, `--mobile: 480px`). |
| **2** | `Layout.tsx` | Añadir lógica de estado para toggle de Sidebar y overlay. |
| **3** | `Navbar.tsx` & `Sidebar.tsx` | Integrar botón hamburguesa y clases condicionales `.open` / `.closed`. |
| **4** | `Contratos.tsx` & `Usuarios.tsx` | Envolver tablas en contenedores responsivos y ajustar grid de filtros. |
| **5** | `Dashboard.tsx` | Reestructurar grillas de analíticas para colapsar en una columna. |
