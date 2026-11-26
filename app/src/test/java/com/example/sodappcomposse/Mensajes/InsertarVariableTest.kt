package com.example.sodappcomposse.Mensajes

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertarVariableTest {

    @Test
    fun `inserts the variable at the cursor and moves the cursor after it`() {
        val resultado = insertarVariable(TextFieldValue("Hola !", TextRange(5)), "cliente")

        assertEquals("Hola {cliente}!", resultado.text)
        assertEquals(TextRange(14), resultado.selection)
    }

    @Test
    fun `replaces the selected text, whatever its direction`() {
        assertEquals("Hola {cliente}", insertarVariable(TextFieldValue("Hola Ana", TextRange(5, 8)), "cliente").text)
        assertEquals("Hola {cliente}", insertarVariable(TextFieldValue("Hola Ana", TextRange(8, 5)), "cliente").text)
    }

    @Test
    fun `never goes over the 1000 characters of mensajes php`() {
        val lleno = TextFieldValue("a".repeat(995), TextRange(995))

        assertEquals(lleno, insertarVariable(lleno, "cliente"))
        assertEquals(1000, insertarVariable(TextFieldValue("a".repeat(991)), "cliente").text.length)
    }
}
