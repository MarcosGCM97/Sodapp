# Guía de Integración: Android App a Backend PHP

Este documento describe cómo la aplicación Android se comunica con el servidor PHP actual. Está diseñado para ser procesado por una IA con el fin de realizar mejoras en el lado del servidor.

## 🛠 Configuración Técnica (Cliente Android)

- **Librería de Red:** Retrofit 2 + OkHttp 3.
- **URL Base:** `http://www.aplicaciones-servicios-1997.site/`
- **Formato de Datos:** JSON (GSON Converter).
- **Interacción:** Asíncrona mediante Coroutines (`suspend functions`).

---

## 📡 Endpoints y Contratos de Datos

### 1. Autenticación
- **Ruta:** `POST api/login.php`
- **Request Body (`UsuarioRequest`):**
  ```json
  {
    "nombreUs": "string",
    "contrasenaUs": "string"
  }
  ```
- **Response (`UsuarioResponse`):**
  ```json
  {
    "success": boolean,
    "message": "string",
    "usuario": {
      "us_ide": int,
      "us_nom": "string",
      "us_pas": "string"
    }
  }
  ```

### 2. Gestión de Clientes
- **Obtener Todos:** `GET api/clientes.php`
- **Obtener por ID:** `GET api/clientes.php?id={id}`
- **Crear Cliente (`POST`):**
  ```json
  {
    "nombreCl": "string",
    "numTelCl": "string",
    "direccionCl": "string"
  }
  ```
- **Actualizar Cliente (`PUT`):**
  *Nota: Actualmente usa parámetros de consulta (Query Params).*
  `PUT api/clientes.php?id={id}&nombre={nombre}&direccion={dir}&telefono={tel}`
- **Eliminar Cliente:** `DELETE api/clientes.php?id={id}`

### 3. Gestión de Productos
- **Obtener Todos:** `GET api/productos.php`
- **Obtener por Nombre:** `GET api/productos.php?nombre={nombre}`
- **Crear Producto (`POST`):**
  ```json
  {
    "nombrePr": "string",
    "precioUni": double,
    "stock": int
  }
  ```
- **Actualizar Producto (`PUT`):**
  `PUT api/productos.php?nombre={nombre}&precio={precio}&cantidad={cantidad}`
- **Eliminar Producto:** `DELETE api/productos.php?nombre={nombre}`

### 4. Ventas y Deudas
- **Obtener Ventas:** `GET api/ventas.php?usuarioId={id}`
- **Registrar Venta (`POST`):**
  ```json
  {
    "clienteId": int,
    "productos": [
      {
        "pr_nom": "string",
        "vt_can": int
      }
    ],
    "usuarioId": "string"
  }
  ```
- **Actualizar Deuda:** `PUT api/deudaCliente.php?id={id}&deuda={monto}`

---

## 💡 Sugerencias de Mejora para el Backend (PHP)

> [!IMPORTANT]
> Se recomienda encarecidamente que la IA de VS Code implemente los siguientes cambios para estandarizar el backend.

### 1. Estandarización RESTful
- **Cuerpos de Solicitud en PUT:** Cambiar los métodos `PUT` que usan Query Params por `JSON Body`.
  *Ejemplo:* En lugar de `PUT api/clientes.php?id=1&nombre=Juan`, usar `PUT api/clientes.php/1` con un JSON body.
- **Códigos de Estado HTTP:** El backend debería devolver códigos específicos:
    - `201 Created` para POST exitosos.
    - `204 No Content` para DELETE exitosos.
    - `400 Bad Request` para errores de validación.
    - `401 Unauthorized` para fallos de login.

### 2. Seguridad
- **HTTPS:** Migrar el endpoint de `http` a `https`.
- **JWT (JSON Web Tokens):** Implementar tokens de sesión en lugar de enviar la contraseña del usuario en cada respuesta de login.
- **Validación y Sanitización:** Asegurar que el PHP use *Prepared Statements* (PDO o MySQLi) para evitar Inyección SQL.

### 3. Arquitectura
- **Respuesta Unificada:** Mantener un formato de error consistente:
  ```json
  {
    "error": true,
    "code": "ERROR_CODE",
    "message": "Descripción amigable"
  }
  ```
- **Paginación:** Si la lista de clientes o ventas crece, implementar `limit` y `offset` en los endpoints `GET`.

---

## 🧬 Mapeo de Campos (Base de Datos vs Kotlin)

| Campo DB | Propiedad Kotlin | Descripción |
| :--- | :--- | :--- |
| `cl_ide` | `idCl` | ID del Cliente |
| `cl_nom` | `nombreCl` | Nombre del Cliente |
| `pr_nom` | `nombrePr` | Nombre del Producto |
| `vt_can` | `cantidad` | Cantidad vendida |
| `vt_fec` | `fecha` | Fecha de la venta |
