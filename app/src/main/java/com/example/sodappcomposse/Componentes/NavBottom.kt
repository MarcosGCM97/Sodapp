package com.example.sodappcomposse.Componentes

import androidx.compose.material3.MaterialTheme
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.SodAppComposseTheme

// Colores personalizados según especificación
private val NavyBackground = Color(0xFF1E2A38)
private val PurpleAccent = Color(0xFF6A5ACD)
private val GrayInactive = Color(0xFFC8CFD8)

@Composable
fun NavBottom(
    currentRoute: ContenidoBienvenida,
    onContenidoSeleccionado: (ContenidoBienvenida) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface) // Usar surface para adaptarse al tema
            .navigationBarsPadding() 
            .padding(top = 18.dp, bottom = 18.dp, start = 8.dp, end = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ítem VENTAS
            ExpandableNavItem(
                label = "Ventas",
                iconResId = R.drawable.outline_format_list_bulleted_24,
                isSelected = currentRoute == ContenidoBienvenida.VENTAS,
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.VENTAS) }
            )

            // Ítem CLIENTES
            ExpandableNavItem(
                label = "Clientes",
                iconResId = R.drawable.outline_person_add_24,
                isSelected = currentRoute == ContenidoBienvenida.CLIENTES,
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.CLIENTES) }
            )

            // Ítem STOCK (Inventario)
            ExpandableNavItem(
                label = "Inventario",
                iconResId = R.drawable.outline_stacked_inbox_24,
                isSelected = currentRoute == ContenidoBienvenida.STOCK,
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.STOCK) }
            )

            // Ítem CAJA
            ExpandableNavItem(
                label = "Caja",
                iconResId = R.drawable.outline_money_bag_24,
                isSelected = currentRoute == ContenidoBienvenida.CAJA,
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.CAJA) }
            )
        }
    }
}

@Composable
private fun ExpandableNavItem(
    label: String,
    @DrawableRes iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animación de color de fondo (de transparente a morado)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PurpleAccent else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "pillBackground"
    )

    // Animación de color de icono
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else GrayInactive,
        animationSpec = tween(durationMillis = 300),
        label = "contentColor"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .animateContentSize(
                animationSpec = tween(durationMillis = 300)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "NavBottom Expandible - Ventas Seleccionada")
@Composable
fun NavBottomExpandablePreview() {
    var currentRoutePreview by remember { mutableStateOf(ContenidoBienvenida.VENTAS) }
    SodAppComposseTheme {
        NavBottom(
            currentRoute = currentRoutePreview,
            onContenidoSeleccionado = { currentRoutePreview = it }
        )
    }
}

@Preview(showBackground = true, name = "NavBottom Expandible - Clientes Seleccionada")
@Composable
fun NavBottomExpandableClientsPreview() {
    var currentRoutePreview by remember { mutableStateOf(ContenidoBienvenida.CLIENTES) }
    SodAppComposseTheme {
        NavBottom(
            currentRoute = currentRoutePreview,
            onContenidoSeleccionado = { currentRoutePreview = it }
        )
    }
}
