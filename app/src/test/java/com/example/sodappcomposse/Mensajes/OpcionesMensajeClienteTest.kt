package com.example.sodappcomposse.Mensajes

import android.content.res.Resources
import com.example.sodappcomposse.Funciones.EnvaseMensaje
import com.example.sodappcomposse.Funciones.ProductoMensaje
import com.example.sodappcomposse.Funciones.armarMensajeEnvases
import com.example.sodappcomposse.Funciones.armarMensajeSaldo
import com.example.sodappcomposse.Funciones.armarMensajeVenta
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class OpcionesMensajeClienteTest {

    private val textos = mapOf(
        R.string.wpp_saludo to "¡Hola! 👋",
        R.string.wpp_saludo_nombre to "¡Hola, %1\$s! 👋",
        R.string.wpp_gracias_compra to "Muchas gracias por tu compra 🛒",
        R.string.wpp_producto_format to "• %1\$d %2\$s a $%3\$s c/u",
        R.string.wpp_total_format to "💵 Total: $%1\$s",
        R.string.wpp_saldo_pendiente_format to "📋 Saldo pendiente: $%1\$s",
        R.string.wpp_saldo_a_favor_format to "🎉 Saldo a favor: $%1\$s",
        R.string.wpp_al_dia to "✅ Tu cuenta está al día.",
        R.string.wpp_despedida to "¡Que tengas un lindo día! 😊",
        R.string.wpp_estado_cuenta to "Te compartimos el estado de tu cuenta:",
        R.string.wpp_consultas to "Cualquier consulta, estamos para ayudarte 🙌",
        R.string.wpp_firma_format to "%1\$s 💧",
        R.string.wpp_envases_intro to "Te recordamos los envases retornables que quedaron en tu domicilio:",
        R.string.wpp_envase_format to "• %1\$d %2\$s",
    )

    private val resources = mockk<Resources> {
        every { getString(any()) } answers { textos.getValue(firstArg()) }
        every { getString(any(), *anyVararg()) } answers { textos.getValue(firstArg()).format(*(args[1] as Array<*>)) }
    }

    private val envases = listOf(EnvaseMensaje(2, "Bidón 20L"))
    private val ventas = listOf(
        Venta(producto = "Sifón", precio = 800.0, cantidad = 1, fecha = "2026-11-20 10:00:00", monto = 800.0),
        Venta(producto = "Bidón 20L", precio = 5000.0, cantidad = 2, fecha = "2026-11-21 09:30:00", monto = 9000.0),
    )

    @Test
    fun `offers the three messages in order with the default texts`() {
        val opciones = opcionesMensajeCliente(resources, "Ana", 300.0, envases, ventas, "Sodería", emptyMap())

        assertEquals(
            listOf(
                OpcionMensaje(TipoPlantilla.DEUDA, armarMensajeSaldo(resources, "Ana", 300.0, "Sodería")),
                OpcionMensaje(TipoPlantilla.ENVASES, armarMensajeEnvases(resources, "Ana", envases, "Sodería")),
                OpcionMensaje(
                    TipoPlantilla.VENTA,
                    armarMensajeVenta(resources, "Ana", listOf(ProductoMensaje(2, "Bidón 20L", 4500.0)), 9000.0, 300.0, "Sodería")
                ),
            ),
            opciones
        )
    }

    @Test
    fun `uses the owner template of each type`() {
        val plantillas = mapOf(TipoPlantilla.DEUDA to "{cliente} {saldo}", TipoPlantilla.VENTA to "{total}")

        val opciones = opcionesMensajeCliente(resources, "Ana", 300.0, envases, ventas, "Sodería", plantillas)

        assertEquals("Ana 📋 Saldo pendiente: $300,00", opciones[0].mensaje)
        assertEquals(armarMensajeEnvases(resources, "Ana", envases, "Sodería"), opciones[1].mensaje)
        assertEquals("$9.000,00", opciones[2].mensaje)
    }

    @Test
    fun `envases and venta are disabled without containers or sales, the balance never`() {
        val opciones = opcionesMensajeCliente(resources, "Ana", 0.0, listOf(EnvaseMensaje(0, "Sifón")), emptyList(), null, emptyMap())

        assertEquals(listOf(TipoPlantilla.DEUDA, TipoPlantilla.ENVASES, TipoPlantilla.VENTA), opciones.map { it.tipo })
        assertEquals(armarMensajeSaldo(resources, "Ana", 0.0, null), opciones[0].mensaje)
        assertEquals(null, opciones[1].mensaje)
        assertEquals(null, opciones[2].mensaje)
    }
}
