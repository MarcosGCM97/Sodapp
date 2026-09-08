package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeToggleFAB(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    SmallFloatingActionButton(
        onClick = onToggle,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .padding(16.dp)
            .size(40.dp) // Tamaño reducido
    ) {
        Icon(
            imageVector = if (isDark) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = if (isDark) "Pasar a modo claro" else "Pasar a modo oscuro"
        )
    }
}
