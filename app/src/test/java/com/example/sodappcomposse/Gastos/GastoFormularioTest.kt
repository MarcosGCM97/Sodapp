package com.example.sodappcomposse.Gastos

import com.example.sodappcomposse.Producto.Producto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GastoFormularioTest {

    private val fecha = LocalDate.of(2026, 11, 20)
    private val bidon = Producto(id = 3, nombrePr = "Bidón", retornable = 1)
    private val vaso = Producto(id = 4, nombrePr = "Vaso", retornable = 0)

    private fun armar(
        categoria: CategoriaGasto,
        monto: String = "1500,50",
        producto: Producto? = null,
        cantidad: String = "",
        estado: EstadoEnvase? = null
    ) = armarGasto(null, categoria, monto, fecha, "Pinchadura", 8, producto, cantidad, estado)

    @Test
    fun `an expense without stock needs only a positive amount`() {
        assertEquals(
            NuevoGasto(categoria = CategoriaGasto.VEHICULO, monto = 1500.5, fecha = "2026-11-20", descripcion = "Pinchadura", repartoId = 8),
            armar(CategoriaGasto.VEHICULO)
        )
        assertNull(armar(CategoriaGasto.VEHICULO, monto = "0"))
        assertNull(armar(CategoriaGasto.VEHICULO, monto = "mucho"))
    }

    @Test
    fun `a purchase needs product and quantity, and full or empty for a returnable one`() {
        assertNull(armar(CategoriaGasto.COMPRA_PRODUCTO, cantidad = "4"))
        assertNull(armar(CategoriaGasto.COMPRA_PRODUCTO, producto = vaso, cantidad = "0"))
        assertNull(armar(CategoriaGasto.COMPRA_PRODUCTO, producto = vaso, cantidad = "100001"))
        assertNull(armar(CategoriaGasto.COMPRA_PRODUCTO, producto = bidon, cantidad = "4"))
        assertEquals(EstadoEnvase.VACIO, armar(CategoriaGasto.COMPRA_PRODUCTO, producto = bidon, cantidad = "4", estado = EstadoEnvase.VACIO)?.estado)
        val noRetornable = armar(CategoriaGasto.COMPRA_PRODUCTO, producto = vaso, cantidad = "6", estado = EstadoEnvase.VACIO)
        assertEquals(4, noRetornable?.productoId)
        assertEquals(6, noRetornable?.cantidad)
        assertNull(noRetornable?.estado)
    }

    @Test
    fun `a refill only takes returnable products and no state`() {
        assertNull(armar(CategoriaGasto.RECARGA_ENVASES, producto = vaso, cantidad = "2"))
        val recarga = armar(CategoriaGasto.RECARGA_ENVASES, producto = bidon, cantidad = "2", estado = EstadoEnvase.LLENO)
        assertEquals(3, recarga?.productoId)
        assertNull(recarga?.estado)
        assertEquals(listOf(bidon), productosDeCategoria(CategoriaGasto.RECARGA_ENVASES, listOf(bidon, vaso)))
        assertEquals(listOf(bidon, vaso), productosDeCategoria(CategoriaGasto.COMPRA_PRODUCTO, listOf(bidon, vaso)))
    }

    @Test
    fun `without stock editar the stock categories are not offered`() {
        assertEquals(
            listOf(CategoriaGasto.VEHICULO, CategoriaGasto.COMBUSTIBLE, CategoriaGasto.OTRO),
            categoriasDisponibles(puedeMoverStock = false)
        )
        assertEquals(CategoriaGasto.entries.toList(), categoriasDisponibles(puedeMoverStock = true))
    }
}
