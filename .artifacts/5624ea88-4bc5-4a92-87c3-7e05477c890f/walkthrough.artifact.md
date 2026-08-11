# Walkthrough - Migración de Strings en ViewModels a Recursos

Se ha completado la migración de todos los mensajes hardcodeados en los ViewModels hacia el sistema de recursos de Android, manteniendo una arquitectura limpia y desacoplada de `Context`.

## Cambios Realizados

### [Recursos]

#### [strings.xml](file:///C:/Users/marco/SodAppComposse/app/src/main/res/values/strings.xml)
- Se agregaron plantillas de error genéricas (`error_servidor_format`, `error_http_format`, `error_red_format`).
- Se centralizaron mensajes de éxito específicos para cada entidad (Cliente, Producto, Venta, Login).

### [ViewModels (Lógica)]

Se refactorizaron las `sealed class` de estado en los siguientes archivos para usar `@StringRes Int` y argumentos variables (`Array<Any>`):

1. **Cliente:** [ClienteViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteViewModel.kt)
2. **Producto:** [ProductoViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoViewModel.kt)
3. **Ventas:** [VentasViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentasViewModel.kt)
4. **Caja:** [CajaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaViewModel.kt)
5. **Login:** [LoginViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/IngresoUsuario/LoginViewModel.kt)

> [!NOTE]
> Se implementaron los métodos `equals` y `hashCode` manualmente en las data classes de estado que contienen `Array<Any>` para asegurar que la comparación de estados de Compose funcione correctamente.

### [Composables (UI)]

Se actualizaron los observadores de estado (`LaunchedEffect`) para resolver los recursos de string en el momento de mostrarlos:

- **Clientes:** [Clientes.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/Clientes.kt)
- **Producto:** [ProductoForm.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoForm.kt)
- **Ventas:** [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)
- **Caja:** [Caja.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/Caja.kt)
- **Login:** [LoginScreen.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/LoginScreen.kt)

## Puntos Clave de la Implementación

- **MVVM Pureza:** Los ViewModels no contienen referencias a `Context`, solo a IDs de recursos (`Int`).
- **Flexibilidad:** El uso de `Array<Any>` permite pasar cualquier cantidad de argumentos a los placeholders de `strings.xml`.
- **Feedback Dinámico:** Los mensajes de error de red y HTTP ahora son informativos y localizables.

## Verificación

- Se validó que todos los Toasts y textos de error consuman correctamente el `messageRes` y sus `args`.
- Se corrigieron advertencias de casts innecesarios y se aseguraron las importaciones de `R`.
