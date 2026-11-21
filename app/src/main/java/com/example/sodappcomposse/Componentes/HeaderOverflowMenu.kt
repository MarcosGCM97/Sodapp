package com.example.sodappcomposse.Componentes

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.sodappcomposse.R

/** Options of the ⋮ menu of the top bars. */
enum class OpcionMenuEncabezado {
    TEMA,
    DATOS_EMPRESA;

    /** The theme option names the mode it switches to, like the old toggle button. */
    @StringRes
    fun textoRes(isDark: Boolean): Int = when (this) {
        TEMA -> if (isDark) R.string.tema_claro_desc else R.string.tema_oscuro_desc
        DATOS_EMPRESA -> R.string.empresa_titulo
    }

    /** Drawable of the option; null when it uses a Material icon. */
    @DrawableRes
    fun iconoRes(isDark: Boolean): Int? = when (this) {
        TEMA -> if (isDark) R.drawable.outline_light_mode_24 else R.drawable.outline_dark_mode_24
        DATOS_EMPRESA -> null
    }
}

/** Menu options in order; the company data only for whoever can edit it (the owner). */
fun opcionesMenuEncabezado(puedeEditarEmpresa: Boolean): List<OpcionMenuEncabezado> = buildList {
    add(OpcionMenuEncabezado.TEMA)
    if (puedeEditarEmpresa) add(OpcionMenuEncabezado.DATOS_EMPRESA)
}

/**
 * ⋮ button of the top bars with the light/dark switch and, when [onDatosEmpresa] is not null,
 * the company data entry. Presentational: the caller decides what each option does.
 */
@Composable
fun HeaderOverflowMenu(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onDatosEmpresa: (() -> Unit)? = null,
    tint: Color = LocalContentColor.current
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { abierto = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.mas_opciones_desc),
                tint = tint
            )
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opcionesMenuEncabezado(puedeEditarEmpresa = onDatosEmpresa != null).forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(stringResource(opcion.textoRes(isDark))) },
                    leadingIcon = {
                        val icono = opcion.iconoRes(isDark)
                        if (icono != null) {
                            Icon(painter = painterResource(icono), contentDescription = null)
                        } else {
                            Icon(imageVector = Icons.Filled.Home, contentDescription = null)
                        }
                    },
                    onClick = {
                        abierto = false
                        when (opcion) {
                            OpcionMenuEncabezado.TEMA -> onToggleTheme()
                            OpcionMenuEncabezado.DATOS_EMPRESA -> onDatosEmpresa?.invoke()
                        }
                    }
                )
            }
        }
    }
}
