# Plataforma Proveedor: diseño

Extensión de SodApp desde el nivel "un sodero" al nivel **proveedor** (planta o distribuidora): un **panel web** para el dueño y el encargado, y la **app actual** para los repartidores. Permite el control centralizado de las jornadas de los repartidores empleados, el inventario de envases y la cuenta corriente de los repartidores independientes, manteniendo la privacidad de los datos de cada cuenta.

> Estado: **diseño aprobado en conversación, sin implementar**. Fecha: 2026-10-21.

## Resumen de decisiones

| Tema | Decisión |
|------|----------|
| Dueño de los datos | Todo dato pertenece a una **Cuenta** (`empresa_id`). Los usuarios operan dentro de cuentas según su rol. |
| Repartidor empleado | Opera dentro de la cuenta del proveedor. Sus clientes, ventas y deudas son del proveedor. |
| Repartidor independiente | Tiene su propia cuenta, **100% privada**: el proveedor no ve nada, ni siquiera totales. A su vez es un **cliente mayorista** dentro de la cuenta del proveedor. |
| Vista del independiente (v1) | **Solo lectura** de su deuda y sus movimientos con el proveedor, mediante un vínculo creado por el proveedor. |
| Jornada del empleado | Apertura con carga (web), recorrido (app), cierre con conteo (web). Se concilian producto, envases y dinero. |
| Diferencias de cierre | **Solo se registran**. No generan deuda automática al empleado; se resuelven en privado. |
| Envases | Saldo por cliente y por tipo de envase, calculado desde un **registro de movimientos**. Los faltantes se clasifican como en cliente, fuera de servicio o perdido. |
| Roles | RBAC por cuenta. El código valida **permisos**, no nombres de rol. Validación en la API. |
| Conteo de cierre | **A ciegas**: quien cuenta no ve lo esperado ni la diferencia. |
| Permisos configurables | El dueño activa o desactiva permisos configurables por rol (por ejemplo, que el encargado reciba la rendición de dinero). Los permisos críticos quedan bloqueados para el dueño. |

## Modelo conceptual

```
Cuenta PROVEEDOR (empresa_id = P)
 ├── Usuarios: Dueño (web), Encargado (web), Empleados (app)
 ├── Clientes propios ─ deuda, saldo de envases
 ├── Clientes mayoristas ─ incluye a cada independiente
 ├── Jornadas de empleados
 └── Inventario de envases

Cuenta INDEPENDIENTE (empresa_id = I)       ← privada
 ├── Usuario: el independiente (app)
 └── Sus clientes, ventas y deudas

Vínculo:  I ──► cliente mayorista #N en P   (solo lectura, creado por P)
```

**Regla de oro:** un usuario solo accede a los datos de las cuentas donde tiene una membresía, y dentro de ellas solo a lo que permiten sus permisos. `empresa_id` y los permisos **siempre** se resuelven en el servidor a partir del JWT validado, nunca desde parámetros de la request.

## Roles y permisos

Los roles son paquetes de permisos por cuenta. Cada endpoint exige un permiso concreto.

| Permiso | Dueño | Encargado | Empleado (app) | Configurable |
|---------|:-:|:-:|:-:|:-:|
| `jornada.abrir` (registrar carga) | ✅ | ✅ | ❌ | sí |
| `cierre.contar` (producto y envases) | ✅ | ✅ | ❌ | sí |
| `cierre.recibir_dinero` | ✅ | ❌ por defecto | ❌ | **sí** |
| `cierre.ver_diferencias` | ✅ | ❌ | ❌ | bloqueado |
| `clientes.ver` | ✅ | ✅ | solo asignados | sí |
| `clientes.ver_deuda` | ✅ | ❌ por defecto | solo asignados | **sí** |
| `envases.gestionar` (bajas, ajustes) | ✅ | ✅ | ❌ | sí |
| `mayoristas.gestionar` | ✅ | ❌ | ❌ | sí |
| `reportes.ver` | ✅ | ❌ | ❌ | sí |
| `venta.registrar`, `deuda.cobrar` | ✅ | ❌ | ✅ | sí |
| `usuarios.gestionar`, `roles.gestionar`, `cuenta.configurar` | ✅ | ❌ | ❌ | bloqueado |

Reglas:

