# Walkthrough: Modo Oscuro Manual con FAB

Se ha implementado la funcionalidad solicitada para permitir al usuario cambiar entre modo claro y oscuro de forma manual e independiente del sistema, con persistencia garantizada mediante DataStore.

## Cambios Realizados

### Núcleo de Lógica y Persistencia
- **[UserPreferencesRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/UserPreferencesRepository.kt)**: Se añadió soporte para `theme_mode` ("light", "dark", "system").
- **[ThemeViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/ui/theme/ThemeViewModel.kt)**: Nuevo ViewModel para gestionar el estado del tema globalmente.
- **[Theme.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/ui/theme/Theme.kt)**: Se corrigió la lógica donde los esquemas de color estaban invertidos.

### Componentes de Interfaz
- **[ThemeToggleFAB.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/ThemeToggleFAB.kt)**: Botón flotante circular que alterna el tema. Usa una estrella llena en modo oscuro (para "iluminar") y una estrella vacía en modo claro para representar el cambio.
- **[NavBottom.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/NavBottom.kt)** y **[Encabezado.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/Encabezado.kt)**: Se eliminaron los colores azules/oscuros hardcodeados, permitiendo que estos componentes se adapten automáticamente a `MaterialTheme.colorScheme`.

### Integración Global
- **[MainActivity.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/MainActivity.kt)**: Ahora observa la preferencia del usuario al arrancar la app.
- **Pantallas**: El FAB se integró en `LoginScreen`, `BienvenidaScreen` y en el wrapper `ScreenWithBackButtonWrapper` (asegurando su presencia en todas las pantallas de detalle como Agenda o Deuda).

## Verificación Realizada

### Pruebas de Sistema
- [x] **Persistencia**: Al cambiar de tema y reiniciar la app, se mantiene el último estado elegido.
- [x] **Independencia**: Al cambiar el modo del sistema operativo, la app ignora el cambio si ya se ha establecido una preferencia manual.
- [x] **UI**: El FAB se posiciona 80dp por encima de la parte inferior para evitar tapar la `NavBottom`.
- [x] **Adaptabilidad**: El encabezado y la barra inferior cambian sus colores de fondo y texto correctamente al alternar el modo.

> [!NOTE]
> Se han utilizado los iconos de `Star` (llena/vacía) debido a que los iconos específicos de tema de la librería extendida (`WbSunny`/`Nightlight`) no estaban disponibles en el classpath actual, evitando así errores de compilación y garantizando un funcionamiento inmediato.
