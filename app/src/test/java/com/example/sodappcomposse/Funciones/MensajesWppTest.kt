package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MensajesWppTest {

    // Same templates as strings.xml, so the tests read the real wording
    private val plantillas = mapOf(
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
    )

    private val resources = mockk<Resources> {
        every { getString(any()) } answers { plantillas.getValue(firstArg()) }
        every { getString(any(), *anyVararg()) } answers {
            plantillas.getValue(firstArg()).format(*(args[1] as Array<*>))
        }
    }

    private val productos = listOf(ProductoMensaje(2, "Bidón 20L", 4500.0), ProductoMensaje(1, "Sifón", 800.0))

    @Test
    fun `sale message greets the client, lists products and total, and is signed by the business`() {
        val mensaje = armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "Sodería Ariza")

        assertEquals(
            "¡Hola, Ana! 👋\n\n" +
                "Muchas gracias por tu compra 🛒\n\n" +
                "• 2 Bidón 20L a $4.500,00 c/u\n• 1 Sifón a $800,00 c/u\n\n" +
                "💵 Total: $9.800,00\n\n" +
                "📋 Saldo pendiente: $300,00\n\n" +
                "¡Que tengas un lindo día! 😊\n\n" +
                "Sodería Ariza 💧",
            mensaje
        )
    }

    @Test
    fun `balance message shows a credit balance as a positive amount and is signed by the business`() {
        val mensaje = armarMensajeSaldo(resources, "Ana", -700.0, "Sodería Ariza")

        assertEquals(
            "¡Hola, Ana! 👋\n\n" +
                "Te compartimos el estado de tu cuenta:\n\n" +
                "🎉 Saldo a favor: $700,00\n\n" +
                "Cualquier consulta, estamos para ayudarte 🙌\n\n" +
                "Sodería Ariza 💧",
            mensaje
        )
    }

    @Test
    fun `a zero balance says the account is up to date`() {
        assertTrue(armarMensajeSaldo(resources, "Ana", 0.0, "X").contains("✅ Tu cuenta está al día."))
        assertTrue(armarMensajeVenta(resources, "Ana", productos, 1.0, null, "X").contains("✅ Tu cuenta está al día."))
    }

    @Test
    fun `missing client or business name falls back to a plain greeting and no signature`() {
        val mensaje = armarMensajeSaldo(resources, " ", 100.0, null)

        assertTrue(mensaje.startsWith("¡Hola! 👋"))
        assertTrue(mensaje.endsWith("Cualquier consulta, estamos para ayudarte 🙌"))
    }

    @Test
    fun `no message uses the word deuda`() {
        val mensajes = listOf(
            armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "X"),
            armarMensajeSaldo(resources, "Ana", 300.0, "X"),
        ) + plantillas.values

        mensajes.forEach { assertFalse(it, it.contains("deuda", ignoreCase = true)) }
    }
}