1. **Los permisos bloqueados no se pueden asignar a otros roles.** Así se evita la escalada de privilegios (un encargado que se otorga permisos a sí mismo).
2. **Ocultar en la UI no es seguridad.** Toda la validación ocurre en la API (`require_permission('cierre.recibir_dinero')`).
3. Hoy `login.php` infiere el rol a partir de columnas variables (`role`, `rol`, `us_rol`, …). Ese mecanismo se reemplaza por membresías explícitas.

## Jornada del repartidor empleado

```
APERTURA (web)            RECORRIDO (app)               CIERRE (web)
Encargado/Dueño           Empleado                      Encargado/Dueño
registra la carga   →     ventas, cobros de deuda,  →   conteo a ciegas de llenos,
                          envases entregados/retirados  vacíos y efectivo
```

Estados: `abierta` → `en_cierre` → `pendiente_dinero` (si nadie con `cierre.recibir_dinero` registró el efectivo) → `cerrada`.

### Conciliación al cierre

| Control | Esperado (sistema) | Real (conteo) | Diferencia |
|---------|--------------------|---------------|-----------|
| Producto | cargado − vendido | llenos que vuelven | real − esperado |
| Envases | Σ vacíos retirados en la jornada | vacíos que vuelven | se clasifica |
| Dinero | Σ cobrado en ventas + Σ cobros de deuda | efectivo rendido | real − esperado |

- Lo esperado y la diferencia solo los ve quien tiene `cierre.ver_diferencias`.
- Al cerrar se guarda una **foto** de lo esperado, lo real y la diferencia, para que el cálculo no cambie si después se corrige algún dato.
- Un faltante de envases se clasifica explícitamente en **en cliente**, **fuera de servicio** o **perdido**. Lo que no volvió no se da por perdido automáticamente.

### Varias jornadas por día

Una jornada representa **una salida** del camión, no un día calendario. Un empleado puede hacer varias salidas por día (por ejemplo, vuelve a mediodía, recarga y sale de nuevo).

- **Una sola jornada abierta a la vez por repartidor.** Así cada venta, cobro y movimiento de envases pertenece sin ambigüedad a una jornada. La API rechaza abrir una segunda jornada si hay otra sin cerrar.
- Cada salida tiene su propia carga, su propio cierre a ciegas y su propia conciliación.
- **Arrastre opcional:** los llenos contados en el cierre de una salida pueden pasar directamente como parte de la carga de la siguiente, sin volver a descargarlos.
- El reporte diario del repartidor suma todas sus jornadas del día.

## Envases

Tipos de envase por cuenta (por ejemplo, Bidón 20L o Sifón 1,5L). Cada producto que viaja en un envase retornable apunta a su tipo de envase.

El saldo **nunca** se guarda como un contador que se pisa. Se calcula:

```
saldo(cliente, tipo) = Σ entregados − Σ retirados ± ajustes
```

Estados del inventario: `planta_lleno`, `planta_vacio`, `camion`, `en_cliente`, `fuera_servicio`, `perdido`.

Tipos de movimiento: `entrega`, `retiro`, `carga`, `devolucion`, `ajuste`, `baja_fuera_servicio`, `baja_perdido`, `alta` (compra de envases nuevos).

## Modelo de datos (tentativo)

Los nombres siguen la convención actual `sap_xx00`. Las columnas son orientativas.

| Tabla | Propósito | Columnas clave |
|-------|-----------|----------------|
| `sap_em00` | Cuenta (si aún no existe como tabla) | `em_id`, `em_tipo` (`proveedor`/`independiente`), `em_nom` |
| `sap_mb00` | Membresía usuario ↔ cuenta | `us_id`, `empresa_id`, `ro_id` |
| `sap_ro00` | Roles por cuenta | `ro_id`, `empresa_id`, `ro_nom`, `ro_sistema` |
| `sap_rp00` | Permisos de cada rol | `ro_id`, `permiso` |
| `sap_vi00` | Vínculo independiente → mayorista | `empresa_independiente_id`, `empresa_proveedor_id`, `cl_id`, `estado`, `codigo` |
| `sap_jo00` | Jornada | `jo_id`, `empresa_id`, `us_repartidor_id`, `fecha`, `estado`, `abierta_por`, `cerrada_por` |
| `sap_jc00` | Carga de apertura | `jo_id`, `pr_id`, `cantidad` |
| `sap_jx00` | Conteo de cierre | `jo_id`, `concepto`, `pr_id`/`et_id`, `cantidad`, `monto`, `contado_por` |
| `sap_jd00` | Foto de la conciliación | `jo_id`, `control`, `esperado`, `real`, `diferencia`, `clasificacion` |
| `sap_et00` | Tipo de envase | `et_id`, `empresa_id`, `et_nom` |
| `sap_me00` | Movimiento de envase | `me_id`, `empresa_id`, `et_id`, `cl_id?`, `jo_id?`, `tipo`, `cantidad`, `us_id`, `fecha` |
| `sap_cb00` | Cobro de deuda (si aún no existe un registro por cobro) | `cb_id`, `empresa_id`, `cl_id`, `jo_id?`, `monto`, `us_id`, `fecha` |

