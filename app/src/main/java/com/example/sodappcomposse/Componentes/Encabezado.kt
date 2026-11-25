package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.R

/**
 * Main top bar:app name, signed-in user and the ⋮ menu (light/dark switch; company data,
 * messages and users and roles for the owner; logout).
 */
@Composable
fun Encabezado(
    nombre: String,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    // Ends the session and goes back to login (handled by the Navigator)
    navLogin: () -> Unit,
    // Opens users and roles administration; null hides the entry (only the owner gets it)
    onAdministrar: (() -> Unit)? = null,
    // Opens the company data; null hides the menu entry (only the owner gets it)
    onDatosEmpresa: (() -> Unit)? = null,
    // Opens the WhatsApp message templates; null hides the menu entry (only the owner gets it)
    onMensajes: (() -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_water_drop_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "SodApp",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            // Only "SodApp" takes the free space, so the user and the actions stay on the right
            Text(
                text = nombre,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp)
            )
            HeaderOverflowMenu(
                isDark = isDark,
                onToggleTheme = onToggleTheme,
                onDatosEmpresa = onDatosEmpresa,
                onMensajes = onMensajes,
                onAdministrar = onAdministrar,
                onSalir = navLogin,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
