package com.example.sodappcomposse.auth

import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.PestanaClientes
import com.example.sodappcomposse.PestanaVentas

/*
 * What the UI shows for a set of permissions. Only hides UI: the backend enforces
 * every permission per endpoint.
 */

/** Tabs of Ventas the user can open: Ventas needs ventas.ver, Reparto needs repartos.ver. */
fun pestanasVentas(permisos: SessionPermissions): List<PestanaVentas> = buildList {
    if (permisos.tiene(Permiso.VENTAS_VER)) add(PestanaVentas.VENTAS)
    if (permisos.tiene(Permiso.REPARTOS_VER)) add(PestanaVentas.REPARTO)
}

/** Tabs of Clientes the user can open: Clientes needs clientes.ver, Cobros needs cobros.ver. */
fun pestanasClientes(permisos: SessionPermissions): List<PestanaClientes> = buildList {
    if (permisos.tiene(Permiso.CLIENTES_VER)) add(PestanaClientes.CLIENTES)
    if (permisos.tiene(Permiso.COBROS_VER)) add(PestanaClientes.COBROS)
}

/** Home sections in bottom bar order; a section shows when at least one of its tabs does. */
fun seccionesVisibles(permisos: SessionPermissions): List<ContenidoBienvenida> = buildList {
    if (pestanasVentas(permisos).isNotEmpty()) add(ContenidoBienvenida.VENTAS)
    if (pestanasClientes(permisos).isNotEmpty()) add(ContenidoBienvenida.CLIENTES)
    if (permisos.tiene(Permiso.STOCK_VER)) add(ContenidoBienvenida.STOCK)
    if (permisos.tiene(Permiso.CAJA_VER)) add(ContenidoBienvenida.CAJA)
}

/** [actual] when the user can see it, else the first visible section; null when there is none. */
fun seccionInicial(permisos: SessionPermissions, actual: ContenidoBienvenida): ContenidoBienvenida? {
    val visibles = seccionesVisibles(permisos)
    return if (actual in visibles) actual else visibles.firstOrNull()
}

/** [seleccionada] when it is among [visibles], else the first visible tab. */
fun <T> pestanaVisible(seleccionada: T, visibles: List<T>): T =
    if (seleccionada in visibles) seleccionada else visibles.first()

/** Only the owner administers users and roles. */
fun puedeAdministrar(permisos: SessionPermissions): Boolean = permisos.esDueno
