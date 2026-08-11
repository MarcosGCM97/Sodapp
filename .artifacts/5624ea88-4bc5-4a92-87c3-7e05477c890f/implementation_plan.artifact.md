# Plan de Migración de Strings en ViewModels a Recursos

Este plan detalla la migración de strings hardcodeados en los ViewModels al sistema de recursos de Android, siguiendo el patrón MVVM y evitando dependencias de `Context` en la capa de lógica.

## User Review Required

> [!IMPORTANT]
> Se modificarán las estructuras de `UiState` (sealed classes/interfaces) para usar IDs de recursos (`@StringRes Int`) y argumentos dinámicos. Esto afectará a todos los componentes que consumen estos estados.

## Proposed Changes

### [Recursos]

#### [MODIFY] [strings.xml](file:///C:/Users/marco/SodAppComposse/app/src/main/res/values/strings.xml)
Se agregarán plantillas genéricas y específicas para mensajes de éxito y error:
- `error_red_verificar`: Error de Red: Verifica tu conexión.
- `error_servidor_format`: Error servidor: %1$d
- `error_http_format`: Error HTTP: %1$d
- `error_inesperado_format`: Error inesperado: %1$s
- `cuerpo_nulo_error`: Respuesta exitosa pero cuerpo nulo.
- `campos_requeridos_error`: Todos los campos son requeridos.
- Y mensajes específicos para Clientes, Productos, Ventas y Login.

### [Capa de Lógica (ViewModels)]

#### [MODIFY] [ClienteViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteViewModel.kt)
#### [MODIFY] [ProductoViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoViewModel.kt)
#### [MODIFY] [VentasViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentasViewModel.kt)
#### [MODIFY] [CajaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaViewModel.kt)
#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/IngresoUsuario/LoginViewModel.kt)
- Actualizar `UiState` para usar `messageRes: Int` y `args: Array<Any>`.
- Reemplazar strings literales por referencias a `R.string`.

### [Capa de UI (Composables)]

#### [MODIFY] [Clientes.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/Clientes.kt)
#### [MODIFY] [ProductoForm.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoForm.kt)
#### [MODIFY] [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)
#### [MODIFY] [Caja.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/Caja.kt)
#### [MODIFY] [LoginScreen.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/LoginScreen.kt)
- Resolver los recursos de string usando `context.getString(resId, *args)` para Toasts.
- Usar `stringResource(resId, *args)` para componentes `Text`.

## Verification Plan

### Manual Verification
- Compilar el proyecto para asegurar que todas las referencias a `UiState` han sido actualizadas.
- Probar flujos de error (ej: desconectar internet) para verificar que los mensajes se muestran correctamente desde recursos.
- Validar que los mensajes de éxito con parámetros (ej: "Cliente 'X' guardado") funcionen correctamente.
