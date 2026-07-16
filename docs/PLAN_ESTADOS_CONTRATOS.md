# Plan de Implementación: Estados de Contratos y Búsqueda Avanzada

Este documento detalla el plan para añadir gestión de estados (Activo, Finalizado, Cancelado) y búsqueda interactiva en el módulo de Contratos.

Para ayudar a incrementar el historial de Pull Requests (PR) en tu cuenta de GitHub, dividiremos el trabajo en **dos fases independientes**. Cada fase se desarrollará en su propia rama de Git para que puedas abrir dos PRs por separado.

---

## 📍 Fase 1 (PR #1): Barra de Búsqueda y Filtros de Contratos (Frontend)
* **Rama propuesta:** `feat/busqueda-contratos`
* **Objetivo:** Permitir al usuario filtrar la lista de contratos en tiempo real desde la interfaz de forma interactiva.

### Cambios en Código:
1. **`Contratos.tsx` (Filtros en React):**
   * Añadir campos de estado en el componente para `searchTerm` (búsqueda por código de contrato, nombre de cliente o placa) y `filterVehiculo` (filtro por modelo de auto).
   * Implementar la lógica de filtrado reactiva en la constante `filteredContratos` antes de renderizar la tabla.
   * Añadir una barra de entrada (`<input>`) con ícono de lupa (`Search`) y un selector dropdown de vehículos en la sección superior de la página.

---

## 📍 Fase 2 (PR #2): Gestión de Estados de Contrato (Backend & Frontend)
* **Rama propuesta:** `feat/estados-contratos`
* **Objetivo:** Reemplazar el borrado físico (DELETE) de contratos por un cambio de estado ("Activo", "Finalizado", "Rescindido"), añadiendo insignias visuales (badges) de colores.

### Cambios en Código:
1. **`ContratoAlquiler.java` (JPA Entity):**
   * Añadir el campo `@Column(name = "estado") private String estado;` (por defecto "ACTIVO" al crearse).
2. **`ContratoAlquilerController.java` (Backend):**
   * Modificar el endpoint de cancelación/rescisión para que en lugar de eliminar el registro de la base de datos, actualice el campo `estado` a `"RESCINDIDO"` o `"FINALIZADO"`.
3. **`Contratos.tsx` (Frontend):**
   * Añadir la columna "Estado" en la tabla de contratos.
   * Renderizar insignias de colores dinámicas (`badge-success` para ACTIVO, `badge-danger` para RESCINDIDO, `badge-neutral` para FINALIZADO).
   * Ajustar la lógica del botón de cancelación para actualizar el estado a través del API en lugar de eliminar el registro físico.

---

## 🚀 Workflow recomendado para crear los PRs en GitHub:

### Para la Fase 1 (PR #1):
1. Crear y cambiar a la rama de búsqueda:
   ```bash
   git checkout -b feat/busqueda-contratos
   ```
2. Aplicar los cambios en el frontend.
3. Subir la rama a GitHub:
   ```bash
   git push origin feat/busqueda-contratos
   ```
4. Entrar a GitHub y abrir el **Pull Request #1** de `feat/busqueda-contratos` hacia `main`. Fusionar el PR una vez aprobado.

### Para la Fase 2 (PR #2):
1. Regresar a `main`, descargar la fusión del PR #1 y crear la rama de estados:
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feat/estados-contratos
   ```
2. Aplicar los cambios en el backend y frontend.
3. Subir la rama a GitHub:
   ```bash
   git push origin feat/estados-contratos
   ```
4. Abrir el **Pull Request #2** de `feat/estados-contratos` hacia `main` en GitHub.
