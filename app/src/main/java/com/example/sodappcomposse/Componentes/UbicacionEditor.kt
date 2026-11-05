package com.example.sodappcomposse.Componentes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sodappcomposse.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

private const val ESPERA_GPS_MS = 15_000L
private const val ESPERA_RED_MS = 10_000L

/** Address found by the on-device Geocoder, waiting for the user to accept it. */
private data class UbicacionPropuesta(val descripcion: String, val lat: Double, val lng: Double)

/**
 * Optional coordinates of a client or the plant: from the phone GPS ("Guardar ubicación aquí")
 * or from the typed address and locality ("Ubicar por dirección", confirmed before use).
 * Free: Android LocationManager and Geocoder, no API key.
 */
@Composable
fun UbicacionEditor(
    direccion: String,
    localidad: String,
    coordenadas: Pair<Double, Double>?,
    onCambiar: (Pair<Double, Double>?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var buscando by remember { mutableStateOf(false) }
    var propuesta by remember { mutableStateOf<UbicacionPropuesta?>(null) }

    fun leerGps() {
        buscando = true
        scope.launch {
            val ubicacion = ubicacionActual(context)
            buscando = false
            if (ubicacion == null) toast(context, R.string.ubicacion_no_disponible)
            else onCambiar(ubicacion.latitude to ubicacion.longitude)
        }
    }

    val permisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { otorgados ->
        if (otorgados.values.any { it }) leerGps() else toast(context, R.string.permiso_ubicacion_denegado)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            coordenadas?.let { stringResource(R.string.ubicacion_guardada_format, it.first.toString(), it.second.toString()) }
                ?: stringResource(R.string.sin_ubicacion),
            style = MaterialTheme.typography.bodySmall
        )
        if (buscando) {
            CircularProgressIndicator()
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {
                    if (tienePermisoUbicacion(context)) leerGps()
                    else permisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.guardar_ubicacion_aqui))
                }
                OutlinedButton(
                    onClick = {
                        buscando = true
                        scope.launch {
                            propuesta = buscarDireccion(context, direccion, localidad)
                            buscando = false
                            if (propuesta == null) toast(context, R.string.ubicacion_no_encontrada)
                        }
                    },
                    enabled = direccion.isNotBlank() && localidad.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.ubicar_por_direccion))
                }
            }
            if (coordenadas != null) {
                TextButton(onClick = { onCambiar(null) }) { Text(stringResource(R.string.quitar_ubicacion)) }
            }
        }
    }

    propuesta?.let { p ->
        AlertDialog(
            onDismissRequest = { propuesta = null },
            title = { Text(stringResource(R.string.ubicacion_encontrada_titulo)) },
            text = { Text(p.descripcion) },
            confirmButton = {
                TextButton(onClick = {
                    onCambiar(p.lat to p.lng)
                    propuesta = null
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { propuesta = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}

private fun toast(context: Context, mensaje: Int) {
    Toast.makeText(context, context.getString(mensaje), Toast.LENGTH_LONG).show()
}

private fun tienePermisoUbicacion(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/**
 * One fresh fix. GPS is used only with precise permission (approximate-only would throw), then
 * the network provider as fallback (indoors GPS often gets no fix); null when none answers in time.
 */
@SuppressLint("MissingPermission") // checked by the caller and per provider below
private suspend fun ubicacionActual(context: Context): Location? {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val preciso = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val proveedores = buildList {
        if (preciso) add(LocationManager.GPS_PROVIDER to ESPERA_GPS_MS)
        add(LocationManager.NETWORK_PROVIDER to ESPERA_RED_MS)
    }.filter { (p, _) -> runCatching { manager.isProviderEnabled(p) }.getOrDefault(false) }

    for ((proveedor, espera) in proveedores) {
        val fix = try {
            withTimeoutOrNull(espera) { unaUbicacion(context, manager, proveedor) }
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
        if (fix != null) return fix
    }
    return null
}

@SuppressLint("MissingPermission")
private suspend fun unaUbicacion(context: Context, manager: LocationManager, proveedor: String): Location? =
    suspendCancellableCoroutine { cont ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cancelacion = android.os.CancellationSignal()
            cont.invokeOnCancellation { cancelacion.cancel() }
            manager.getCurrentLocation(proveedor, cancelacion, ContextCompat.getMainExecutor(context)) {
                if (cont.isActive) cont.resume(it)
            }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (cont.isActive) cont.resume(location)
                }
            }
            cont.invokeOnCancellation { manager.removeUpdates(listener) }
            @Suppress("DEPRECATION")
            manager.requestSingleUpdate(proveedor, listener, Looper.getMainLooper())
        }
    }

/** "address, locality" through the on-device Geocoder; null when it finds nothing or is unavailable. */
private suspend fun buscarDireccion(context: Context, direccion: String, localidad: String): UbicacionPropuesta? {
    if (!Geocoder.isPresent()) return null
    val consulta = "${direccion.trim()}, ${localidad.trim()}"
    return withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION") // the listener overload needs API 33; this runs off the main thread
            val resultado = Geocoder(context, Locale("es", "AR")).getFromLocationName(consulta, 1)?.firstOrNull()
            resultado?.let {
                UbicacionPropuesta(it.getAddressLine(0) ?: consulta, it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }
}
