package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.Mensajes.TipoPlantilla
import com.example.sodappcomposse.R
import com.example.sodappcomposse.Ventas.Venta
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
        R.string.wpp_envases_intro to "Te recordamos los envases retornables que quedaron en tu domicilio:",
        R.string.wpp_envase_format to "• %1\$d %2\$s",
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

    // ---------------------------------------------------------------- envases

    private val envases = listOf(EnvaseMensaje(3, "Bidón 20L"), EnvaseMensaje(0, "Sifón"), EnvaseMensaje(1, "Bidón 12L"))

    @Test
    fun `containers message lists only the containers the client keeps and is signed by the business`() {
        val mensaje = armarMensajeEnvases(resources, "Ana", envases, "Sodería Ariza")

        assertEquals(
            "¡Hola, Ana! 👋\n\n" +
                "Te recordamos los envases retornables que quedaron en tu domicilio:\n\n" +
                "• 3 Bidón 20L\n• 1 Bidón 12L\n\n" +
                "Cualquier consulta, estamos para ayudarte 🙌\n\n" +
                "Sodería Ariza 💧",
            mensaje
        )
        assertFalse(mensaje.contains("deuda", ignoreCase = true))
    }

    @Test
    fun `tieneEnvases is false when every container balance is zero or negative`() {
        assertTrue(tieneEnvases(envases))
        assertFalse(tieneEnvases(listOf(EnvaseMensaje(0, "Sifón"), EnvaseMensaje(-2, "Bidón 20L"))))
        assertFalse(tieneEnvases(emptyList()))
    }

    // ---------------------------------------------------------------- templates

    @Test
    fun `a null or blank template keeps the default text`() {
        assertEquals(armarMensajeSaldo(resources, "Ana", 300.0, "X"), armarMensajeSaldo(resources, "Ana", 300.0, "X", plantilla = null))
        assertEquals(armarMensajeSaldo(resources, "Ana", 300.0, "X"), armarMensajeSaldo(resources, "Ana", 300.0, "X", plantilla = "  "))
        assertEquals(
            armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "X"),
            armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "X", plantilla = null)
        )
        assertEquals(armarMensajeEnvases(resources, "Ana", envases, "X"), armarMensajeEnvases(resources, "Ana", envases, "X", plantilla = null))
    }

    @Test
    fun `balance template replaces cliente, saldo and empresa`() {
        val mensaje = armarMensajeSaldo(resources, " Ana ", 300.0, "Sodería Ariza", plantilla = "Hola {cliente}. {saldo} -- {empresa}")

        assertEquals("Hola Ana. 📋 Saldo pendiente: $300,00 -- Sodería Ariza", mensaje)
    }

    @Test
    fun `sale template replaces productos, total and saldo`() {
        val mensaje = armarMensajeVenta(
            resources, "Ana", productos, 9800.0, -700.0, "Sodería Ariza",
            plantilla = "{cliente} compró:\n{productos}\nTotal {total}\n{saldo}\n{empresa}"
        )

        assertEquals(
            "Ana compró:\n• 2 Bidón 20L a $4.500,00 c/u\n• 1 Sifón a $800,00 c/u\nTotal $9.800,00\n🎉 Saldo a favor: $700,00\nSodería Ariza",
            mensaje
        )
    }

    @Test
    fun `containers template replaces envases`() {
        val mensaje = armarMensajeEnvases(resources, "Ana", envases, "Sodería Ariza", plantilla = "{cliente}: {envases} ({empresa})")

        assertEquals("Ana: • 3 Bidón 20L\n• 1 Bidón 12L (Sodería Ariza)", mensaje)
    }

    @Test
    fun `missing client and business names leave their variables empty`() {
        assertEquals(
            "Hola . ✅ Tu cuenta está al día. ",
            armarMensajeSaldo(resources, null, 0.0, " ", plantilla = "Hola {cliente}. {saldo} {empresa}")
        )
    }

    @Test
    fun `variables of another type, unknown words and loose braces stay as written`() {
        val mensaje = armarMensajeSaldo(resources, "Ana", 0.0, "X", plantilla = "{productos} {nombre} { cliente } {}")

        assertEquals("{productos} {nombre} { cliente } {}", mensaje)
    }

    @Test
    fun `the base template of each type renders the default message`() {
        assertEquals(
            armarMensajeSaldo(resources, "Ana", 300.0, "Sodería Ariza"),
            armarMensajeSaldo(resources, "Ana", 300.0, "Sodería Ariza", plantilla = plantillaBase(resources, TipoPlantilla.DEUDA))
        )
        assertEquals(
            armarMensajeEnvases(resources, "Ana", envases, "Sodería Ariza"),
            armarMensajeEnvases(resources, "Ana", envases, "Sodería Ariza", plantilla = plantillaBase(resources, TipoPlantilla.ENVASES))
        )
        assertEquals(
            armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "Sodería Ariza"),
            armarMensajeVenta(resources, "Ana", productos, 9800.0, 300.0, "Sodería Ariza", plantilla = plantillaBase(resources, TipoPlantilla.VENTA))
        )
    }

    @Test
    fun `base templates use exactly the variables of their type`() {
        TipoPlantilla.entries.forEach { tipo ->
            val usadas = Regex("\\{([A-Za-z_]+)\\}").findAll(plantillaBase(resources, tipo)).map { it.groupValues[1] }.toSet()
            assertEquals(tipo.name, tipo.variables.toSet(), usadas)
        }
    }

    // ---------------------------------------------------------------- last sale

    @Test
    fun `ultimaVenta takes the rows of the latest date with the price charged at the sale`() {
        val ventas = listOf(
            Venta(producto = "Bidón 20L", precio = 5000.0, cantidad = 2, fecha = "2026-11-20 10:00:00", monto = 9000.0),
            Venta(producto = "Sifón", precio = 800.0, cantidad = 1, fecha = "2026-11-20 10:00:00", monto = 800.0),
            Venta(producto = "Bidón 12L", precio = 3000.0, cantidad = 1, fecha = "2026-11-21 09:30:00", monto = 3000.0),
            Venta(producto = "Bidón 20L", precio = 5000.0, cantidad = 1, fecha = "2026-11-21 09:30:00", monto = 4500.0),
        )

        assertEquals(
            UltimaVenta(
                productos = listOf(ProductoMensaje(1, "Bidón 12L", 3000.0), ProductoMensaje(1, "Bidón 20L", 4500.0)),
                total = 7500.0
            ),
            ultimaVenta(ventas)
        )
    }

    @Test
    fun `ultimaVenta uses the product price when the line has no amount, and is null without sales`() {
        val ventas = listOf(Venta(producto = "Sifón", precio = 800.0, cantidad = 2, fecha = "2026-11-21 09:30:00", monto = 0.0))

        assertEquals(UltimaVenta(listOf(ProductoMensaje(2, "Sifón", 800.0)), 1600.0), ultimaVenta(ventas))
        assertEquals(null, ultimaVenta(emptyList()))
    }
}
