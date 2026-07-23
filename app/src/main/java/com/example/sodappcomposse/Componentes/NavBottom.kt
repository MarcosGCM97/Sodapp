package com.example.sodappcomposse.Componentes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.SodAppComposseTheme

@Composable
fun NavBottom(
    currentRoute: ContenidoBienvenida,
    onContenidoSeleccionado: (ContenidoBienvenida) -> Unit){
    val selectedBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    val unselectedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 40.dp)
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Top,

    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ítem VENTAS
            CustomNavIconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.VENTAS) },
                isSelected = currentRoute == ContenidoBienvenida.VENTAS,
                selectedBackgroundColor = selectedBackgroundColor,
                unselectedBackgroundColor = unselectedBackgroundColor,
                selectedIconColor = selectedIconColor,
                unselectedIconColor = unselectedIconColor,
                iconResId = R.drawable.outline_format_list_bulleted_24,
                contentDescription = "Ventas"
            )

            // Ítem CLIENTES
            CustomNavIconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.CLIENTES) },
                isSelected = currentRoute == ContenidoBienvenida.CLIENTES,
                selectedBackgroundColor = selectedBackgroundColor,
                unselectedBackgroundColor = unselectedBackgroundColor,
                selectedIconColor = selectedIconColor,
                unselectedIconColor = unselectedIconColor,
                iconResId = R.drawable.outline_person_add_24,
                contentDescription = "Clientes"
            )

            // Ítem STOCK
            CustomNavIconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.STOCK) },
                isSelected = currentRoute == ContenidoBienvenida.STOCK,
                selectedBackgroundColor = selectedBackgroundColor,
                unselectedBackgroundColor = unselectedBackgroundColor,
                selectedIconColor = selectedIconColor,
                unselectedIconColor = unselectedIconColor,
                iconResId = R.drawable.outline_stacked_inbox_24,
                contentDescription = "Stock"
            )

            // Ítem CAJA
            CustomNavIconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.CAJA) },
                isSelected = currentRoute == ContenidoBienvenida.CAJA,
                selectedBackgroundColor = selectedBackgroundColor,
                unselectedBackgroundColor = unselectedBackgroundColor,
                selectedIconColor = selectedIconColor,
                unselectedIconColor = unselectedIconColor,
                iconResId = R.drawable.outline_money_bag_24,
                contentDescription = "Caja"
            )
        }
    }
}

@Composable
private fun CustomNavIconButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    selectedBackgroundColor: Color,
    unselectedBackgroundColor: Color,
    selectedIconColor: Color,
    unselectedIconColor: Color,
    @DrawableRes iconResId: Int,
    contentDescription: String
) {
    val backgroundColor = if (isSelected) selectedBackgroundColor else unselectedBackgroundColor
    val iconColor = if (isSelected) selectedIconColor else unselectedIconColor

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(60.dp) // Tamaño del área del botón
            // Usamos .clip antes de .background para asegurar que el fondo respete la forma
            .clip(CircleShape)
            .background(color = backgroundColor) // Ya no se necesita el shape aquí debido al clip
            // Opcional: añadir un borde si está seleccionado
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                else Modifier
            )
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp), // Tamaño del ícono en sí
            tint = iconColor // Aplicar el color del ícono
        )
    }
}


// --- Para el Preview ---
@Preview(showBackground = true, name = "NavBottom Original Style Light")
@Composable
fun NavBottomOriginalStylePreviewLight() {
    SodAppComposseTheme(darkTheme = false) {
        var currentRoutePreview by remember { mutableStateOf(ContenidoBienvenida.VENTAS) }
        NavBottom(
            currentRoute = currentRoutePreview,
            onContenidoSeleccionado = { currentRoutePreview = it }
        )
    }
}

@Preview(showBackground = true, name = "NavBottom Original Style Dark")
@Composable
fun NavBottomOriginalStylePreviewDark() {
    SodAppComposseTheme(darkTheme = true) {
        var currentRoutePreview by remember { mutableStateOf(ContenidoBienvenida.CLIENTES) }
        NavBottom(
            currentRoute = currentRoutePreview,
            onContenidoSeleccionado = { currentRoutePreview = it }
        )
    }
}