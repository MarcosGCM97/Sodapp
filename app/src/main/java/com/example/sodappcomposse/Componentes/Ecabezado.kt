package com.example.sodappcomposse.Componentes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sodappcomposse.IngresoUsuario.LoginViewModel
import com.example.sodappcomposse.R
import com.example.sodappcomposse.ui.theme.azulFuerte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Encabezado(
    nombre: String,
    navLogin: () -> Unit,
    loginModel: LoginViewModel = viewModel()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 1.dp, end = 1.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_water_drop_24),
            contentDescription = "icono",
            modifier = Modifier
                .size(60.dp),
            tint = Color.Gray
        )
        Text(
            text = "SodApp",
            color = azulFuerte,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
        )
        Text(
            text = nombre,
            color = Color.Blue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 15.dp)
        )

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Icono",
            modifier = Modifier
                .size(50.dp)
        )

        TextButton(
            onClick = {
                navLogin()
                loginModel.resetLoginState()
                      },
            modifier = Modifier
                .width(70.dp)
        ) {
            Text(
                text = "Salir",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}