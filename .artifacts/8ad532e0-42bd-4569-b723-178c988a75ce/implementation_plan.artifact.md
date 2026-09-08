# Plan de Implementación: Modo Oscuro Manual con FAB Persistente

Este plan detalla los cambios necesarios para desacoplar el tema de la aplicación de la configuración del sistema y permitir al usuario alternar manualmente entre modo claro y oscuro mediante un Botón de Acción Flotante (FAB) global.

## User Review Required

> [!IMPORTANT]
> Se implementará una nueva clave en DataStore para persistir la preferencia del usuario. Por defecto, la app seguirá el sistema hasta que el usuario interactúe con el FAB por primera vez. A partir de ahí, la elección manual prevalecerá.

> [!TIP]
> Para evitar duplicidad de lógica, se consolidará el estado del tema en un `ThemeViewModel` que será inyectado en `MainActivity`.

## Proposed Changes

### Core Logic & Persistence

#### [MODIFY] [UserPreferencesRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/UserPreferencesRepository.kt)
- Agregar la clave `THEME_MODE` (String: "system", "light", "dark").
- Exponer un `Flow<Boolean?>` para el modo actual (null = sistema).
- Agregar función `setThemeMode(isDark: Boolean)` para guardar la elección manual.

#### [MODIFY] [Theme.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/ui/theme/Theme.kt)
- Corregir el mapeo de `darkTheme` a `ColorScheme` (actualmente está invertido).
- Desactivar colores dinámicos si se elige un modo manual para asegurar consistencia con la paleta de la app.

#### [NEW] [ThemeViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/ui/theme/ThemeViewModel.kt)
- Gestionar el estado del tema y proporcionar la función de alternancia (toggle).

### UI Components

#### [NEW] [ThemeToggleFAB.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/ThemeToggleFAB.kt)
- Implementar el FAB circular de 56dp.
- Iconos: `Icons.Default.WbSunny` (para pasar a claro) e `Icons.Default.Nightlight` (para pasar a oscuro).
- Ubicación: Esquina inferior derecha con elevación y margen adecuado.

#### [MODIFY] [NavBottom.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/NavBottom.kt)
- Cambiar el color de fondo hardcodeado (`NavyBackground`) por `MaterialTheme.colorScheme.surface` para que se adapte al modo claro.

### Integration

#### [MODIFY] [MainActivity.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/MainActivity.kt)
- Observar `isDarkMode` del `ThemeViewModel`.
- Pasar el estado al `SodAppComposseTheme`.

#### [MODIFY] [BienvenidaScreen.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/BienvenidaScreen.kt)
- Agregar el `ThemeToggleFAB` al `Scaffold`.

#### [MODIFY] [ScreenWithBackButtonWrapper.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/ScreenWithBackButtonWrapper.kt)
- Agregar el `ThemeToggleFAB` al `Scaffold` para que esté presente en pantallas de detalle (Agenda, Deuda, etc.).

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/LoginScreen.kt)
- Envolver el contenido en un `Scaffold` básico para incluir el `ThemeToggleFAB`.

## Verification Plan

### Manual Verification
- **Persistencia**: Cambiar a modo oscuro, cerrar la app y volver a abrir. Debe mantenerse en modo oscuro.
- **Independencia del Sistema**: Cambiar el modo del celular mientras la app está abierta en modo manual. La app NO debe cambiar.
- **UI**: Verificar que el FAB no tape el contenido de las listas en Ventas/Clientes (usar scroll).
- **Iconografía**: Confirmar que el sol aparece en modo oscuro y la luna en modo claro.
- **Navegación**: Verificar que el botón esté presente en la pantalla de Login, Bienvenida, Agenda y Deuda.
