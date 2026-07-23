# Fix: Corrección de Crash en Caja y Conflicto de DataStore

Este plan aborda dos errores críticos identificados tras la refactorización inicial: el fallo de casteo en DataStore y la incompatibilidad del modelo de Ventas con el reporte de Caja.

## Propuestas de Cambio

### 1. Solución al conflicto de DataStore
- **Problema**: El cambio de tipo de `Set<String>` a `String` causa un error `ClassCastException` porque DataStore conserva los datos viejos bajo la misma llave.
- **Solución**: Renombrar las llaves internas de `user_id` y `user_name` a una versión nueva. Esto forzará una migración limpia y evitará el crash.

#### [MODIFY] [UserPreferencesRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/UserPreferencesRepository.kt)

### 2. Solución al Crash en Caja (NPE)
- **Problema**: La API `cajaMes.php` devuelve un JSON con campos planos (`cl_nom`, `pr_nom`) mientras que `Venta` esperaba campos anidados o con nombres diferentes (`vt_cli`, `vt_pro`). Esto hacía que el campo `producto` fuera nulo, provocando un crash al agrupar.
- **Solución**:
    - Usar `alternate` en `@SerializedName` para que `producto` reconozca tanto `vt_pro` como `pr_nom`.
    - Añadir campos planos para el cliente y una propiedad calculada para unificar la visualización.

#### [MODIFY] [Venta.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/Venta.kt)

### 3. Refuerzo de Seguridad (Null-Safety)
- **Problema**: Operaciones como `groupBy { it.producto }` pueden fallar si el servidor devuelve nulos inesperados.
- **Solución**: Usar el operador elvis `?: ""` y `ifBlank` para garantizar que los nombres de productos nunca sean nulos en la lógica de agrupación.

#### [MODIFY] [VentasViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentasViewModel.kt)
#### [MODIFY] [CajaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaViewModel.kt)

## Verificación Plan

### Automated Tests
- `gradlew assembleDebug`: Asegurar que el proyecto compile.

### Manual Verification
- **Login**: Iniciar sesión (se requerirá una vez más debido al cambio de llaves) y verificar que no haya error de casteo.
- **Caja**: Presionar "Ver caja Mes" y confirmar que los datos se muestran correctamente sin cerrar la aplicación.