Cambios en tablas existentes:

- `sap_vt00` (ventas): agregar `jo_id` y `us_id` (quién vendió) para sumar por jornada.
- `sap_pr00` (productos): agregar `et_id` (tipo de envase retornable, opcional).
- `sap_cl00` (clientes): agregar `us_asignado_id` (repartidor asignado) y `cl_tipo` (`final`/`mayorista`).

> **A verificar:** `deudaCliente.php` actualiza la deuda directamente sobre `sap_cl00`. Para conciliar el dinero por jornada hace falta un registro **por cobro** (`sap_cb00`) con su `jo_id`. Antes de la Fase 1, confirmar si ese registro ya existe.

## API (tentativa)

Siguiendo el estilo actual de un archivo PHP por recurso:

| Endpoint | Uso | Permiso |
|----------|-----|---------|
| `jornadas.php` | abrir, listar, ver detalle | `jornada.abrir` |
| `cierres.php` | registrar conteo (a ciegas), cerrar | `cierre.contar` / `cierre.recibir_dinero` |
| `conciliacion.php` | ver esperado vs. real | `cierre.ver_diferencias` |
| `envases.php` | tipos, movimientos, saldos, bajas | `envases.gestionar` |
| `usuarios.php`, `roles.php` | membresías, roles y permisos configurables | `usuarios.gestionar`, `roles.gestionar` |
| `vinculos.php` | crear invitación, aceptar código | `mayoristas.gestionar` / independiente |
| `mi_cuenta_proveedor.php` | vista del independiente: deuda, movimientos y envases | membresía vía vínculo, solo lectura |

## Panel web: módulos

| Módulo | Contenido | Fase |
|--------|-----------|------|
| Jornadas | Apertura, jornadas en curso, cierre a ciegas, conciliación | 1 |
| Repartidores | Alta de empleados, asignación de clientes | 1 |
| Configuración ▸ Roles | Activar o desactivar permisos configurables | 0–1 |
| Envases | Tipos, inventario por estado, bajas | 2 |
| Mayoristas | Independientes: cuenta corriente, vínculos | 3 |
| Reportes | Ventas por repartidor, deuda en la calle, diferencias | 4 |

## Fases

| Fase | Entrega | Depende de |
|------|---------|-----------|
| **0. Fundaciones** | Cuentas, membresías, roles y permisos en la API (`require_permission`); `us_id` en ventas; cobros como registro | aislamiento por `empresa_id` existente |
| **1. Jornadas (MVP)** | Apertura, recorrido vinculado a la jornada, cierre a ciegas, conciliación de producto y dinero, panel web mínimo | Fase 0 |
| **2. Envases** | Tipos, movimientos desde la app en cada entrega, saldo por cliente, conciliación de envases, bajas | Fase 1 |
| **3. Independientes** | Vínculo por invitación, vista de solo lectura de deuda y envases con el proveedor | Fase 0 |
| **4. Reportes y mayoristas** | Tablero del dueño, gestión de mayoristas | Fases 1–3 |

## Stack

| Pieza | Tecnología | Notas |
|-------|------------|-------|
| Panel web | React + TypeScript + Vite | Tablas, formularios y reportes del dueño y el encargado |
| App de repartidores | Kotlin + Jetpack Compose (actual) | Se mantiene; se extiende con jornada y envases |
| Backend | PHP (actual) | Una sola API para ambos clientes |

Recomendaciones para el backend antes de crecer:

1. **Punto de entrada único con middleware**: JWT → `empresa_id` → `require_permission()`, en lugar de chequeos por archivo (por ejemplo, con Slim).
2. **Lógica de negocio separada de los endpoints**: la conciliación de cierre como función pura y testeable.
3. **Migraciones versionadas** (por ejemplo, Phinx) en lugar de dumps `.sql`.
4. **Contrato OpenAPI** compartido entre la app Kotlin y el panel web.
5. **Tests de permisos** con PHPUnit (por ejemplo, "el encargado no puede ver diferencias").
6. **Auth en el navegador**: CORS restringido al dominio del panel y token en cookie `httpOnly`, no en `localStorage`.

