# Fix: Productos no se listan en el formulario de Nueva Venta

El problema identificado es que el Composable `AddVentaForm` no dispara la carga de productos desde el `ProductoViewModel`, lo que resulta en un estado `Idle` permanente y el mensaje "No hay productos disponibles" en el selector.

## Propuestas de Cambio

### 1. Activar carga de productos en `AddVentaForm`
- **Cambio**: Añadir un `LaunchedEffect(Unit)` dentro de `AddVentaForm` para llamar a `productoModel.getProductos()`.
- **Mejora**: Pasar las instancias de los ViewModels desde el Composable padre `Ventas` para asegurar consistencia en el estado y evitar inyecciones redundantes si la jerarquía cambia.

#### [MODIFY] [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)
- Añadir `LaunchedEffect(Unit) { productoModel.getProductos() }` en `AddVentaForm`.
- Actualizar la llamada a `AddVentaForm()` en `Ventas()` para pasar los modelos explícitamente.

## Verificación Plan

### Manual Verification
1. Abrir la pantalla de Ventas.
2. Desplegar el formulario "Cargar la venta".
3. Verificar que el selector de "Producto" muestre los productos cargados desde la base de datos en lugar de "No hay productos disponibles".
4. Seleccionar un producto y confirmar que se agrega a la lista de la venta.
