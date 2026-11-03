package com.example.sodappcomposse.Ruta

import java.net.URLEncoder
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * The Google Maps app takes 9 waypoints plus the destination; the origin is left out so the
 * first leg starts at the phone's current location. Every later leg starts at the previous
 * leg's last point, so each leg adds up to 10 new points.
 */
const val PUNTOS_POR_TRAMO = 10

private const val ESTADO_PENDIENTE = "pendiente"
private val FORMATO_FECHA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * One Maps leg. desde / hasta are the reparto positions (orden) of its first and last stop,
 * null when the leg only goes back to the plant.
 */
data class TramoRuta(
    val numero: Int,
    val desde: Int?,
    val hasta: Int?,
    val terminaEnPlanta: Boolean,
    val url: String
)

/** Where Maps should go: the coordinates when present, else "address, locality"; null when there is nothing. */
fun ubicacionMaps(direccion: String?, localidad: String?, lat: Double?, lng: Double?): String? {
    if (lat != null && lng != null) return "$lat,$lng"
    val texto = listOfNotNull(direccion?.trim(), localidad?.trim())
        .filter { it.isNotEmpty() }
        .joinToString(", ")
    return texto.ifEmpty { null }
}

/**
 * Legs of the route: only the pendiente stops, in reparto order, then the plant when it is
 * configured. Stops without any location are skipped.
 */
fun armarTramos(ruta: Ruta): List<TramoRuta> {
    data class Punto(val ubicacion: String, val orden: Int?)

    val puntos = ruta.paradas
        .filter { it.estado == ESTADO_PENDIENTE }
        .mapNotNull { p -> ubicacionMaps(p.direccion, p.localidad, p.lat, p.lng)?.let { Punto(it, p.orden) } }
        .toMutableList()
    ruta.planta?.let { pl ->
        ubicacionMaps(pl.direccion, pl.localidad, pl.lat, pl.lng)?.let { puntos.add(Punto(it, null)) }
    }

    var origen: String? = null
    return puntos.chunked(PUNTOS_POR_TRAMO).mapIndexed { i, tramo ->
        val ordenes = tramo.mapNotNull { it.orden }
        val url = urlMaps(origen, tramo.dropLast(1).map { it.ubicacion }, tramo.last().ubicacion)
        origen = tramo.last().ubicacion
        TramoRuta(
            numero = i + 1,
            desde = ordenes.firstOrNull(),
            hasta = ordenes.lastOrNull(),
            terminaEnPlanta = tramo.last().orden == null,
            url = url
        )
    }
}

/** Maps URL (https://developers.google.com/maps/documentation/urls/get-started#directions-action). */
fun urlMaps(origen: String?, intermedios: List<String>, destino: String): String {
    val sb = StringBuilder("https://www.google.com/maps/dir/?api=1")
    if (origen != null) sb.append("&origin=").append(codificar(origen))
    sb.append("&destination=").append(codificar(destino))
    if (intermedios.isNotEmpty()) sb.append("&waypoints=").append(codificar(intermedios.joinToString("|")))
    sb.append("&travelmode=driving")
    return sb.toString()
}

private fun codificar(texto: String): String = URLEncoder.encode(texto, "UTF-8").replace("+", "%20")

/** Whole minutes between the trip start and end; null when either is missing or unreadable. */
fun duracionMinutos(inicio: String?, fin: String?): Long? {
    if (inicio == null || fin == null) return null
    return try {
        Duration.between(
            LocalDateTime.parse(inicio, FORMATO_FECHA_HORA),
            LocalDateTime.parse(fin, FORMATO_FECHA_HORA)
        ).toMinutes().coerceAtLeast(0)
    } catch (e: DateTimeParseException) {
        null
    }
}