## App offline-first

Hay zonas sin señal, y la app debe seguir registrando ventas, cobros y envases. Hoy la app es 100% online (Retrofit directo, sin Room), así que esto es un cambio de arquitectura.

```
UI ──► Room (base local) ◄──► Sincronizador (WorkManager) ◄──► API PHP
          ▲                         │
          └── outbox ───────────────┘
```

1. **Descarga de ruta** al abrir la jornada: clientes asignados, productos, precios, carga, deudas y saldos de envases.
2. **Outbox**: cada operación se guarda en Room y en una cola de pendientes; la UI responde al instante.
3. **WorkManager** envía la cola en orden, con reintentos, cuando hay señal.
4. **UUID generado en el teléfono + idempotencia**: el servidor ignora un UUID ya recibido, así un reintento nunca duplica.
5. **Número de secuencia por jornada** en cada operación (1, 2, 3…), para que el servidor detecte huecos.

Los saldos mostrados offline (deuda, envases, stock del camión) son **provisorios**; los definitivos los calcula el servidor. Como todo son movimientos que solo se agregan, no hay conflictos de escritura.

### Cierre bloqueado hasta sincronizar

El servidor no puede saber qué hay en el teléfono, así que el teléfono se lo informa:

- Al terminar el recorrido, la app envía **"fin de recorrido"** con la cantidad total de operaciones de la jornada (por ejemplo, 45).
- El cierre se habilita solo cuando el servidor recibió el fin de recorrido **y** las 45 operaciones, sin huecos de secuencia.
- El panel muestra "Esperando sincronización: 42 de 45".
- **Salida de emergencia** (teléfono roto o perdido): solo con un permiso bloqueado del dueño (`cierre.forzar_sin_sincronizar`). Queda registrado quién, cuándo y cuántas operaciones faltaban.

## Migración de usuarios actuales

Todos los usuarios actuales de SodApp son **soderos independientes**. La proveedora es una cuenta nueva que se crea aparte.

Hoy el aislamiento usa `cl_emp` (y equivalentes) con el **ID del usuario** (`cl_emp = $userId`): usuario y empresa son 1 a 1. La migración aprovecha eso:

1. Crear una cuenta en `sap_em00` por cada usuario, con `em_id = us_id` y `em_tipo = 'independiente'`. Así, los `cl_emp` existentes siguen siendo válidos sin reescribir datos.
2. Crear una membresía en `sap_mb00` para cada usuario en su cuenta, con el rol **Dueño** (todos los permisos).
3. Pasar la resolución de cuenta en la API de "usuario = empresa" a "membresía del usuario".
4. **Deuda:** crear `sap_cb00` (un registro por cobro). Mantener `cl_deb` como saldo de lectura rápida, actualizado en la misma transacción que el cobro. El saldo inicial de cada cliente se registra como un movimiento de apertura.

Requisito: la migración **no puede romper la app actual en producción**. Primero se despliega el backend compatible (los JWT actuales siguen funcionando y el Dueño tiene todos los permisos) y recién después las versiones nuevas de la app.

## Fuera de alcance (implementación futura)

- El independiente **informa pagos** al proveedor desde la app (requiere el estado "pendiente de aprobación").
- El independiente **hace pedidos de carga** al proveedor desde la app.
- Roles personalizados creados por el dueño (en v1 hay roles fijos con permisos configurables).

## Preguntas abiertas

- [x] Stack del panel web: React + TypeScript + Vite, contra el mismo backend PHP (ver [Stack](#stack)).
- [x] Funcionamiento sin conexión: offline-first con cierre bloqueado hasta sincronizar (ver [App offline-first](#app-offline-first)).
- [x] Registro por cobro: **no existe**. `deudaCliente.php` solo hace `UPDATE sap_cl00 SET cl_deb = cl_deb - ?`. Se necesita `sap_cb00` (ver [Migración](#migración-de-usuarios-actuales)).
- [x] Migración: cada usuario actual pasa a ser Dueño de su propia cuenta independiente (ver [Migración](#migración-de-usuarios-actuales)).
- [x] Varias jornadas por día: sí, pero **una sola abierta a la vez** por repartidor (ver [Varias jornadas por día](#varias-jornadas-por-día)).
