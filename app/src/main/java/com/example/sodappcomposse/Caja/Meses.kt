package com.example.sodappcomposse.Caja

enum class Meses(val numero: Int) {
    ENERO(1),
    FEBRERO(2),
    MARZO(3),
    ABRIL(4),
    MAYO(5),
    JUNIO(6),
    JULIO(7),
    AGOSTO(8),
    SEPTIEMBRE(9),
    OCTUBRE(10),
    NOVIEMBRE(11),
    DICIEMBRE(12); // Punto y coma es opcional si no hay más miembros después de las constantes

    // Puedes añadir métodos aquí si es necesario
    companion object {
        fun fromNumero(numero: Int): Meses? {
            return entries.find { it.numero == numero }
            // O antes de Kotlin 1.9: return values().find { it.numero == numero }
        }
    }
}