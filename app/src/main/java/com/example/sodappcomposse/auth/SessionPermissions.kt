package com.example.sodappcomposse.auth

/**
 * What the logged-in user may do, as sent by login.php and sesion.php.
 *
 * The owner of the company ([esDueno]) implicitly has every permission plus user and
 * role administration. The backend enforces permissions per endpoint; the app only uses
 * them to hide what the user cannot use.
 */
data class SessionPermissions(
    val esDueno: Boolean = false,
    val permisos: Set<String> = emptySet()
) {
    fun tiene(permiso: String): Boolean = esDueno || permiso in permisos
}

/** Permission codes of the backend catalog (sap_pm00.pm_cod). */
object Permiso {
    const val VENTAS_VER = "ventas.ver"
    const val VENTAS_CARGAR = "ventas.cargar"
    const val VENTAS_ELIMINAR = "ventas.eliminar"
    const val REPARTOS_VER = "repartos.ver"
    const val REPARTOS_GESTIONAR = "repartos.gestionar"
    const val CLIENTES_VER = "clientes.ver"
    const val CLIENTES_EDITAR = "clientes.editar"
    const val COBROS_VER = "cobros.ver"
    const val COBROS_CARGAR = "cobros.cargar"
    const val STOCK_VER = "stock.ver"
    const val STOCK_EDITAR = "stock.editar"
    const val CAJA_VER = "caja.ver"
}
