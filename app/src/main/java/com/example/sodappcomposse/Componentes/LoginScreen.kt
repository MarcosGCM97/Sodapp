package com.example.sodappcomposse.Componentes

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.example.sodappcomposse.ui.theme.azulFuerte
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import com.example.sodappcomposse.IngresoUsuario.LoginViewModel
import com.example.sodappcomposse.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sodappcomposse.IngresoUsuario.LoginUiState

@Composable
fun LoginScreen(navBienvenida: (String) -> Unit){
    EncabezadoLogin()
    CuerpoLogin(navBienvenida)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncabezadoLogin() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        Text(
            text = "SodApp",
            color = azulFuerte,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
        )

        Text(
            text = "Usuario",
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
                .height(50.dp)
                .width(50.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuerpoLogin(
    navBienvenida: (String) -> Unit,
    loginModel: LoginViewModel = viewModel()
    ){
    var nombre by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    val contexto = LocalContext.current
    var passShow by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val loginUiState = loginModel.loginUiState
    val login = loginModel.usuario

    LaunchedEffect(loginUiState) {
        when (val currentState = loginModel.loginUiState) {
            is LoginUiState.Success -> {
                Toast.makeText(contexto, currentState.message, Toast.LENGTH_SHORT).show()
                navBienvenida(nombre) // Navega aquí
                // Opcional: Resetea el estado en el ViewModel para evitar navegaciones repetidas
                // loginModel.resetLoginState() // Necesitarías esta función en LoginViewModel
            }
            is LoginUiState.Error -> {
                Toast.makeText(contexto, currentState.message, Toast.LENGTH_SHORT).show()
                // loginModel.resetLoginState()
            }
            is LoginUiState.Loading -> {
                // El estado isLoading ya se deriva arriba, puedes mostrar un indicador global
            }
            is LoginUiState.Idle -> {
                // Estado inicial
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_manage_accounts_24),
            contentDescription = "Icono",
            modifier = Modifier
                .height(100.dp)
                .width(100.dp),
            tint = Color.Gray
        )

        Text(
            text = "Ingresa con tu Usuario",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height((40.dp)))

        TextField(
            value = nombre,
            onValueChange = {nombre=it},
            leadingIcon = {Icon(painter = painterResource(R.drawable.baseline_email_24), contentDescription = "email")},
            placeholder = {Text(text="nombre electronico")},
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(25.dp), color = Color.Gray),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height((10.dp)))

        TextField(
            value = contrasenia,
            onValueChange = {contrasenia = it},
            leadingIcon = {Icon(painter = painterResource(R.drawable.baseline_key_24), contentDescription = "email")},
            placeholder = {Text(text="Contraseña")},
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(25.dp), color = Color.Gray),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            ),
            maxLines = 1,
            visualTransformation = if(passShow) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val iconoShow: Painter = if(passShow) painterResource(R.drawable.baseline_visibility_24) else painterResource(
                    R.drawable.baseline_visibility_off_24)

                IconButton(
                    onClick = {
                        passShow = !passShow
                    }
                ){
                    Icon(
                        painter = iconoShow,
                        contentDescription = "Icono visivilidad"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height((10.dp)))

        Button(
            onClick = {
                if(nombre.isNotBlank() && contrasenia.isNotBlank()){
                    loginModel.login(nombre, contrasenia)
                    Toast.makeText(contexto, "Nombre/Contraseña valido", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(contexto, "Nombre/Contraseña invalido", Toast.LENGTH_SHORT).show()
                }
                /*if(nombre != "" && contrasenia != ""){
                    navBienvenida(nombre)
                }else{
                    Toast.makeText(contexto, "Nombre/Contraseña invalido", Toast.LENGTH_SHORT).show()
                }*/
                      },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }
    }
}
/*
@Preview(showSystemUi = true)
@Composable
fun PreviewLogin(){
    LoginScreen(navBienvenida = { nombre ->
        println("Preview: Navegar a Bienvenida con nombre: $nombre")
        // No ocurrirá una navegación real en la preview
    })
}*/
