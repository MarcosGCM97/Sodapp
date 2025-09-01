package com.example.sodappcomposse.Funciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

// Funciones openWhatsApp y openDialer
fun openWhatsApp(context: Context, numero: String, mensaje: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "https://api.whatsapp.com/send?phone=$numero&text=${Uri.encode(mensaje)}"
        intent.data = Uri.parse(url)
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp no está instalado.", Toast.LENGTH_SHORT).show()
    }
}