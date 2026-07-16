# TODO - Nuevo backend alquiler

## Paso 1: Limpieza
- [x] Eliminar backend viejo `com.alamo.asistencia`

## Paso 2: Nuevo módulo/arranque
- [x] Crear `AlamoAlquilerApplication`
- [x] Crear CORS en `com.alamo.alquiler.config`

## Paso 3: Modelo (13 entidades)
- [x] Crear entidades en `com.alamo.alquiler.model`

## Paso 4: Persistencia
- [x] Crear repositorios en `com.alamo.alquiler.repository` (13)

## Paso 5: API REST CRUD
- [x] Crear `AbstractCrudController`
- [x] Crear controllers REST CRUD para: 
  - [x] Usuario
  - [x] Rol
  - [x] Vehiculo
  - [x] CategoriaVehiculo
  - [x] ContratoAlquiler
  - [x] Horario
  - [x] Seguro
  - [x] ContratoSeguro
  - [x] Servicio
  - [x] InspeccionVehiculo
  - [x] Pago
  - [x] Informe
  - [x] Actividad

## Paso 6: Validación
- [x] Ajustar `application.properties` para PostgreSQL
- [x] Ejecutar `mvn test` y/o `mvn spring-boot:run` (en tu entorno local o Jenkins)
- [x] Verificar endpoints con Postman/curl


