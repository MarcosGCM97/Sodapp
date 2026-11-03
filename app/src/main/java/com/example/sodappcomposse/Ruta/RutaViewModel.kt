package com.example.sodappcomposse.Ruta

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/** "Llegué a planta" is offered once the trip started, is not finished and no stop is pendiente. */
fun puedeFinalizar(ruta: Ruta?): Boolean =
    ruta != null && ruta.inicio != null && ruta.fin == null &&
        ruta.paradas.isNotEmpty() && ruta.paradas.none { it.estado == "pendiente" }

/** Route of a reparto in Google Maps legs, the trip times and the plant setting. */
@HiltViewModel
class RutaViewModel @Inject constructor(
    private val repository: RutaRepository
) : ViewModel() {

    private val _ruta = mutableStateOf<Ruta?>(null)
    val ruta: State<Ruta?> = _ruta

    private val _tramos = mutableStateOf<List<TramoRuta>>(emptyList())
    val tramos: State<List<TramoRuta>> = _tramos

    private val _planta = mutableStateOf<Planta?>(null)
    val planta: State<Planta?> = _planta

    private val _uiState = mutableStateOf<ProductoUiState>(ProductoUiState.Idle)
    val uiState: State<ProductoUiState> = _uiState

    private var carga: Job? = null

    /**
     * Only the latest load counts: a slower earlier response can not overwrite it. A failed load
     * keeps the last route and stays quiet, so the optional route card never interrupts deliveries.
     */
    fun cargar(repartoId: Int) {
        carga?.cancel()
        carga = viewModelScope.launch {
            val result = repository.getRuta(repartoId)
            if (result is ProductoResult.Success) {
                _ruta.value = result.data
                _tramos.value = result.data?.let { armarTramos(it) } ?: emptyList()
            }
        }
    }

    /** Called when a leg is opened in Maps; only the first call stores the start. */
    fun iniciar(repartoId: Int) {
        viewModelScope.launch {
            when (val result = repository.iniciar(repartoId)) {
                is ProductoResult.Success -> cargar(repartoId)
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun finalizar(repartoId: Int) {
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            when (val result = repository.finalizar(repartoId)) {
                is ProductoResult.Success -> {
                    _uiState.value = ProductoUiState.Success(R.string.recorrido_finalizado)
                    cargar(repartoId)
                }
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun cargarPlanta() {
        viewModelScope.launch {
            when (val result = repository.getPlanta()) {
                is ProductoResult.Success -> _planta.value = result.data
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun guardarPlanta(direccion: String, localidad: String, lat: Double?, lng: Double?) {
        if (direccion.isBlank() || localidad.isBlank()) {
            _uiState.value = ProductoUiState.Error(R.string.campos_requeridos_error)
            return
        }
        if (_uiState.value is ProductoUiState.Loading) return
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            val planta = Planta(direccion.trim(), localidad.trim(), lat, lng)
            when (val result = repository.guardarPlanta(planta)) {
                is ProductoResult.Success -> {
                    _uiState.value = ProductoUiState.Success(R.string.planta_guardada)
                    cargarPlanta()
                }
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetState() {
        _uiState.value = ProductoUiState.Idle
    }
}
