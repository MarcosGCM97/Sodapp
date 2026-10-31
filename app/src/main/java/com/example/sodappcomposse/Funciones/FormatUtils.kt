package com.example.sodappcomposse.Funciones

import android.content.res.Resources
import com.example.sodappcomposse.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

fun formatearPrecio(precio: Double?): String {
    if (precio == null) return "0,00"
    val symbols = DecimalFormatSymbols(Locale("es", "AR"))
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(precio)
}

// A negative debt is money the client paid for a sale that was later deleted
fun textoSaldoCliente(resources: Resources, deuda: Double?): String {
    val saldo = deuda ?: 0.0
    return if (saldo < 0) {
        resources.getString(R.string.saldo_a_favor_format, formatearPrecio(abs(saldo)))
    } else {
        resources.getString(R.string.deuda_format, formatearPrecio(saldo))
    }
}
