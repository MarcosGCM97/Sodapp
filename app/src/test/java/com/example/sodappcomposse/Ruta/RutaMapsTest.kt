package com.example.sodappcomposse.Ruta

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RutaMapsTest {

    private val base = "https://www.google.com/maps/dir/?api=1"

    private fun parada(orden: Int, estado: String = "pendiente", lat: Double? = null, lng: Double? = null) = RutaParada(
        paradaId = 100 + orden, orden = orden, estado = estado, clienteId = orden,
        nombre = "C$orden", direccion = "Calle $orden", localidad = "La Calera", lat = lat, lng = lng
    )

    private val planta = Planta(direccion = "Ruta 20 km 6", localidad = "La Calera")

    @Test
    fun `location uses coordinates when present`() {
        assertEquals("-31.343512,-64.335871", ubicacionMaps("San Martin 120", "La Calera", -31.343512, -64.335871))
    }

    @Test
    fun `location falls back to address and locality`() {
        assertEquals("San Martin 120, La Calera", ubicacionMaps(" San Martin 120 ", "La Calera", null, null))
        assertEquals("San Martin 120", ubicacionMaps("San Martin 120", null, -31.0, null))
        assertNull(ubicacionMaps(" ", null, null, null))
    }

    @Test
    fun `url leaves the origin out and encodes destination and waypoints`() {
        assertEquals(
            "$base&destination=Ruta%2020%2C%20La%20Calera&waypoints=-31.3%2C-64.3%7CCalle%201&travelmode=driving",
            urlMaps(null, listOf("-31.3,-64.3", "Calle 1"), "Ruta 20, La Calera")
        )
        assertEquals("$base&origin=A&destination=B&travelmode=driving", urlMaps("A", emptyList(), "B"))
    }

    @Test
    fun `short route is one leg from here through the stops back to the plant`() {
        val tramos = armarTramos(Ruta(paradas = listOf(parada(1), parada(2, lat = -31.3, lng = -64.3)), planta = planta))
        assertEquals(1, tramos.size)
        assertEquals(TramoRuta(1, 1, 2, true, urlMaps(null, listOf("Calle 1, La Calera", "-31.3,-64.3"), "Ruta 20 km 6, La Calera")), tramos[0])
    }

    @Test
    fun `only pendiente stops are part of the route`() {
        val tramos = armarTramos(Ruta(paradas = listOf(parada(1, "entregada"), parada(2), parada(3, "no_entregada")), planta = planta))
        assertEquals(TramoRuta(1, 2, 2, true, urlMaps(null, listOf("Calle 2, La Calera"), "Ruta 20 km 6, La Calera")), tramos.single())
    }

    @Test
    fun `long route is split in legs of 10 points, each starting where the previous ended`() {
        val paradas = (1..23).map { parada(it) }
        val tramos = armarTramos(Ruta(paradas = paradas, planta = planta))
        assertEquals(3, tramos.size)
        assertEquals(listOf(1 to 10, 11 to 20, 21 to 23), tramos.map { it.desde to it.hasta })
        assertEquals(listOf(false, false, true), tramos.map { it.terminaEnPlanta })
        assertEquals(urlMaps(null, (1..9).map { "Calle $it, La Calera" }, "Calle 10, La Calera"), tramos[0].url)
        assertEquals(urlMaps("Calle 10, La Calera", (11..19).map { "Calle $it, La Calera" }, "Calle 20, La Calera"), tramos[1].url)
        assertEquals(urlMaps("Calle 20, La Calera", (21..23).map { "Calle $it, La Calera" }, "Ruta 20 km 6, La Calera"), tramos[2].url)
    }

    @Test
    fun `the plant can be a leg of its own`() {
        val tramos = armarTramos(Ruta(paradas = (1..10).map { parada(it) }, planta = planta))
        assertEquals(2, tramos.size)
        assertEquals(TramoRuta(2, null, null, true, urlMaps("Calle 10, La Calera", emptyList(), "Ruta 20 km 6, La Calera")), tramos[1])
    }

    @Test
    fun `without plant the last leg ends at the last stop`() {
        val tramos = armarTramos(Ruta(paradas = listOf(parada(1), parada(2)), planta = null))
        assertEquals(TramoRuta(1, 1, 2, false, urlMaps(null, listOf("Calle 1, La Calera"), "Calle 2, La Calera")), tramos.single())
    }

    @Test
    fun `nothing pending and no plant means no legs`() {
        assertTrue(armarTramos(Ruta(paradas = listOf(parada(1, "entregada")))).isEmpty())
    }

    @Test
    fun `duration in minutes between start and end`() {
        assertEquals(205L, duracionMinutos("2026-11-03 08:00:00", "2026-11-03 11:25:59"))
        assertNull(duracionMinutos("2026-11-03 08:00:00", null))
        assertNull(duracionMinutos("ayer", "2026-11-03 11:25:00"))
    }
}
