# ALAMO Rent-A-Car - Arquitectura de Expansion

## Objetivo

Evolucionar ALAMO desde un monolito MVC + REST de gestion interna hacia una plataforma de reserva y alquiler de autos para Peru, manteniendo intactos los modulos de asistencia, horarios y usuarios internos.

## Separacion de dominios

- `tb_usuario`: personal interno de ALAMO. Agentes, administradores e inspectores. Sigue controlando asistencia, horarios, turnos y permisos.
- `tb_cliente`: turista o arrendatario. No reemplaza a `tb_usuario`; permite separar analitica comercial de gestion humana.
- `tb_contacto`: lead o prospecto previo a formalizar el contrato.
- `tb_pago_garantia`: nueva fuente transaccional para ingresos de alquiler.
- `tb_servicio_adicional`: evolucion funcional para extras del contrato, como GPS, silla de bebe o conductor adicional.

## MER logico

```text
tb_categoria_vehiculo 1 --- N tb_vehiculo
    @OneToMany CategoriaVehiculo.vehiculos
    @ManyToOne Vehiculo.categoria

tb_cliente 1 --- N tb_contrato_alquiler
    @OneToMany Cliente.contratos
    @ManyToOne ContratoAlquiler.cliente

tb_usuario 1 --- N tb_contrato_alquiler
    @ManyToOne ContratoAlquiler.agente
    Uso: agente interno que atiende, vende y despacha.

tb_vehiculo 1 --- N tb_contrato_alquiler
    @OneToMany Vehiculo.contratos
    @ManyToOne ContratoAlquiler.vehiculo

tb_contrato_alquiler 1 --- 1 tb_pago_garantia
    @OneToOne ContratoAlquiler.pagoGarantia
    @OneToOne PagoGarantia.contrato

tb_contrato_alquiler 1 --- N tb_inspeccion_vehiculo
    @OneToMany ContratoAlquiler.inspecciones
    @ManyToOne InspeccionVehiculo.contrato

tb_contrato_alquiler N --- N tb_seguro
    Implementado con tb_contrato_seguro.
    @OneToMany ContratoAlquiler.segurosContratados
    @OneToMany Seguro.contratos
    @ManyToOne ContratoSeguro.contrato
    @ManyToOne ContratoSeguro.seguro

tb_contrato_alquiler 1 --- N tb_servicio_adicional
    @OneToMany ContratoAlquiler.serviciosAdicionales
    @ManyToOne ServicioAdicional.contrato
```

## Entidades agregadas

- `CategoriaVehiculo`: catalogo restringido a `ECONOMICA`, `STANDAR`, `PREMIUM`; contiene tarifa base diaria.
- `Vehiculo`: placa, marca, modelo, anio, color, kilometraje y estado operativo.
- `Cliente`: documentos del arrendatario, licencia, pais de origen y datos de contacto.
- `ContratoAlquiler`: eje transaccional; relaciona cliente, agente interno y vehiculo.
- `PagoGarantia`: monto de alquiler, metodo de pago, garantia bloqueada y referencia de pasarela.
- `InspeccionVehiculo`: checklist de entrega/devolucion, combustible, kilometraje, danos y firma.
- `Seguro`: catalogo de coberturas.
- `ContratoSeguro`: tabla intermedia para seguros contratados.
- `ServicioAdicional`: extras vinculados al contrato.

## Controladores propuestos

### MVC Thymeleaf

- `GET /rentacar`: dashboard operativo para counter.
- `GET /rentacar/contratos/nuevo`: pantalla de formalizacion.
- `GET /rentacar/inspecciones`: pantalla de despacho/devolucion.

Estas rutas estan preparadas en `RentacarController`; los templates se deben crear en `resources/templates/rentacar/`.

### API REST

- `GET /api/rentacar/categorias`
- `GET /api/rentacar/vehiculos/disponibles?categoria=ECONOMICA&pickup=2026-06-02T10:00:00&dropoff=2026-06-05T10:00:00`
- `POST /api/rentacar/clientes`
- `POST /api/rentacar/contratos`
- `POST /api/rentacar/pagos-garantias`
- `POST /api/rentacar/inspecciones`
- `GET /api/rentacar/seguros`

## Workflow operativo

1. El agente marca asistencia con el flujo actual.
2. El agente consulta vehiculos disponibles por categoria y rango de fechas.
3. Se registra o actualiza el cliente, separando turista de empleado.
4. Se crea el contrato en estado `PENDIENTE`.
5. Se registra el pago y bloqueo de garantia.
6. Se genera la inspeccion de `ENTREGA`; el contrato pasa a `ACTIVO` y el vehiculo a `ALQUILADO`.
7. Al retorno, se genera la inspeccion de `DEVOLUCION`.
8. Si no hay cargos, la garantia pasa a `LIBERADO`, el contrato a `FINALIZADO` y el vehiculo a `DISPONIBLE`.
9. Si hay danos o penalidades, la garantia puede quedar `RETENIDO` hasta conciliacion.

## Hitos de implementacion

1. **Modelo y persistencia**: entidades JPA, enums y repositorios. Ya agregados sin modificar las tablas actuales.
2. **Seeds/catalogos**: registrar categorias base, seguros base y algunos vehiculos iniciales.
3. **Servicios de negocio**: mover la logica transaccional a `service`, especialmente calculo de monto, disponibilidad, activacion, devolucion y liberacion de garantia.
4. **Templates Thymeleaf**: crear vistas `rentacar/dashboard.html`, `rentacar/contrato-form.html` e `rentacar/inspecciones.html`.
5. **PDF operativo**: usar iText 8 para exportar contrato y checklist desde `ContratoAlquiler` + `InspeccionVehiculo`.
6. **CRM de leads**: conectar `tb_contacto` con cotizaciones web y conversion a `tb_cliente`.
7. **Reporteria financiera**: construir reportes desde `tb_pago_garantia`, manteniendo `tb_ingreso` como historico o resumen contable.
8. **Seguridad por rol**: restringir operaciones de contrato, pago e inspeccion segun rol de `tb_usuario`.

## Reglas de integracion cuidadosa

- No mezclar clientes con usuarios internos.
- No eliminar `Producto`, `Servicio` ni `Ingreso` hasta migrar reportes existentes.
- Usar `BigDecimal` para dinero.
- Usar enums persistidos como `STRING` para estados y catalogos.
- Mantener `spring.jpa.hibernate.ddl-auto=update` solo en desarrollo; para produccion conviene migrar a scripts versionados.
