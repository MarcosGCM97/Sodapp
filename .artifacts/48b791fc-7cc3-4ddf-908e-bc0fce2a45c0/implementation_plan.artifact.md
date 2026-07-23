# Fase 5 — Introducir Repository Pattern

Esta fase introduce una capa de abstracción entre los ViewModels y la API de Retrofit. Esto nos permite centralizar el manejo de datos, facilitar las pruebas unitarias y cumplir con el principio de Inversión de Dependencias (D).

## User Review Required

> [!NOTE]
> Se crearán interfaces para cada repositorio siguiendo la estructura de dominios definida en la Fase 4. Esto cambiará la forma en que los ViewModels acceden a los datos, pero la funcionalidad de la app permanecerá idéntica.

## Propuestas de Cambio

### 1. Definir Interfaces y Repositorios
Se agruparán los endpoints de `ApiServices` en repositorios lógicos:

- **`ClienteRepository`**: Gestión de datos maestros de clientes y deudas.
- **`ProductoRepository`**: CRUD de productos.
- **`VentaRepository`**: Gestión de transacciones de venta.
- **`CajaRepository`**: Reportes de caja mensual.
- **`AgendaRepository`**: Planificación de visitas y entregas.
- **`AuthRepository`**: Inicio de sesión y gestión de usuario.

#### [NEW] Repositorios (Interfaces e Implementaciones)
- Se crearán archivos como `ClienteRepository.kt`, `ProductoRepository.kt`, etc., en sus respectivos paquetes de dominio.

### 2. Configurar Inyección de Dependencias (Hilt)
Crearemos un nuevo módulo para proveer las implementaciones de los repositorios.

#### [NEW] [RepositoryModule.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/di/RepositoryModule.kt)

### 3. Refactorizar ViewModels
Actualizaremos todos los ViewModels para que dependan de las interfaces de los repositorios en lugar de llamar directamente a `ApiServices`.

- **`ClientesViewModel`** -> `ClienteRepository`
- **`ProductoViewModel`** -> `ProductoRepository`
- **`VentasViewModel`** -> `VentaRepository`
- **`AgendaViewModel`** -> `AgendaRepository`
- **`DeudaViewModel`** -> `ClienteRepository` (o un `DeudaRepository` si se prefiere)
- **`CajaViewModel`** -> `CajaRepository`
- **`LoginViewModel`** -> `AuthRepository`

## Verificación Plan

### Automated Tests
- `gradlew assembleDebug`: Confirmar que el grafo de dependencias de Hilt es válido y la app compila.

### Manual Verification
- Navegar por todas las pantallas (Clientes, Productos, Ventas, Caja, Agenda) para confirmar que la carga de datos sigue funcionando correctamente a través de la nueva capa.
