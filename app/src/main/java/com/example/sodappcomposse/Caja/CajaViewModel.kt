package com.example.sodappcomposse.Caja

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sodappcomposse.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class ReporteUiState {
    object Loading : ReporteUiState()
    data class Success(val reporte: Reporte) : ReporteUiState()
    data class Error(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : ReporteUiState()
}

@HiltViewModel
class CajaViewModel @Inject constructor(
    private val cajaRepository: CajaRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Header of the exported PDF
    val nombreEmpresa: Flow<String?> = userPreferencesRepository.userName

    // Today's date; replaced in tests
    internal var hoy: () -> LocalDate = { LocalDate.now() }

    private val _periodo = MutableStateFlow(Periodo.de(TipoPeriodo.MES, LocalDate.now()))
    val periodo: StateFlow<Periodo> = _periodo.asStateFlow()

    private val _uiState = MutableStateFlow<ReporteUiState>(ReporteUiState.Loading)
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    init {
        cargar()
    }

    // Switching the kind keeps the day the user was looking at (the start of the current period)
    fun seleccionarTipo(tipo: TipoPeriodo) {
        val actual = _periodo.value
        if (actual.tipo == tipo) return
        val referencia = if (actual.contiene(hoy())) hoy() else actual.desde
        seleccionarPeriodo(Periodo.de(tipo, referencia))
    }

    fun seleccionarFecha(fecha: LocalDate) {
        seleccionarPeriodo(Periodo.de(_periodo.value.tipo, minOf(fecha, hoy())))
    }

    fun anterior() = seleccionarPeriodo(_periodo.value.anterior())

    fun siguiente() {
        if (puedeAvanzar()) seleccionarPeriodo(_periodo.value.siguiente())
    }

    // Future periods have no data yet
    fun puedeAvanzar(): Boolean = _periodo.value.hasta.isBefore(hoy())

    fun reintentar() = cargar()

    internal fun seleccionarPeriodo(periodo: Periodo) {
        if (periodo == _periodo.value) return
        _periodo.value = periodo
        cargar()
    }

    private fun cargar() {
        val periodo = _periodo.value
        // Drop any in-flight request so a slow answer of another period never shows
        cargaJob?.cancel()
        _uiState.value = ReporteUiState.Loading
        cargaJob = viewModelScope.launch {
            val result = cajaRepository.getReporte(periodo)
            ensureActive()
            if (_periodo.value != periodo) return@launch
            _uiState.value = when (result) {
                is ReporteResult.Success -> ReporteUiState.Success(result.reporte)
                is ReporteResult.Error -> ReporteUiState.Error(result.messageRes, result.args)
            }
        }
    }
}
