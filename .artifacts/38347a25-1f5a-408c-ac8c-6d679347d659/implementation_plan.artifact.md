# Rediseño de Barra de Navegación Inferior (Estilo Píldora Expandible)

El objetivo es transformar la barra de navegación inferior actual de iconos fijos a un estilo moderno de "etiqueta expandible", donde el elemento seleccionado muestra su texto y un fondo destacado (píldora), mientras que los inactivos muestran solo el icono.

## Cambios Propuestos

### Componentes de UI

#### [MODIFY] [NavBottom.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Componentes/NavBottom.kt)

Se rediseñará el componente `NavBottom` y su sub-componente `CustomNavIconButton` (que pasará a tener una lógica de expansión).

**Especificaciones visuales:**
- **Fondo de la barra:** Navy oscuro (`#1e2a38`).
- **Color de acento (activo):** Morado (`#6a5acd`).
- **Color de iconos inactivos:** Gris claro (`#c8cfd8`).
- **Animación:** Transición de 200-300ms para el ancho del ítem y el desvanecimiento del texto.

**Lógica de implementación:**
1.  Actualizar los colores predefinidos dentro de la función o usar el sistema de temas si se prefiere (se usarán los hex especificados por el usuario para fidelidad).
2.  Rediseñar `CustomNavIconButton` (o renombrarlo internamente) para usar un contenedor `Box` o `Row` con `animateContentSize()`.
3.  El ítem seleccionado mostrará un `Row` que contiene `Icon` + `Text`.
4.  El ítem no seleccionado mostrará solo el `Icon`.
5.  Se usará `AnimatedVisibility` o estados animados para asegurar una transición fluida del texto.
6.  Se mantendrá la firma de la función `NavBottom` para asegurar compatibilidad con `BienvenidaScreen.kt`.

## Plan de Verificación

### Pruebas Manuales
- **Navegación:** Verificar que al tocar cada pestaña, el contenido de la pantalla cambie correctamente (Ventas, Clientes, Inventario, Caja).
- **Animación:** Observar que el ítem seleccionado se expanda suavemente y el anterior se contraiga.
- **Persistencia:** Confirmar que al cambiar de pestaña y volver, el estado se mantiene (ya gestionado por la arquitectura actual de `BienvenidaScreen`).
- **Previsualización:** Usar los Previews en Android Studio para verificar el diseño en modo claro y oscuro (aunque el fondo de la barra será fijo navy).

### Compilación
- Ejecutar `./gradlew assembleDebug` para asegurar que no hay errores de sintaxis.
