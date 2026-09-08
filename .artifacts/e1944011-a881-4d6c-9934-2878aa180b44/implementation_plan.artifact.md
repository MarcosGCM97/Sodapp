# Plan de Implementación - Corregir Actualización de Días de Entrega

El objetivo es sincronizar la aplicación Android con los requisitos del backend PHP para la actualización de los días de visita de los clientes. Se transformará la lista de días seleccionados en flags individuales (0/1) y se corregirá el nombre de la clave del ID.

## User Review Required

> [!WARNING]
> **Cambio de Contrato de API**: Este cambio modifica la estructura del JSON enviado al servidor. He detectado un error crítico en tu script PHP (desajuste entre los `?` del SQL y los parámetros de `bind_param`) que debe ser corregido para que esta solución funcione.

## Proposed Changes

### [Componente de Datos]

#### [MODIFY] [Cliente.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/Cliente.kt)
- Agregar la clase `DiasEntregaUpdateRequest` con los campos: `cl_ide`, `cl_lun`, `cl_mar`, `cl_mie`, `cl_jue`, `cl_vie`, `cl_sab`, `cl_dom`.

#### [MODIFY] [ApiServices.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/API/ApiServices.kt)
- Cambiar la firma de `updateDiasEntrega` para que acepte `DiasEntregaUpdateRequest` en lugar de `DiasEntrega`.

### [Lógica de Negocio]

#### [MODIFY] [AgendaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/AgendaViewModel.kt)
- En la función `updateDiasEntrega`, añadir la lógica para convertir `List<String>` a los campos booleanos del nuevo objeto de petición.

## Verification Plan

### Manual Verification
- Ejecutar la aplicación, seleccionar un cliente y editar sus días de visita.
- Verificar en Logcat (OkHttp) que el JSON enviado tenga la forma: `{"cl_ide": 49, "cl_lun": 1, "cl_mar": 0, ...}`.
- Confirmar que el backend responda exitosamente (una vez corregido el PHP).

---

## Sugerencia de Backend (PHP)

Al final de la ejecución, te proporcionaré el código PHP corregido que resuelve el error de `bind_param`.
