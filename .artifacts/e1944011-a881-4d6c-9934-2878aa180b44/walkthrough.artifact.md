# Solución: Corrección de Actualización de Clientes (PUT)

Se ha corregido el error `java.net.SocketException: Socket closed` que ocurría al intentar editar un cliente. El problema se debía a que los datos se enviaban en la URL (Query Parameters) con un cuerpo vacío, lo cual era rechazado por el servidor.

## Cambios Realizados

### [API Layer]

#### [MODIFY] [ApiServices.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/API/ApiServices.kt)
- Se cambió la anotación `@PUT` de `updateCliente` para recibir un objeto `@Body Cliente` en lugar de parámetros individuales `@Query`. Esto asegura que los datos viajen en formato JSON en el cuerpo de la petición.

### [Data Layer]

#### [MODIFY] [ClienteRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteRepository.kt)
- Se actualizó la interfaz `ClienteRepository` y su implementación `ClienteRepositoryImpl` para aceptar el objeto `Cliente` completo.
- Se eliminó la lógica de pasar parámetros individuales, delegando la estructura al objeto `Cliente`.

### [UI/Logic Layer]

#### [MODIFY] [ClienteViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Cliente/ClienteViewModel.kt)
- Se corrigió la función `editarCliente` para pasar correctamente la instancia del cliente al repositorio.

## Verificación

> [!IMPORTANT]
> Se recomienda realizar una prueba manual editando un cliente y observando los logs de OkHttp. Ahora deberías ver:
> 1. `Content-Length` mayor a 0.
> 2. Un cuerpo JSON con los campos `cl_ide`, `cl_nom`, `cl_tel`, `cl_dir`.
> 3. Una respuesta exitosa del servidor.

> [!TIP]
> Si el servidor sigue fallando, es posible que espere nombres de campos diferentes en el JSON. En ese caso, deberíamos crear una clase `UpdateClienteRequest` específica, pero el uso del modelo `Cliente` es el estándar en este proyecto.
