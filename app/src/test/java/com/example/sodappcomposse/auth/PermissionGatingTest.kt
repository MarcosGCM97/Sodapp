package com.example.sodappcomposse.auth

import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.PestanaClientes
import com.example.sodappcomposse.PestanaVentas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionGatingTest {

    private val dueno = SessionPermissions(esDueno = true)

    // Default "Vendedor" role seeded by the backend
    private val vendedor = SessionPermissions(
        esDueno = false,
        permisos = setOf(
            Permiso.VENTAS_VER, Permiso.VENTAS_CARGAR,
            Permiso.REPARTOS_VER, Permiso.REPARTOS_GESTIONAR,
            Permiso.CLIENTES_VER, Permiso.CLIENTES_EDITAR
        )
    )

    @Test
    fun `the owner sees every section in the bottom bar order`() {
        assertEquals(
            listOf(ContenidoBienvenida.VENTAS, ContenidoBienvenida.CLIENTES, ContenidoBienvenida.STOCK, ContenidoBienvenida.CAJA),
            seccionesVisibles(dueno)
        )
        assertEquals(listOf(PestanaVentas.VENTAS, PestanaVentas.REPARTO), pestanasVentas(dueno))
        assertEquals(listOf(PestanaClientes.CLIENTES, PestanaClientes.COBROS), pestanasClientes(dueno))
        assertTrue(puedeAdministrar(dueno))
    }

    @Test
    fun `the owner has every permission even without codes`() {
        assertTrue(dueno.tiene(Permiso.VENTAS_ELIMINAR))
        assertTrue(dueno.tiene(Permiso.CAJA_VER))
    }

    @Test
    fun `the default vendedor sees Ventas, Reparto and Clientes only`() {
        assertEquals(listOf(ContenidoBienvenida.VENTAS, ContenidoBienvenida.CLIENTES), seccionesVisibles(vendedor))
        assertEquals(listOf(PestanaVentas.VENTAS, PestanaVentas.REPARTO), pestanasVentas(vendedor))
        assertEquals(listOf(PestanaClientes.CLIENTES), pestanasClientes(vendedor))
        assertFalse(vendedor.tiene(Permiso.COBROS_VER))
        assertFalse(vendedor.tiene(Permiso.STOCK_VER))
        assertFalse(vendedor.tiene(Permiso.CAJA_VER))
        assertFalse(vendedor.tiene(Permiso.VENTAS_ELIMINAR))
        assertFalse(puedeAdministrar(vendedor))
    }

    @Test
    fun `a tab permission alone shows its section`() {
        val soloReparto = SessionPermissions(permisos = setOf(Permiso.REPARTOS_VER))
        val soloCobros = SessionPermissions(permisos = setOf(Permiso.COBROS_VER))

        assertEquals(listOf(ContenidoBienvenida.VENTAS), seccionesVisibles(soloReparto))
        assertEquals(listOf(PestanaVentas.REPARTO), pestanasVentas(soloReparto))
        assertEquals(listOf(ContenidoBienvenida.CLIENTES), seccionesVisibles(soloCobros))
        assertEquals(listOf(PestanaClientes.COBROS), pestanasClientes(soloCobros))
    }

    @Test
    fun `the initial section keeps the current one when visible`() {
        assertEquals(ContenidoBienvenida.CLIENTES, seccionInicial(vendedor, ContenidoBienvenida.CLIENTES))
    }

    @Test
    fun `the initial section falls back to the first visible one`() {
        val soloCaja = SessionPermissions(permisos = setOf(Permiso.CAJA_VER))

        assertEquals(ContenidoBienvenida.VENTAS, seccionInicial(vendedor, ContenidoBienvenida.CAJA))
        assertEquals(ContenidoBienvenida.CAJA, seccionInicial(soloCaja, ContenidoBienvenida.VENTAS))
    }

    @Test
    fun `a user without permissions has no section`() {
        val sinPermisos = SessionPermissions()

        assertTrue(seccionesVisibles(sinPermisos).isEmpty())
        assertNull(seccionInicial(sinPermisos, ContenidoBienvenida.VENTAS))
    }

    @Test
    fun `the selected tab falls back to the first visible tab`() {
        assertEquals(PestanaClientes.CLIENTES, pestanaVisible(PestanaClientes.COBROS, pestanasClientes(vendedor)))
        assertEquals(PestanaVentas.REPARTO, pestanaVisible(PestanaVentas.REPARTO, pestanasVentas(vendedor)))
    }

    @Test
    fun `collecting a payment needs both cobros permissions`() {
        assertTrue(puedeCobrar(dueno))
        assertFalse(puedeCobrar(vendedor))
        val soloCargar = SessionPermissions(esDueno = false, permisos = setOf(Permiso.COBROS_CARGAR))
        assertFalse(puedeCobrar(soloCargar))
        val ambos = SessionPermissions(esDueno = false, permisos = setOf(Permiso.COBROS_VER, Permiso.COBROS_CARGAR))
        assertTrue(puedeCobrar(ambos))
    }
}
