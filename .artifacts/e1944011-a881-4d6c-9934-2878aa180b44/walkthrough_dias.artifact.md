# Solución: Corrección de Agenda (Días de Entrega)

Se ha sincronizado el envío de datos de la agenda desde Android para que coincida con los nombres de campos y el formato esperado por el backend PHP.

## Cambios en Android

### [Componente de Datos]
- **`Cliente.kt`**: Se agregó `DiasEntregaUpdateRequest` con los campos técnicos (`cl_lun`, `cl_mar`, etc.) y el ID correcto (`cl_ide`).
- **`ApiServices.kt`** y **`AgendaRepository.kt`**: Se actualizaron para usar este nuevo modelo en la petición `PUT`.

### [Lógica de Negocio]
- **`AgendaViewModel.kt`**: Se implementó una lógica de mapeo que convierte la lista de nombres de días (ej: "Lunes") a valores numéricos (1 o 0) que el servidor puede procesar.

---

## Sugerencia de Backend (PHP) - Código Corregido

Para que la actualización funcione, debes corregir tu script PHP en `api/dias.php`. El error principal estaba en que la consulta SQL tenía **7 marcadores** (`?`) pero intentabas pasar **8 variables** en `bind_param`.

Aquí tienes el código PHP corregido y optimizado:

```php
if($method === 'PUT') {
    $data = get_json_input();

    // Cambiado a 'cl_ide' para coincidir con Android y sap_cl00
    $id = isset($data['cl_ide']) ? intval($data['cl_ide']) : 0;

    if($id <= 0) {
        send_json(["success" => false, "error" => "ID inválido o faltante (cl_ide)"], 400);
    }

    // Corregido: Ahora el UPDATE tiene 7 marcadores '?' para los 7 días
    // Si usas Domingo, asegúrate de que la tabla sap_cl00 tenga esa columna.
    $sql = "UPDATE sap_cl00 SET cl_lun = ?, cl_mar = ?, cl_mie = ?, cl_jue = ?, cl_vie = ?, cl_sab = ?, cl_dom = ? WHERE cl_ide = ?";
    $stmt = prepare_or_fail($con, $sql);

    // Mapeo de campos recibidos desde Android
    $lunes     = isset($data['cl_lun']) ? (int)$data['cl_lun'] : 0;
    $martes    = isset($data['cl_mar']) ? (int)$data['cl_mar'] : 0;
    $miercoles = isset($data['cl_mie']) ? (int)$data['cl_mie'] : 0;
    $jueves    = isset($data['cl_jue']) ? (int)$data['cl_jue'] : 0;
    $viernes   = isset($data['cl_vie']) ? (int)$data['cl_vie'] : 0;
    $sabado    = isset($data['cl_sab']) ? (int)$data['cl_sab'] : 0;
    $domingo   = isset($data['cl_dom']) ? (int)$data['cl_dom'] : 0;

    // Vincular los 8 parámetros (7 días + 1 ID) a los 8 marcadores '?' del SQL corregido
    mysqli_stmt_bind_param($stmt, 'iiiiiiii',
        $lunes,
        $martes,
        $miercoles,
        $jueves,
        $viernes,
        $sabado,
        $domingo,
        $id
    );

    if (mysqli_stmt_execute($stmt)) {
        send_json(["success" => true, "message" => "Días de entrega actualizados"], 200);
    } else {
        send_json(["success" => false, "error" => mysqli_stmt_error($stmt)], 500);
    }
}
```

> [!IMPORTANT]
> He añadido el campo `cl_dom` tanto en Android como en PHP. Asegúrate de que tu tabla `sap_cl00` tenga la columna `cl_dom`. Si no la tiene, simplemente quítala de ambos lados.
