# 🎨 PARTE 4: FRONTEND Y INTERFAZ DE USUARIO

**Responsable:** Integrante 4

## Descripción
Esta parte contiene toda la interfaz visual del usuario:
- Templates HTML con Thymeleaf
- Estilos CSS
- JavaScript para interactividad
- Imágenes y recursos estáticos

## Archivos Principales

### Código Fuente
```
src/main/resources/templates/                # Templates HTML (Thymeleaf)
├── login.html                               # Página de login
├── menu.html                                # Menú principal
├── perfil.html                              # Perfil de usuario
├── usuarios.html                            # Gestión de usuarios
├── asignartareas.html                       # Asignación de tareas
├── mistareas.html                           # Mis tareas
├── historialdia.html                        # Historial diario
├── historialgeneral.html                    # Historial general
├── agenda.html                              # Agenda
├── reportes.html                            # Reportes
├── informes.html                            # Informes
├── listarproductos.html                     # Productos
├── listarservicios.html                     # Servicios
├── Horarios.html                            # Horarios
├── registrar.html                           # Registro de usuarios
├── contraseña.html                          # Cambiar contraseña
├── mobile.html                              # Vista móvil
└── fragmentos/
    └── sidebar.html                         # Barra lateral reutilizable

src/main/resources/static/                   # Recursos estáticos
├── index.html                               # Página de inicio
├── css/
│   └── asignartareas.css                    # Estilos CSS
│   └── style.css                            # Estilos generales
├── js/                                      # JavaScript
│   └── (scripts de funcionalidad)
└── img/                                     # Imágenes
```

## Tareas Principales

### ✅ Durante el desarrollo:
1. Crear/actualizar templates HTML con Thymeleaf
2. Diseñar estilos CSS responsivos
3. Implementar funcionalidades con JavaScript
4. Asegurar UX/UI consistente
5. Optimizar para móvil
6. Validaciones de cliente (JavaScript)

### 📝 Convenciones Thymeleaf

```html
<!-- Expresiones -->
<p th:text="${usuario.nombre}">Nombre</p>

<!-- Iteraciones -->
<tr th:each="usuario : ${usuarios}">
    <td th:text="${usuario.nombre}"></td>
</tr>

<!-- Condicionales -->
<div th:if="${usuario.activo}">
    <p>Usuario activo</p>
</div>

<!-- Links -->
<a th:href="@{/usuarios/{id}(id=${usuario.id})}">Ver</a>

<!-- Formularios -->
<form th:action="@{/usuarios}" th:object="${usuarioDTO}" method="post">
    <input type="text" th:field="*{nombre}" />
    <button type="submit">Guardar</button>
</form>
```

### 🎨 Estructura CSS

```css
/* Variables y paleta */
:root {
    --primary-color: #007bff;
    --secondary-color: #6c757d;
    --danger-color: #dc3545;
}

/* Componentes reutilizables */
.btn { ... }
.btn-primary { ... }
.card { ... }
.table { ... }
```

### 🔧 JavaScript - Funcionalidades Típicas

```javascript
// Validación de formularios
document.getElementById('form').addEventListener('submit', function(e) {
    if (!validateForm()) {
        e.preventDefault();
    }
});

// AJAX para acciones sin recargar
fetch('/api/usuarios/' + id, { method: 'DELETE' })
    .then(response => response.json())
    .then(data => { /* actualizar UI */ });
```

## 📱 Responsive Design
- Mobile-first approach
- Breakpoints: 576px, 768px, 992px, 1200px
- Usar Bootstrap o similar framework

## Commits Esperados
- `feat: create login and registration pages`
- `feat: create main menu and navigation`
- `feat: create usuario management UI`
- `feat: create asistencia tracking UI`
- `feat: add responsive CSS styling`
- `feat: add JavaScript validations and interactivity`
- `feat: create reportes and informes views`
- `feat: create mobile-friendly layout`

## Testing
- Probar en diferentes navegadores
- Probar responsividad (mobile, tablet, desktop)
- Validar accesibilidad

## Integración
Ver [INTEGRACION.md](../INTEGRACION.md) en la raíz del proyecto.
