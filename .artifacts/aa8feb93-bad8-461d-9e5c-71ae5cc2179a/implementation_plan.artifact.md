# Implementation Plan - General Repository and ViewModel Refactor

Apply the refactoring pattern established in SD-012 to all other components in the app. This involves moving HTTP validation and exception handling from ViewModels to Repositories using specific `Result` sealed classes.

## User Review Required

> [!IMPORTANT]
> This refactor affects all major modules of the app. I will proceed component by component to ensure stability.
> I will also harmonize `AgendaUiState` to use `@StringRes` instead of raw `String` to be consistent with the rest of the app.

## Proposed Changes

### 1. Cliente & Agenda Component
- **[MODIFY] [ClienteRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteRepository.kt)**: Define `ClienteResult` and refactor all methods to return it.
- **[MODIFY] [ClienteViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteViewModel.kt)**: Simplify methods using `when` on `ClienteResult`.
- **[MODIFY] [AgendaRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/AgendaRepository.kt)**: Define `AgendaResult` and refactor methods.
- **[MODIFY] [AgendaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/AgendaViewModel.kt)**: Update `AgendaUiState` to use `@StringRes` and simplify logic.

### 2. Producto Component
- **[MODIFY] [ProductoRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoRepository.kt)**: Define `ProductoResult` and refactor methods.
- **[MODIFY] [ProductoViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Producto/ProductoViewModel.kt)**: Simplify logic using `when` on `ProductoResult`.

### 3. Ventas Component
- **[MODIFY] [VentaRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentaRepository.kt)**: Define `VentaResult` and refactor methods.
- **[MODIFY] [VentasViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Ventas/VentasViewModel.kt)**: Simplify logic using `when` on `VentaResult`.

### 4. Auth Component
- **[MODIFY] [AuthRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/IngresoUsuario/AuthRepository.kt)**: Define `AuthResult` and refactor methods.
- **[MODIFY] [LoginViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/IngresoUsuario/LoginViewModel.kt)**: Simplify logic using `when` on `AuthResult`.

### 5. Common Patterns
- I will ensure all `Error` data classes in the `Result` sealed classes have proper `equals` and `hashCode` implementations to facilitate testing and predictable UI updates.

## Verification Plan

### Automated Tests
- I will create unit tests for at least one refactored Repository and ViewModel as a sanity check (similar to `Caja` tests).
- Run `./gradlew test` to ensure no regressions.

### Manual Verification
- Deploy the app and perform basic operations (Login, Load Clients, Add Sale, etc.) to verify that the refactored logic still works as expected.
