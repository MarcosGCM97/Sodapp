package com.example.sodappcomposse.Componentes

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Funciones.openWhatsApp
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.GreenPrimario

/** Compact WhatsApp button (icon + "Enviar") for cards with little room, like a sale. */
@Composable
fun BotonWpp(
    context: Context,
    cliente: Cliente?,
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { openWhatsApp(context, cliente?.numTelCl.toString(), mensaje) },
        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimario, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = modifier.height(36.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.whatsapp_icono),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.enviar_wpp_label))
    }
}

@Composable
fun CardWpp(
    context: Context,
    cliente: Cliente?,
    mensaje: String
){
    //Log.d("CardWpp", "Cliente: ${cliente?.idCl}")
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(end = 8.dp)
            .clickable {
                val numTel = cliente?.numTelCl.toString()
                val mensaje = mensaje
                openWhatsApp(context, numTel, mensaje)
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = GreenPrimario,
            contentColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.whatsapp_icono),
                contentDescription = "WhatsApp",
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            /*Text(
                "WhatsApp",
                style = TextStyle(fontWeight = FontWeight.Normal),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )*/
        }
    }
}