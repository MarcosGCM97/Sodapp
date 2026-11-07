package com.example.sodappcomposse.Caja

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

// ---------------------------------------------------------------- API payload (reporte.php)

data class ReporteResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("desde") val desde: String? = null,
    @SerializedName("hasta") val hasta: String? = null,
    @SerializedName("resumen") val resumen: ResumenReporte? = null,
    @SerializedName("anterior") val anterior: ResumenAnterior? = null,
    @SerializedName("cobros") val cobros: List<CobroForma>? = null,
    @SerializedName("productos") val productos: List<ProductoReporte>? = null,
    @SerializedName("clientes") val clientes: List<ClienteReporte>? = null,
    @SerializedName("dias") val dias: List<DiaReporte>? = null,
    @SerializedName("detalle") val detalle: List<LineaReporte>? = null,
    @SerializedName("primerCobro") val primerCobro: String? = null
)

data class ResumenReporte(
    @SerializedName("vendido") val vendido: Double = 0.0,
    @SerializedName("cobrado") val cobrado: Double = 0.0,
    @SerializedName("diferencia") val diferencia: Double = 0.0,
    @SerializedName("ventas") val ventas: Int = 0,
    @SerializedName("clientes") val clientes: Int = 0,
    @SerializedName("lineasEstimadas") val lineasEstimadas: Int = 0
)

data class ResumenAnterior(
    @SerializedName("desde") val desde: String = "",
    @SerializedName("hasta") val hasta: String = "",
    @SerializedName("vendido") val vendido: Double = 0.0,
    @SerializedName("cobrado") val cobrado: Double = 0.0,
    @SerializedName("ventas") val ventas: Int = 0
)

// Amount collected in the period with one payment method
data class CobroForma(
    @SerializedName("forma") val formaPago: String = "",
    @SerializedName("total") val total: Double = 0.0
)

data class ProductoReporte(
    @SerializedName("producto") val producto: String? = null,
    @SerializedName("cantidad") val cantidad: Int = 0,
    @SerializedName("monto") val monto: Double = 0.0
)

data class ClienteReporte(
    @SerializedName("clienteId") val clienteId: Int? = null,
    @SerializedName("cliente") val cliente: String? = null,
    @SerializedName("ventas") val ventas: Int = 0,
    @SerializedName("monto") val monto: Double = 0.0
)

data class DiaReporte(
    @SerializedName("fecha") val fecha: String = "",
    @SerializedName("monto") val monto: Double = 0.0
)

data class LineaReporte(
    @SerializedName("fecha") val fecha: String = "",
    @SerializedName("clienteId") val clienteId: Int? = null,
    @SerializedName("cliente") val cliente: String? = null,
    @SerializedName("producto") val producto: String? = null,
    @SerializedName("cantidad") val cantidad: Int = 0,
    @SerializedName("monto") val monto: Double = 0.0,
    @SerializedName("estimado") val estimado: Boolean = false
)

// ---------------------------------------------------------------- domain

// Report of a period as the screen and the PDF show it; every list is non-null
data class Reporte(
    val periodo: Periodo,
    val resumen: ResumenReporte,
    val anterior: ResumenAnterior,
    val cobros: List<CobroForma>,
    val productos: List<ProductoReporte>,
    val clientes: List<ClienteReporte>,
    val dias: List<DiaReporte>,
    val detalle: List<LineaReporte>,
    val primerCobro: LocalDateTime?
) {
    // Average amount per sale rounded half-up to cents (450.125 -> 450.13); null without sales
    val ticketPromedio: Double?
        get() = if (resumen.ventas > 0) {
            BigDecimal.valueOf(resumen.vendido).divide(BigDecimal(resumen.ventas), 2, RoundingMode.HALF_UP).toDouble()
        } else null

    // Payments were not recorded before the first one: the period starts earlier, so cobrado is partial
    val cobrosParciales: Boolean
        get() = primerCobro != null && primerCobro.toLocalDate().isAfter(periodo.desde)

    val ventasDetalle: List<VentaDetalle>
        get() = agruparVentas(detalle)
}

// One registration: every line of a sale shares cliente and timestamp
data class VentaDetalle(
    val fecha: String,
    val clienteId: Int?,
    val cliente: String?,
    val lineas: List<LineaReporte>,
    val total: Double
)

fun agruparVentas(detalle: List<LineaReporte>): List<VentaDetalle> =
    detalle.groupBy { it.fecha to it.clienteId }.map { (clave, lineas) ->
        VentaDetalle(
            fecha = clave.first,
            clienteId = clave.second,
            cliente = lineas.first().cliente,
            lineas = lineas,
            // Exact decimal sum of the server-rounded amounts
            total = lineas.fold(BigDecimal.ZERO) { acc, l -> acc + BigDecimal.valueOf(l.monto) }.toDouble()
        )
    }

// Percentage of parte over total; null when the total is zero
fun porcentaje(parte: Double, total: Double): Double? = if (total == 0.0) null else parte / total * 100

// Change from anterior to actual in percent; null when there is nothing to compare with
fun variacion(actual: Double, anterior: Double): Double? =
    if (anterior == 0.0) null else (actual - anterior) / anterior * 100

// ---------------------------------------------------------------- periods

enum class TipoPeriodo { DIA, SEMANA, MES }

// Inclusive date range of one day, one Monday-to-Sunday week or one calendar month
data class Periodo(val tipo: TipoPeriodo, val desde: LocalDate, val hasta: LocalDate) {

    fun anterior(): Periodo = de(tipo, desde.minusDays(1))

    fun siguiente(): Periodo = de(tipo, hasta.plusDays(1))

    fun contiene(fecha: LocalDate): Boolean = !fecha.isBefore(desde) && !fecha.isAfter(hasta)

    companion object {
        fun de(tipo: TipoPeriodo, fecha: LocalDate): Periodo = when (tipo) {
            TipoPeriodo.DIA -> Periodo(tipo, fecha, fecha)
            TipoPeriodo.SEMANA -> {
                val lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Periodo(tipo, lunes, lunes.plusDays(6))
            }
            TipoPeriodo.MES -> Periodo(
                tipo,
                fecha.withDayOfMonth(1),
                fecha.with(TemporalAdjusters.lastDayOfMonth())
            )
        }
    }
}

val FORMATO_API: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val FORMATO_FECHA_HORA_API: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun parseFechaHoraApi(valor: String?): LocalDateTime? =
    valor?.let { runCatching { LocalDateTime.parse(it, FORMATO_FECHA_HORA_API) }.getOrNull() }
