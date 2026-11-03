package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.Normalizer

// Lowercase without accents, so "jose" finds "José"
private fun normalizar(texto: String): String =
    Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

/** Options whose label contains [texto], ignoring case and accents; all of them for a blank text. */
fun <T> filtrarOpciones(opciones: List<T>, texto: String, etiqueta: (T) -> String): List<T> {
    val buscado = normalizar(texto)
    if (buscado.isEmpty()) return opciones
    return opciones.filter { normalizar(etiqueta(it)).contains(buscado) }
}

/**
 * Select with search: open the list with the arrow, or type to filter it by [etiqueta].
 * With [limpiarAlSeleccionar] the field empties after each choice (to add several items);
 * otherwise it shows the chosen option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectBuscable(
    opciones: List<T>,
    etiqueta: (T) -> String,
    onSeleccionar: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    limpiarAlSeleccionar: Boolean = false
) {
    var texto by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }
    val filtradas = filtrarOpciones(opciones, texto, etiqueta)

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = {
                texto = it
                expandido = true
            },
            label = { Text(label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        if (filtradas.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false },
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                filtradas.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(etiqueta(opcion)) },
                        onClick = {
                            texto = if (limpiarAlSeleccionar) "" else etiqueta(opcion)
                            expandido = false
                            onSeleccionar(opcion)
                        }
                    )
                }
            }
        }
    }
}
