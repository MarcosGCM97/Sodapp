package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

/** Single-line hidden password field with the password keyboard. */
@Composable
fun CampoContrasena(valor: String, onCambiar: (String) -> Unit, etiqueta: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}
