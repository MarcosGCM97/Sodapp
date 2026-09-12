# Plan de Implementación: Formateo de Precios Personalizado

Este plan detalla los cambios para implementar el formato de precios `1.200,00` (punto para miles, coma para decimales) en toda la aplicación.

## User Review Required

> [!IMPORTANT]
> Se centralizará la lógica de formateo en un nuevo archivo `FormatUtils.kt` para que pueda ser reutilizado en todas las pantallas. Esto asegurará la consistencia visual en toda la app.

## Proposed Changes

### Core Logic

#### [NEW] [FormatUtils.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/FormatUtils.kt)
- Definir la función `formatearPrecio(precio: Double?): String` usando `DecimalFormat` con `Locale("es", "AR")`.

#### [DELETE] `formatearPrecio` en [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)
- Eliminar la versión antigua de la función.

### UI & Messages

#### [MODIFY] [strings.xml](file:///C:/Users/marco/SodAppComposse/app/src/main/res/values/strings.xml)
- Cambiar los especificadores de formato de `%f` a `%s` en las cadenas relacionadas con precios (`precio_format`, `total_format`, `wpp_mensaje_ventas`, etc.) para aceptar el `String` formateado.

#### [MODIFY] [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)
- Importar `formatearPrecio` de `FormatUtils`.
- Corregir el bug en `armarMensajeVentasWpp` y aplicar el formateo.
- Aplicar `formatearPrecio` en las llamadas a `stringResource`.

#### [MODIFY] [Caja.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/Caja.kt)
- Aplicar `formatearPrecio` en las llamadas a `stringResource`.

#### [MODIFY] [DeudaScreen.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/DeudaScreen.kt)
- Aplicar `formatearPrecio` en los Toasts y llamadas a `stringResource`.

## Verification Plan

### Manual Verification
- **Pantalla de Ventas**: Verificar que el total y los precios de los ítems muestren el formato `1.200,00`.
- **Mensaje de WhatsApp**: Verificar que el mensaje generado contenga los precios formateados correctamente.
- **Pantalla de Caja**: Verificar que el resumen mensual use el nuevo formato.
- **Detalle de Deuda**: Verificar que el saldo y los Toasts de pago muestren el formato solicitado.
