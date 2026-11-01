package com.example.sodappcomposse.Reparto

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Cliente.ClienteResult
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoResult
import com.example.sodappcomposse.Producto.ProductoUiState
import com.example.sodappcomposse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Builds the reparto of one date: create it, add/remove stops, plan their lines and order them. */
@HiltViewModel
class RepartoViewModel @Inject constructor(
    private val repartoRepository: RepartoRepository,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _fecha = mutableStateOf(LocalDate.now())
    val fecha: State<LocalDate> = _fecha

    // Null while loading or when the date has no reparto yet (see cargando)
    private val _reparto = mutableStateOf<Reparto?>(null)
    val reparto: State<Reparto?> = _reparto

    private val _cargando = mutableStateOf(false)
    val cargando: State<Boolean> = _cargando

    private val _clientes = mutableStateOf<List<Cliente>>(emptyList())
    val clientes: State<List<Cliente>> = _clientes

    private val _productos = mutableStateOf<List<Producto>>(emptyList())
    val productos: State<List<Producto>> = _productos

    private val _uiState = mutableStateOf<ProductoUiState>(ProductoUiState.Idle)
    val uiState: State<ProductoUiState> = _uiState

    fun cargar(fecha: LocalDate = _fecha.value) {
        _fecha.value = fecha
        _cargando.value = true
        viewModelScope.launch {
            val result = repartoRepository.getReparto(fecha.toString())
            // A newer date was chosen while this one loaded
            if (_fecha.value != fecha) return@launch
            when (result) {
                is ProductoResult.Success -> _reparto.value = result.data
                is ProductoResult.Error -> {
                    _reparto.value = null
                    _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
                }
            }
            _cargando.value = false
        }
    }

    fun cambiarDia(dias: Long) = cargar(_fecha.value.plusDays(dias))

    /** Clients for "agregar parada" and products for the lines; the backend validates both. */
    fun cargarCatalogos() {
        viewModelScope.launch {
            when (val result = clienteRepository.getClientes()) {
                is ClienteResult.Success -> _clientes.value = result.data.clientes
                is ClienteResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
            when (val result = productoRepository.getProductos()) {
                is ProductoResult.Success -> _productos.value = result.data.productos
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun crear(origen: OrigenReparto) {
        val fecha = _fecha.value
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            when (val result = repartoRepository.crearReparto(fecha.toString(), origen)) {
                is ProductoResult.Success -> {
                    if (_fecha.value == fecha) _reparto.value = result.data.reparto
                    _uiState.value = ProductoUiState.Success(R.string.reparto_creado)
                }
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun agregarParada(clienteId: Int) {
        val repartoId = _reparto.value?.id ?: return
        ejecutar(R.string.parada_agregada) { repartoRepository.agregarParada(repartoId, clienteId) }
    }

    fun quitarParada(paradaId: Int) {
        ejecutar(R.string.parada_quitada) { repartoRepository.quitarParada(paradaId) }
    }

    /** Replaces the planned lines of a stop; products left at 0 are dropped, a null precio takes pr_val. */
    fun guardarLineas(paradaId: Int, lineas: List<LineaRepartoRequest>) {
        if (lineas.any { it.cantidad < 0 }) {
            _uiState.value = ProductoUiState.Error(R.string.error_cantidad_negativa)
            return
        }
        val planificadas = lineas.filter { it.cantidad > 0 }
        if (planificadas.any { it.precio != null && it.precio <= 0 }) {
            _uiState.value = ProductoUiState.Error(R.string.error_precio_invalido)
            return
        }
        ejecutar(R.string.lineas_reparto_guardadas) { repartoRepository.guardarLineas(paradaId, planificadas) }
    }

    /** Moves a stop [delta] places (-1 up, +1 down); shown at once and reloaded if saving fails. */
    fun mover(paradaId: Int, delta: Int) {
        val actual = _reparto.value ?: return
        val paradas = actual.paradas.toMutableList()
        val desde = paradas.indexOfFirst { it.id == paradaId }
        val hasta = desde + delta
        if (desde < 0 || hasta !in paradas.indices) return

        paradas.add(hasta, paradas.removeAt(desde))
        _reparto.value = actual.copy(paradas = paradas)
        viewModelScope.launch {
            val result = repartoRepository.ordenarParadas(actual.id, paradas.map { it.id })
            if (result is ProductoResult.Error) {
                _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
                cargar()
            }
        }
    }

    fun guardarPlantilla() {
        val repartoId = _reparto.value?.id ?: return
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            when (val result = repartoRepository.guardarPlantilla(repartoId)) {
                is ProductoResult.Success -> _uiState.value = ProductoUiState.Success(R.string.plantilla_reparto_guardada)
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }

    fun resetState() {
        _uiState.value = ProductoUiState.Idle
    }

    // Runs a stop edit and reloads the reparto so the screen shows what the backend stored
    private fun ejecutar(exito: Int, accion: suspend () -> ProductoResult<RepartoResponse>) {
        _uiState.value = ProductoUiState.Loading
        viewModelScope.launch {
            when (val result = accion()) {
                is ProductoResult.Success -> {
                    _uiState.value = ProductoUiState.Success(exito)
                    cargar()
                }
                is ProductoResult.Error -> _uiState.value = ProductoUiState.Error(result.messageRes, result.args)
            }
        }
    }
}
