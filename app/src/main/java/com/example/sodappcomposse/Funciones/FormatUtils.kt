package com.example.sodappcomposse.Funciones

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatearPrecio(precio: Double?): String {
    if (precio == null) return "0,00"
    val symbols = DecimalFormatSymbols(Locale("es", "AR"))
    symbols.groupingSeparator = '.'
    symbols.decimalSeparator = ','
    val df = DecimalFormat("#,##0.00", symbols)
    return df.format(precio)
}
