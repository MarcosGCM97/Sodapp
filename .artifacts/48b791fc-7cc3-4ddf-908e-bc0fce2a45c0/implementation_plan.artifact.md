# Fase 7 — Rendimiento (LazyColumn, Caché y Lógica de UI)

Esta fase se enfoca en optimizar el rendimiento de la aplicación al renderizar listas largas y reducir las llamadas innecesarias a la red. También desacoplaremos la lógica de negocio que aún reside en la capa de UI.

## User Review Required

> [!TIP]
> Al migrar a `LazyColumn`, la aplicación será mucho más fluida al manejar cientos de ventas o productos, ya que solo se renderizarán los elementos visibles en pantalla.

## Propuestas de Cambio

### 1. Optimización de la Pantalla de Ventas
- **Problema**: `Ventas.kt` utiliza un `Column` con `verticalScroll`, lo que carga todas las ventas en memoria. Además, la lógica de agrupación por cliente y fecha está en el Composable.
- **Solución**:
    - Mover la lógica de `ventasAgrupadas` al `VentasViewModel`.
    - Reemplazar `Column` por `LazyColumn`.
    - Usar `item { AddVentaForm() }` y `items(ventasAgrupadas) { ... }`.

#### [MODIFY] [VentasViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentasViewModel.kt)
#### [MODIFY] [Ventas.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Ventas.kt)

### 2. Optimización de la Pantalla de Caja
- **Problema**: Similar a Ventas, los totales y la agrupación por producto se calculan en la UI.
- **Solución**:
    - Mover cálculos de `cantidadDeVentasPorProducto`, `cantidadDeVentas` y `cantidadDePlata` al `CajaViewModel`.
    - Migrar la visualización de resultados a `LazyColumn`.

#### [MODIFY] [CajaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaViewModel.kt)
#### [MODIFY] [Caja.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/Caja.kt)

### 3. Implementación de Caché en Repositorios
- **Problema**: Cada vez que se navega entre pantallas, los ViewModels vuelven a pedir los datos a la API (ej. `getProductos`, `getClientes`).
- **Solución**: Implementar una política de caché simple en memoria dentro de los repositorios para que, si los datos ya fueron cargados recientemente, se devuelvan inmediatamente mientras se refrescan en segundo plano (opcional) o simplemente se usen los cacheados si no ha pasado mucho tiempo.

#### [MODIFY] [ProductoRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoRepository.kt)
#### [MODIFY] [ClienteRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteRepository.kt)

## Verificación Plan

### Automated Tests
- `gradlew assembleDebug`: Asegurar que los cambios en los ViewModels no rompan la compilación de la UI.

### Manual Verification
- **Scroll**: Verificar que el scroll en la pantalla de Ventas sea fluido incluso con muchos registros.
- **Navegación**: Entrar y salir de la pantalla de Productos y notar que la carga es instantánea si el caché está activo.
- **Totales**: Confirmar que los totales en Ventas y Caja sigan siendo correctos tras mover la lógica al ViewModel.
