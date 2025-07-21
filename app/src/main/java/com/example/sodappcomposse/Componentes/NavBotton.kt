package com.example.sodappcomposse.Componentes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.ContenidoBienvenida
import com.example.sodappcomposse.R

@Composable
fun NavBottom(onContenidoSeleccionado: (ContenidoBienvenida) -> Unit){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.8f))
            .padding(top = 10.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Top,

    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.VENTAS) },
                modifier = Modifier
                    .size(60.dp)
                    .border(3.dp, Color.Black, shape = RoundedCornerShape(50.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_format_list_bulleted_24),
                    contentDescription = "Lista/Agregar Ventas",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            IconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.CLIENTES) },
                modifier = Modifier
                    .size(60.dp)
                    .border(3.dp, Color.Black, shape = RoundedCornerShape(50.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_person_add_24),
                    contentDescription = "Lista/Agregar Clientes",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            IconButton(
                onClick = { onContenidoSeleccionado(ContenidoBienvenida.STOCK) },
                modifier = Modifier
                    .size(60.dp)
                    .border(3.dp, Color.Black, shape = RoundedCornerShape(50.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_stacked_inbox_24),
                    contentDescription = "Lista/Agregar Productos",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
