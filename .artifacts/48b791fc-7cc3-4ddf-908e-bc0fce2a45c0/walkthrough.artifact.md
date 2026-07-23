# Walkthrough — Fase 5: Repository Pattern

Se ha implementado el **Repository Pattern** en todo el proyecto, eliminando la dependencia directa de los ViewModels hacia Retrofit e introduciendo una capa de abstracción robusta.

## Cambios Realizados

### Capa de Repositorios
Se han creado interfaces e implementaciones para cada dominio del negocio. Esto permite centralizar el acceso a datos y facilita el cambio de fuentes de datos (ej. agregar caché local) sin afectar el resto de la app.
- **Dominios implementados**: `Auth`, `Cliente`, `Producto`, `Venta`, `Caja` y `Agenda`.
- **Implementaciones**: Usan `ApiServices` (Retrofit) para la obtención de datos actual.

### Inyección de Dependencias (Hilt)
Se creó el módulo [RepositoryModule.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/di/RepositoryModule.kt) usando `@Binds` para inyectar automáticamente las implementaciones cuando se solicita una interfaz de repositorio.

### Refactorización de ViewModels
Todos los ViewModels del proyecto han sido actualizados para depender de interfaces de repositorios:
- Se eliminaron las dependencias directas de `ApiServices`.
- Se limpiaron los imports innecesarios.
- La lógica de negocio ahora es más pura, delegando la responsabilidad de "cómo se obtienen los datos" a los repositorios.

## Verificación

> [!TIP]
> El proyecto ahora cumple con el principio de **Inversión de Dependencias (D)** de SOLID, ya que los ViewModels dependen de abstracciones (interfaces) y no de implementaciones concretas.

> [!IMPORTANT]
> Se ha verificado que `VentasViewModel` utilice tanto `VentaRepository` como `ClienteRepository` para coordinar la eliminación de ventas y actualización de deudas correctamente.
