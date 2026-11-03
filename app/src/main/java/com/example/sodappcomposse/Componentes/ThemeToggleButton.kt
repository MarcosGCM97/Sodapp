package com.example.sodappcomposse.Componentes

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.sodappcomposse.R

/** Light/dark switch for the top bars: shows the mode it switches to. */
@Composable
fun ThemeToggleButton(
    isDark: Boolean,
    onToggle: () -> Unit,
    tint: Color = LocalContentColor.current
) {
    IconButton(onClick = onToggle) {
        Icon(
            painter = painterResource(if (isDark) R.drawable.outline_light_mode_24 else R.drawable.outline_dark_mode_24),
            contentDescription = stringResource(if (isDark) R.string.tema_claro_desc else R.string.tema_oscuro_desc),
            tint = tint
        )
    }
}
